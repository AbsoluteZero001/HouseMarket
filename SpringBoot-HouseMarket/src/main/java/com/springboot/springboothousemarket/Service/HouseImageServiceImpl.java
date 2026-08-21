package com.springboot.springboothousemarket.Service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.springboot.springboothousemarket.Entity.HouseImage;
import com.springboot.springboothousemarket.Mapper.HouseImageMapper;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

@Service
public class HouseImageServiceImpl extends ServiceImpl<HouseImageMapper, HouseImage> implements HouseImageService {

    private static final List<String> ALLOWED_EXTENSIONS = List.of(".jpg", ".jpeg", ".png", ".gif", ".webp");

    @Value("${upload.dir:./uploads}")
    private String uploadDir;

    @Override
    public List<HouseImage> listByHouseId(Long houseId) {
        return list(new LambdaQueryWrapper<HouseImage>()
                .eq(HouseImage::getHouseId, houseId)
                .orderByAsc(HouseImage::getSortOrder)
                .orderByAsc(HouseImage::getId));
    }

    @Override
    public Map<Long, List<HouseImage>> listByHouseIds(Collection<Long> houseIds) {
        if (houseIds == null || houseIds.isEmpty()) {
            return Map.of();
        }
        List<HouseImage> images = list(new LambdaQueryWrapper<HouseImage>()
                .in(HouseImage::getHouseId, houseIds)
                .orderByAsc(HouseImage::getSortOrder)
                .orderByAsc(HouseImage::getId));
        return images.stream().collect(Collectors.groupingBy(HouseImage::getHouseId));
    }

    @Override
    @Transactional
    public HouseImage uploadImage(Long houseId, MultipartFile file, Integer sortOrder, boolean cover) throws IOException {
        if (houseId == null) {
            throw new RuntimeException("房源ID不能为空");
        }
        if (file == null || file.isEmpty()) {
            throw new RuntimeException("上传图片不能为空");
        }

        String originalName = StringUtils.cleanPath(file.getOriginalFilename() == null ? "image" : file.getOriginalFilename());
        String extension = getExtension(originalName);
        if (!ALLOWED_EXTENSIONS.contains(extension.toLowerCase(Locale.ROOT))) {
            throw new RuntimeException("仅支持 JPG、PNG、GIF、WEBP 格式图片");
        }
        if (file.getSize() > 5 * 1024 * 1024) {
            throw new RuntimeException("图片大小不能超过 5MB");
        }

        Path basePath = Paths.get(uploadDir).toAbsolutePath().normalize();
        Path houseDir = basePath.resolve("houses").resolve(String.valueOf(houseId)).normalize();
        if (!houseDir.startsWith(basePath)) {
            throw new RuntimeException("非法的房源图片目录");
        }
        Files.createDirectories(houseDir);

        String fileName = UUID.randomUUID().toString().replace("-", "") + extension;
        Path target = houseDir.resolve(fileName).normalize();
        if (!target.startsWith(houseDir)) {
            throw new RuntimeException("非法的房源图片文件名");
        }
        Files.copy(file.getInputStream(), target, StandardCopyOption.REPLACE_EXISTING);

        int nextSort = sortOrder != null ? sortOrder : listByHouseId(houseId).size();
        boolean shouldCover = cover || listByHouseId(houseId).isEmpty();
        if (shouldCover) {
            clearCover(houseId);
        }

        HouseImage image = new HouseImage();
        image.setHouseId(houseId);
        image.setImageUrl("/uploads/houses/" + houseId + "/" + fileName);
        image.setSortOrder(nextSort);
        image.setIsCover(shouldCover ? 1 : 0);
        image.setCreateTime(LocalDateTime.now());
        save(image);
        return image;
    }

    @Override
    @Transactional
    public HouseImage createImage(Long houseId, String imageUrl, Integer sortOrder, boolean cover) {
        if (houseId == null || imageUrl == null || imageUrl.isBlank()) {
            throw new RuntimeException("房源图片地址不能为空");
        }
        boolean shouldCover = cover || listByHouseId(houseId).isEmpty();
        if (shouldCover) {
            clearCover(houseId);
        }
        HouseImage image = new HouseImage();
        image.setHouseId(houseId);
        image.setImageUrl(imageUrl);
        image.setSortOrder(sortOrder != null ? sortOrder : listByHouseId(houseId).size());
        image.setIsCover(shouldCover ? 1 : 0);
        image.setCreateTime(LocalDateTime.now());
        save(image);
        return image;
    }

    @Override
    @Transactional
    public void deleteImage(Long houseId, Long imageId) {
        HouseImage image = getById(imageId);
        if (image == null || !image.getHouseId().equals(houseId)) {
            throw new RuntimeException("房源图片不存在");
        }
        removeById(imageId);
        deleteFile(image.getImageUrl());

        List<HouseImage> remaining = listByHouseId(houseId);
        if (image.getIsCover() != null && image.getIsCover() == 1 && !remaining.isEmpty()) {
            HouseImage first = remaining.stream()
                    .min(Comparator.comparing(HouseImage::getSortOrder).thenComparing(HouseImage::getId))
                    .orElse(null);
            if (first != null) {
                clearCover(houseId);
                first.setIsCover(1);
                updateById(first);
            }
        }
    }

    @Override
    @Transactional
    public HouseImage setCover(Long houseId, Long imageId) {
        HouseImage image = getById(imageId);
        if (image == null || !image.getHouseId().equals(houseId)) {
            throw new RuntimeException("房源图片不存在");
        }
        clearCover(houseId);
        image.setIsCover(1);
        updateById(image);
        return image;
    }

    @Override
    @Transactional
    public void reorder(Long houseId, List<Long> imageIds) {
        if (imageIds == null || imageIds.isEmpty()) {
            return;
        }
        List<HouseImage> existing = listByHouseId(houseId);
        Map<Long, HouseImage> byId = existing.stream()
                .collect(Collectors.toMap(HouseImage::getId, image -> image));
        for (int i = 0; i < imageIds.size(); i++) {
            HouseImage image = byId.get(imageIds.get(i));
            if (image == null) {
                continue;
            }
            image.setSortOrder(i);
            updateById(image);
        }
    }

    @Override
    @Transactional
    public void deleteByHouseId(Long houseId) {
        List<HouseImage> images = listByHouseId(houseId);
        for (HouseImage image : images) {
            deleteFile(image.getImageUrl());
        }
        remove(new LambdaQueryWrapper<HouseImage>().eq(HouseImage::getHouseId, houseId));
    }

    private void clearCover(Long houseId) {
        lambdaUpdate()
                .eq(HouseImage::getHouseId, houseId)
                .eq(HouseImage::getIsCover, 1)
                .set(HouseImage::getIsCover, 0)
                .update();
    }

    private String getExtension(String fileName) {
        int dot = fileName.lastIndexOf('.');
        return dot >= 0 ? fileName.substring(dot) : "";
    }

    private void deleteFile(String imageUrl) {
        if (imageUrl == null || !imageUrl.startsWith("/uploads/")) {
            return;
        }
        String relative = imageUrl.substring("/uploads/".length());
        Path basePath = Paths.get(uploadDir).toAbsolutePath().normalize();
        Path target = basePath.resolve(relative).normalize();
        if (!target.startsWith(basePath)) {
            return;
        }
        try {
            Files.deleteIfExists(target);
        } catch (IOException ignored) {
            // 删除文件失败不影响数据库一致性，服务层异常会让事务回滚。
        }
    }
}
