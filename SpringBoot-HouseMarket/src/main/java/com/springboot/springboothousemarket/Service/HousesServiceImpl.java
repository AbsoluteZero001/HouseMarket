package com.springboot.springboothousemarket.Service;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.springboot.springboothousemarket.Entity.HouseImage;
import com.springboot.springboothousemarket.Entity.Houses;
import com.springboot.springboothousemarket.Entity.Users;
import com.springboot.springboothousemarket.Mapper.HousesMapper;
import com.springboot.springboothousemarket.dto.*;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

@Service
public class HousesServiceImpl extends ServiceImpl<HousesMapper, Houses> implements HousesService {

    private final HouseImageService houseImageService;
    private final ObjectMapper objectMapper;

    public HousesServiceImpl(HouseImageService houseImageService, ObjectMapper objectMapper) {
        this.houseImageService = houseImageService;
        this.objectMapper = objectMapper;
    }

    @Override
    @Transactional
    @CacheEvict(cacheNames = {"houses:list", "home:stats"}, allEntries = true)
    public HouseDetailVO createHouse(HouseCreateDTO dto, Users currentUser) {
        if (currentUser == null) {
            throw new SecurityException("请先登录");
        }

        Long landlordId = dto.getLandlordId();
        if (landlordId == null) {
            landlordId = currentUser.getId();
        }
        if (!"ADMIN".equals(currentUser.getRole()) && !landlordId.equals(currentUser.getId())) {
            throw new SecurityException("没有权限为其他房东创建房源");
        }

        Houses house = new Houses();
        copyToEntity(dto, house);
        house.setLandlordId(landlordId);
        house.setStatus(normalizeStatus(dto.getStatus()));
        house.setViews(0);
        house.setIsDeleted(0);
        house.setImage("[]");
        house.setCreateTime(LocalDateTime.now());
        house.setUpdateTime(LocalDateTime.now());
        applyDefaults(house);
        save(house);

        createImagesFromUrls(house.getId(), dto.getImageUrls());
        return toDetailVO(house, houseImageService.listByHouseId(house.getId()));
    }

    @Override
    public HouseDetailVO getHouseDetailVO(Long id) {
        Houses house = getById(id);
        if (house == null || Integer.valueOf(1).equals(house.getIsDeleted())) {
            return null;
        }
        return toDetailVO(house, houseImageService.listByHouseId(id));
    }

    @Override
    public Houses getHouseById(Long id) {
        Houses house = getById(id);
        return house != null && Integer.valueOf(1).equals(house.getIsDeleted()) ? null : house;
    }

    @Override
    @CacheEvict(cacheNames = "houses:detail", key = "#id")
    public void incrementViews(Long id) {
        this.lambdaUpdate()
                .eq(Houses::getId, id)
                .setSql("views = views + 1")
                .update();
    }

    @Override
    @Transactional
    @CacheEvict(cacheNames = {"houses:list", "home:stats", "houses:detail"}, key = "#id", allEntries = true)
    public HouseDetailVO updateHouse(Long id, HouseUpdateDTO dto, Users currentUser) {
        Houses dbHouse = getById(id);
        if (dbHouse == null || Integer.valueOf(1).equals(dbHouse.getIsDeleted())) {
            throw new RuntimeException("房源不存在");
        }
        assertCanManage(dbHouse, currentUser);

        Houses house = dbHouse;
        copyToEntity(dto, house);
        house.setId(id);
        house.setLandlordId(resolveLandlordId(dbHouse, dto.getLandlordId(), currentUser));
        house.setStatus(dto.getStatus() == null ? dbHouse.getStatus() : normalizeStatus(dto.getStatus()));
        house.setViews(dbHouse.getViews());
        house.setIsDeleted(0);
        house.setImage("[]");
        house.setCreateTime(dbHouse.getCreateTime());
        house.setUpdateTime(LocalDateTime.now());
        updateById(house);

        if (dto.getImageUrls() != null) {
            houseImageService.deleteByHouseId(id);
            createImagesFromUrls(id, dto.getImageUrls());
        }
        return toDetailVO(house, houseImageService.listByHouseId(id));
    }

    @Override
    @Transactional
    @CacheEvict(cacheNames = {"houses:list", "home:stats", "houses:detail"}, key = "#id", allEntries = true)
    public boolean deleteHouse(Long id, Users currentUser) {
        Houses dbHouse = getById(id);
        if (dbHouse == null || Integer.valueOf(1).equals(dbHouse.getIsDeleted())) {
            throw new RuntimeException("房源不存在");
        }
        assertCanManage(dbHouse, currentUser);
        houseImageService.deleteByHouseId(id);
        dbHouse.setIsDeleted(1);
        dbHouse.setUpdateTime(LocalDateTime.now());
        return updateById(dbHouse);
    }

    @Override
    public List<HouseListVO> getHousesByLandlordVO(Long landlordId) {
        List<Houses> houses = list(new QueryWrapper<Houses>()
                .eq("landlord_id", landlordId)
                .eq("is_deleted", 0)
                .orderByDesc("create_time"));
        return toListVO(houses);
    }

