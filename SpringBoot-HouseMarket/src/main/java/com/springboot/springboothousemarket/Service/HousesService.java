package com.springboot.springboothousemarket.Service;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.IService;
import com.springboot.springboothousemarket.Entity.Houses;
import com.springboot.springboothousemarket.Entity.Users;

import java.util.List;

public interface HousesService extends IService<Houses> {

    // 创建房源（带房东ID）
    Houses createHouse(Houses house, Long landlordId);

    // 根据ID获取房源详情
    Houses getHouseById(Long id);

    void incrementViews(Long id);

    // 更新房源（带当前用户ID权限校验）
    Houses updateHouse(Long id, Houses house, Users currentUser);

    // 删除房源（带当前用户ID权限校验）
    boolean deleteHouse(Long id, Users currentUser);

    // 根据房东ID获取房源列表
    List<Houses> getHousesByLandlordId(Long landlordId);

    // 分页获取房源列表（带条件查询）
    Page<Houses> getHouses(String keyword, String type, String district, Double minArea, Double maxArea,
                           Double minPrice, Double maxPrice, String address, String status, int page, int pageSize);
}
