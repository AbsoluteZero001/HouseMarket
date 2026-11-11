package com.springboot.springboothousemarket.Service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.springboot.springboothousemarket.Entity.HouseImage;
import com.springboot.springboothousemarket.Mapper.HouseImageMapper;
import com.springboot.springboothousemarket.Util.ImageUploadValidator;
import com.springboot.springboothousemarket.dto.BusinessException;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
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

    @Value("${upload.dir:./uploads}")
    private String uploadDir;

    /**
     * 单个房源允许的图片数量上限。
     *
     * <p>旧实现没有上限：房东（或拿到 LANDLORD 角色的账号）可以无限次上传，
     * 撑爆磁盘与 {@code house_image} 表。发房源、传图片都应有明确边界。
     */
    @Value("${app.house.max-images:20}")
    private int maxImagesPerHouse;

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
    public HouseImage uploadImage(Long houseId, MultipartFile file, String imageType, Integer sortOrder, boolean cover) throws IOException {
        if (houseId == null) {
            throw new BusinessException("房源ID不能为空");
        }
        if (file == null || file.isEmpty()) {
            throw new BusinessException("上传图片不能为空");
        }
        // 数量上限：防止单个房源无限上传，撑爆磁盘与数据库
        assertWithinImageLimit(houseId);

        // 按文件真实字节判定类型：不信任用户提供的文件名与 Content-Type
        String extension = ImageUploadValidator.validateAndDetectExtension(file, "房源图片");

        Path basePath = Paths.get(uploadDir).toAbsolutePath().normalize();
        Path houseDir = basePath.resolve("houses").resolve(String.valueOf(houseId)).normalize();
        if (!houseDir.startsWith(basePath)) {
            throw new RuntimeException("非法的房源图片目录");
        }
        Files.createDirectories(houseDir);

        String fileName = ImageUploadValidator.randomFileName(extension);
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
        image.setImageType(normalizeImageType(imageType));
        image.setSortOrder(nextSort);
        image.setIsCover(shouldCover ? 1 : 0);
        image.setCreateTime(LocalDateTime.now());
        save(image);
        return image;
    }

    @Override
    @Transactional
    public HouseImage createImage(Long houseId, String imageUrl, String imageType, Integer sortOrder, boolean cover) {
        if (houseId == null || imageUrl == null || imageUrl.isBlank()) {
            throw new BusinessException("房源图片地址不能为空");
        }
        // 与上传入口一致的数量上限：防止通过「批量 imageUrls」一次性塞爆表
        assertWithinImageLimit(houseId);
        boolean shouldCover = cover || listByHouseId(houseId).isEmpty();
        if (shouldCover) {
            clearCover(houseId);
        }
        HouseImage image = new HouseImage();
        image.setHouseId(houseId);
        image.setImageUrl(imageUrl);
        image.setImageType(normalizeImageType(imageType));
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

    /**
     * 校验房源当前图片数量是否已达上限。
     */
    private void assertWithinImageLimit(Long houseId) {
        int limit = Math.max(1, maxImagesPerHouse);
        long existing = count(new LambdaQueryWrapper<HouseImage>().eq(HouseImage::getHouseId, houseId));
        if (existing >= limit) {
            throw new BusinessException("每个房源最多上传 " + limit + " 张图片，请先删除部分图片");
        }
    }

    private String normalizeImageType(String imageType) {
        if (imageType == null || imageType.isBlank()) {
            return "OTHER";
        }
        return imageType.toUpperCase(Locale.ROOT);
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