    @Override
    public Page<HouseListVO> getHouseListVO(String keyword, String type, String district, Double minArea, Double maxArea,
                                            Double minPrice, Double maxPrice, String address, String status, int page, int pageSize) {
        QueryWrapper<Houses> query = new QueryWrapper<>();
        query.eq("is_deleted", 0);
        if (status != null && !status.isBlank()) {
            query.eq("status", status);
        }
        if (keyword != null && !keyword.isBlank()) {
            query.and(wrapper -> wrapper
                    .like("title", keyword)
                    .or()
                    .like("address", keyword)
                    .or()
                    .like("district", keyword));
        }
        if (type != null && !type.isBlank()) {
            query.eq("type", type);
        }
        if (district != null && !district.isBlank()) {
            query.like("district", district);
        }
        if (minArea != null) {
            query.ge("area", minArea);
        }
        if (maxArea != null) {
            query.le("area", maxArea);
        }
        if (minPrice != null) {
            query.ge("price", minPrice);
        }
        if (maxPrice != null) {
            query.le("price", maxPrice);
        }
        if (address != null && !address.isBlank()) {
            query.like("address", address);
        }
        query.orderByDesc("create_time");

        Page<Houses> entityPage = page(new Page<>(page, pageSize), query);
        Page<HouseListVO> voPage = new Page<>(entityPage.getCurrent(), entityPage.getSize(), entityPage.getTotal());
        voPage.setRecords(toListVO(entityPage.getRecords()));
        return voPage;
    }

    private List<HouseListVO> toListVO(List<Houses> houses) {
        if (houses == null || houses.isEmpty()) {
            return new ArrayList<>();
        }
        Map<Long, List<HouseImage>> imageMap = houseImageService.listByHouseIds(
                houses.stream().map(Houses::getId).filter(Objects::nonNull).collect(Collectors.toSet()));
        return houses.stream()
                .map(house -> toListVO(house, imageMap.getOrDefault(house.getId(), Collections.emptyList())))
                .collect(Collectors.toList());
    }

    private HouseListVO toListVO(Houses house, List<HouseImage> images) {
        HouseListVO vo = new HouseListVO();
        copyBase(house, vo);
        vo.setCoverImage(findCover(images));
        return vo;
    }

    private HouseDetailVO toDetailVO(Houses house, List<HouseImage> images) {
        HouseDetailVO vo = new HouseDetailVO();
        copyBase(house, vo);
        vo.setDescription(house.getDescription());
        vo.setImages(images.stream()
                .sorted(Comparator.comparing(HouseImage::getSortOrder, Comparator.nullsLast(Integer::compareTo))
                        .thenComparing(HouseImage::getId))
                .map(this::toImageVO)
                .collect(Collectors.toList()));
        vo.setCoverImage(findCover(images));
        return vo;
    }

    private void copyBase(Houses house, HouseListVO vo) {
        vo.setId(house.getId());
        vo.setTitle(house.getTitle());
        vo.setType(house.getType());
        vo.setDistrict(house.getDistrict());
        vo.setBedrooms(house.getBedrooms());
        vo.setBathrooms(house.getBathrooms());
        vo.setArea(house.getArea());
        vo.setPrice(house.getPrice());
        vo.setOrientation(house.getOrientation());
        vo.setFloor(house.getFloor());
        vo.setDecoration(house.getDecoration());
        vo.setLeaseTerm(house.getLeaseTerm());
        vo.setTags(parseStringList(house.getTags()));
        vo.setAddress(house.getAddress());
        vo.setLandlordId(house.getLandlordId());
        vo.setStatus(house.getStatus());
        vo.setViews(house.getViews());
        vo.setCreateTime(house.getCreateTime());
        vo.setUpdateTime(house.getUpdateTime());
    }

    private HouseImageVO toImageVO(HouseImage image) {
        HouseImageVO vo = new HouseImageVO();
        vo.setId(image.getId());
        vo.setHouseId(image.getHouseId());
        vo.setImageUrl(image.getImageUrl());
        vo.setSortOrder(image.getSortOrder());
        vo.setIsCover(image.getIsCover());
        vo.setCreateTime(image.getCreateTime());
        return vo;
    }

    private String findCover(List<HouseImage> images) {
        if (images == null || images.isEmpty()) {
            return null;
        }
        return images.stream()
                .filter(image -> Integer.valueOf(1).equals(image.getIsCover()))
                .min(Comparator.comparing(HouseImage::getSortOrder, Comparator.nullsLast(Integer::compareTo))
                        .thenComparing(HouseImage::getId))
                .orElse(images.get(0))
                .getImageUrl();
    }

    private List<String> parseStringList(String value) {
        if (value == null || value.isBlank()) {
            return new ArrayList<>();
        }
        try {
            return objectMapper.readValue(value, new TypeReference<>() {
            });
        } catch (Exception ignored) {
            return new ArrayList<>();
        }
    }

