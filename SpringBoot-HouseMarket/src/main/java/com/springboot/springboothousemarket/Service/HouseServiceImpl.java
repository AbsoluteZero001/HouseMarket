package com.springboot.springboothousemarket.Service;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.springboot.springboothousemarket.Entity.House;
import com.springboot.springboothousemarket.Mapper.HouseMapper;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class HouseServiceImpl extends ServiceImpl<HouseMapper, House> implements HouseService {

    private final HouseMapper houseMapper;
    public HouseServiceImpl(HouseMapper houseMapper) {
        this.houseMapper = houseMapper;
    }

    @Override
    public House createHouse(House house) {
        house.setIsDeleted(0); // 默认未删除
        houseMapper.insert(house);
        return house;
    }

    @Override
    public House getHouseById(Long id) {
        return houseMapper.selectById(id);
    }

    @Override
    public House updateHouse(Long id, House house) {
        house.setId(id);
        houseMapper.updateById(house);
        return house;
    }

    @Override
    public boolean deleteHouse(Long id) {
        return houseMapper.deleteById(id) > 0;
    }

    @Override
    public List<House> getAllHouses() {
        QueryWrapper<House> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("is_deleted", 0);
        return houseMapper.selectList(queryWrapper);
    }

    @Override
    public List<House> getHousesByLandlordId(Long landlordId) {
        QueryWrapper<House> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("landlord_id", landlordId)
                .eq("is_deleted", 0);
        return houseMapper.selectList(queryWrapper);
    }

    @Override
    public Page<House> getHouses(String keyword, String type, Double minArea, Double maxArea,
                                 Double minPrice, Double maxPrice, String address, int page, int pageSize) {
        Page<House> pageObj = new Page<>(page, pageSize);
        List<House> houses = houseMapper.selectByConditions(keyword, type, minArea, maxArea, minPrice, maxPrice, address);
        pageObj.setRecords(houses);
        return pageObj;
    }
}