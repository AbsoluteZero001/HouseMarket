package com.springboot.springboothousemarket.Service;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.springboot.springboothousemarket.Entity.Houses;
import com.springboot.springboothousemarket.Mapper.HousesMapper;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

@Service
public class HousesServiceImpl extends ServiceImpl<HousesMapper, Houses> implements HousesService {

    @Override
    public Houses createHouse(Houses house, Long landlordId) {
        house.setLandlordId(landlordId); // 绑定房东ID
        // 设置默认状态，根据数据库约束修改为正常状态
        house.setStatus("NORMAL");
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
    public Houses getHouseById(Long id) {
        return getById(id);
    }

    @Override
    public Houses updateHouse(Long id, Houses house, Long currentUserId) {
        Houses dbHouse = getById(id);
        if (dbHouse == null) {
            throw new RuntimeException("房源不存在");
        }
        if (!dbHouse.getLandlordId().equals(currentUserId)) {
            throw new SecurityException("没有权限修改此房源");
        }

        house.setId(id);
        house.setLandlordId(currentUserId); // 保证房东ID不被篡改
        updateById(house);
        return house;
    }

    @Override
    public boolean deleteHouse(Long id, Long currentUserId) {
        Houses dbHouse = getById(id);
        if (dbHouse == null) {
            throw new RuntimeException("房源不存在");
        }
        if (!dbHouse.getLandlordId().equals(currentUserId)) {
            throw new SecurityException("没有权限删除此房源");
        }
        return removeById(id);
    }

    @Override
    public List<Houses> getAllHouses() {
        return list();
    }

    @Override
    public List<Houses> getHousesByLandlordId(Long landlordId) {
        return list(new QueryWrapper<Houses>().eq("landlord_id", landlordId));
    }

    @Override
    public Page<Houses> getHouses(String keyword, String type, Double minArea, Double maxArea,
                                  Double minPrice, Double maxPrice, String address, int page, int pageSize) {

        QueryWrapper<Houses> query = new QueryWrapper<>();
        if (keyword != null && !keyword.isEmpty())
            query.like("title", keyword);
        if (type != null && !type.isEmpty())
            query.eq("type", type);
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

        return page(new Page<>(page, pageSize), query);
    }
}