    private void copyToEntity(HouseCreateDTO dto, Houses house) {
        house.setTitle(dto.getTitle());
        house.setType(dto.getType());
        house.setDistrict(dto.getDistrict());
        house.setBedrooms(dto.getBedrooms());
        house.setBathrooms(dto.getBathrooms());
        house.setArea(dto.getArea());
        house.setPrice(dto.getPrice());
        house.setOrientation(dto.getOrientation());
        house.setFloor(dto.getFloor());
        house.setDecoration(dto.getDecoration());
        house.setLeaseTerm(dto.getLeaseTerm());
        house.setTags(normalizeTags(dto.getTags()));
        house.setAddress(dto.getAddress());
        house.setDescription(dto.getDescription());
    }

    private void copyToEntity(HouseUpdateDTO dto, Houses house) {
        if (dto.getTitle() != null) house.setTitle(dto.getTitle());
        if (dto.getType() != null) house.setType(dto.getType());
        if (dto.getDistrict() != null) house.setDistrict(dto.getDistrict());
        if (dto.getBedrooms() != null) house.setBedrooms(dto.getBedrooms());
        if (dto.getBathrooms() != null) house.setBathrooms(dto.getBathrooms());
        if (dto.getArea() != null) house.setArea(dto.getArea());
        if (dto.getPrice() != null) house.setPrice(dto.getPrice());
        if (dto.getOrientation() != null) house.setOrientation(dto.getOrientation());
        if (dto.getFloor() != null) house.setFloor(dto.getFloor());
        if (dto.getDecoration() != null) house.setDecoration(dto.getDecoration());
        if (dto.getLeaseTerm() != null) house.setLeaseTerm(dto.getLeaseTerm());
        if (dto.getTags() != null) house.setTags(normalizeTags(dto.getTags()));
        if (dto.getAddress() != null) house.setAddress(dto.getAddress());
        if (dto.getDescription() != null) house.setDescription(dto.getDescription());
    }

    private String normalizeTags(String tags) {
        if (tags == null || tags.isBlank()) {
            return "[]";
        }
        String trimmed = tags.trim();
        if (trimmed.startsWith("[")) {
            return trimmed;
        }
        List<String> values = List.of(trimmed.split(","));
        try {
            return objectMapper.writeValueAsString(values.stream()
                    .map(String::trim)
                    .filter(value -> !value.isEmpty())
                    .collect(Collectors.toList()));
        } catch (Exception ignored) {
            return "[]";
        }
    }

    private void applyDefaults(Houses house) {
        if (house.getTitle() == null || house.getTitle().isBlank()) {
            house.setTitle("未命名房源");
        }
        if (house.getType() == null || house.getType().isBlank()) {
            house.setType("平层");
        }
        if (house.getDistrict() == null || house.getDistrict().isBlank()) {
            house.setDistrict("未知区域");
        }
        if (house.getBedrooms() == null) {
            house.setBedrooms(1);
        }
        if (house.getBathrooms() == null) {
            house.setBathrooms(1);
        }
        if (house.getArea() == null) {
            house.setArea(BigDecimal.ZERO);
        }
        if (house.getPrice() == null) {
            house.setPrice(BigDecimal.ZERO);
        }
        if (house.getOrientation() == null || house.getOrientation().isBlank()) {
            house.setOrientation("南北");
        }
        if (house.getDecoration() == null || house.getDecoration().isBlank()) {
            house.setDecoration("精装");
        }
        if (house.getLeaseTerm() == null || house.getLeaseTerm().isBlank()) {
            house.setLeaseTerm("押一付三");
        }
        if (house.getTags() == null || house.getTags().isBlank()) {
            house.setTags("[]");
        }
        if (house.getAddress() == null || house.getAddress().isBlank()) {
            house.setAddress("未知地址");
        }
        if (house.getDescription() == null || house.getDescription().isBlank()) {
            house.setDescription("暂无描述");
        }
        if (house.getImage() == null || house.getImage().isBlank()) {
            house.setImage("[]");
        }
    }

    private void createImagesFromUrls(Long houseId, List<String> imageUrls) {
        if (imageUrls == null || imageUrls.isEmpty()) {
            return;
        }
        for (int i = 0; i < imageUrls.size(); i++) {
            String url = imageUrls.get(i);
            if (url != null && !url.isBlank()) {
                houseImageService.createImage(houseId, url, i, i == 0);
            }
        }
    }

    private String normalizeStatus(String status) {
        return status == null || status.isBlank() ? "NORMAL" : status;
    }

    private Long resolveLandlordId(Houses dbHouse, Long requestedLandlordId, Users currentUser) {
        if (requestedLandlordId != null) {
            if (!"ADMIN".equals(currentUser.getRole()) && !requestedLandlordId.equals(currentUser.getId())) {
                throw new SecurityException("没有权限变更房源归属");
            }
            return requestedLandlordId;
        }
        return "ADMIN".equals(currentUser.getRole()) ? dbHouse.getLandlordId() : currentUser.getId();
    }

    private void assertCanManage(Houses house, Users currentUser) {
        if (currentUser == null) {
            throw new SecurityException("请先登录");
        }
        if (!"ADMIN".equals(currentUser.getRole()) && !house.getLandlordId().equals(currentUser.getId())) {
            throw new SecurityException("没有权限操作此房源");
        }
    }
}
