package com.springboot.springboothousemarket.Service;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.springboot.springboothousemarket.Entity.Houses;
import com.springboot.springboothousemarket.Entity.Users;
import com.springboot.springboothousemarket.Mapper.HousesMapper;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.cache.annotation.Caching;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

@Service
public class HousesServiceImpl extends ServiceImpl<HousesMapper, Houses> implements HousesService {

    @Override
    @CacheEvict(cacheNames = {"houses:list", "home:stats"}, allEntries = true)
    public Houses createHouse(Houses house, Long landlordId) {
        house.setLandlordId(landlordId); // 绑定房东ID
        // 设置默认状态，根据数据库约束修改为正常状态
        house.setStatus("NORMAL");
        house.setViews(0);
        // 设置创建和更新时间
        house.setCreateTime(LocalDateTime.now());
        house.setUpdateTime(LocalDateTime.now());
        // 设置未删除状态
        house.setIsDeleted(0);
        // 确保必填字段都有值
        if (house.getTitle() == null || house.getTitle().isEmpty()) {
            house.setTitle("未命名房源");
        }
        if (house.getType() == null || house.getType().isEmpty()) {
            house.setType("平层");
        }
        if (house.getArea() == null) {
            house.setArea(BigDecimal.ZERO);
        }
        if (house.getPrice() == null) {
            house.setPrice(BigDecimal.ZERO);
        }
        if (house.getAddress() == null || house.getAddress().isEmpty()) {
            house.setAddress("未知地址");
        }
        if (house.getDistrict() == null || house.getDistrict().isEmpty()) {
            house.setDistrict("未知区域");
        }
        if (house.getBedrooms() == null) {
            house.setBedrooms(1);
        }
        if (house.getBathrooms() == null) {
            house.setBathrooms(1);
        }
        if (house.getOrientation() == null || house.getOrientation().isEmpty()) {
            house.setOrientation("南北");
        }
        if (house.getDecoration() == null || house.getDecoration().isEmpty()) {
            house.setDecoration("精装");
        }
        if (house.getLeaseTerm() == null || house.getLeaseTerm().isEmpty()) {
            house.setLeaseTerm("押一付三");
        }
        if (house.getTags() == null || house.getTags().isEmpty()) {
            house.setTags("[]");
        }
        if (house.getDescription() == null || house.getDescription().isEmpty()) {
            house.setDescription("暂无描述");
        }
        // 确保图片字段有值，避免检查约束违反
        if (house.getImage() == null || house.getImage().isEmpty()) {
            house.setImage("[]"); // 设置空JSON数组作为默认值
        }
        // 保存房源
        save(house);
        return house;
    }

    @Override
    @Cacheable(cacheNames = "houses:detail", key = "#id")
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
    @Caching(evict = {
            @CacheEvict(cacheNames = "houses:list", allEntries = true),
            @CacheEvict(cacheNames = "home:stats", allEntries = true),
            @CacheEvict(cacheNames = "houses:detail", key = "#id")
    })
    public Houses updateHouse(Long id, Houses house, Users currentUser) {
        Houses dbHouse = getById(id);
        if (dbHouse == null) {
            throw new RuntimeException("房源不存在");
        }
        boolean isOwner = dbHouse.getLandlordId().equals(currentUser.getId());
        if (!isOwner && !"ADMIN".equals(currentUser.getRole())) {
            throw new SecurityException("没有权限修改此房源");
        }

        house.setId(id);
        house.setLandlordId(isOwner ? currentUser.getId() : dbHouse.getLandlordId());
        updateById(house);
        return house;
    }

    @Override
    @Caching(evict = {
            @CacheEvict(cacheNames = "houses:list", allEntries = true),
            @CacheEvict(cacheNames = "home:stats", allEntries = true),
            @CacheEvict(cacheNames = "houses:detail", key = "#id")
    })
    public boolean deleteHouse(Long id, Users currentUser) {
        Houses dbHouse = getById(id);
        if (dbHouse == null) {
            throw new RuntimeException("房源不存在");
        }
        boolean isOwner = dbHouse.getLandlordId().equals(currentUser.getId());
        if (!isOwner && !"ADMIN".equals(currentUser.getRole())) {
            throw new SecurityException("没有权限删除此房源");
        }
        dbHouse.setIsDeleted(1);
        return updateById(dbHouse);
    }

    @Override
    public List<Houses> getHousesByLandlordId(Long landlordId) {
        return list(new QueryWrapper<Houses>()
                .eq("landlord_id", landlordId)
                .eq("is_deleted", 0)
                .orderByDesc("create_time"));
    }

    @Override
    @Cacheable(cacheNames = "houses:list",
            key = "{#keyword,#type,#district,#minArea,#maxArea,#minPrice,#maxPrice,#address,#status,#page,#pageSize}",
            unless = "#result == null || #result.getRecords().isEmpty()")
    public Page<Houses> getHouses(String keyword, String type, String district, Double minArea, Double maxArea,
                                  Double minPrice, Double maxPrice, String address, String status, int page, int pageSize) {

        QueryWrapper<Houses> query = new QueryWrapper<>();
        query.eq("is_deleted", 0);
        if (status != null && !status.isEmpty()) {
            query.eq("status", status);
        }
        if (keyword != null && !keyword.isEmpty())
            query.like("title", keyword);
        if (type != null && !type.isEmpty())
            query.eq("type", type);
        if (district != null && !district.isEmpty())
            query.like("district", district);
        if (minArea != null)
            query.ge("area", minArea);
        if (maxArea != null)
            query.le("area", maxArea);
        if (minPrice != null)
            query.ge("price", minPrice);
        if (maxPrice != null)
            query.le("price", maxPrice);
        if (address != null && !address.isEmpty())
            query.like("address", address);
        query.orderByDesc("create_time");

        return page(new Page<>(page, pageSize), query);
    }
}
