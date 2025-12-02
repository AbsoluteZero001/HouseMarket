package com.springboot.springboothousemarket.Service;

import com.github.pagehelper.PageHelper;
import com.github.pagehelper.PageInfo;
import com.springboot.springboothousemarket.Entitiy.House;
import com.springboot.springboothousemarket.Mapper.HouseMapper;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class HouseServiceImpl implements HouseService {

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
        houseMapper.update(house);
        return house;
    }

    @Override
    public boolean deleteHouse(Long id) {
        return houseMapper.deleteById(id) > 0;
    }

    @Override
    public List<House> getAllHouses() {
        return houseMapper.selectAll();
    }

    @Override
    public List<House> getHousesByLandlordId(Long landlordId) {
        return houseMapper.selectByLandlordId(landlordId);
    }

    @Override
    public PageInfo<House> getHouses(String keyword, String type, Double minArea, Double maxArea,
                                     Double minPrice, Double maxPrice, String address, int page, int pageSize) {
        PageHelper.startPage(page, pageSize);
        List<House> houses = houseMapper.selectByConditions(keyword, type, minArea, maxArea, minPrice, maxPrice, address);
        return new PageInfo<>(houses);
    }
}