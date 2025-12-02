package com.springboot.springboothousemarket.Service;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.IService;
import com.springboot.springboothousemarket.Entity.House;

import java.util.List;

public interface HouseService extends IService<House> {

    /**
     * 创建房源信息
     *
     * @param house 房源信息
     * @return 创建结果
     */
    House createHouse(House house);

    /**
     * 根据ID获取房源详情
     *
     * @param id 房源ID
     * @return 房源信息
     */
    House getHouseById(Long id);

    /**
     * 更新房源信息
     *
     * @param id    房源ID
     * @param house 更新的房源信息
     * @return 更新结果
     */
    House updateHouse(Long id, House house);

    /**
     * 删除房源信息
     *
     * @param id 房源ID
     * @return 删除结果
     */
    boolean deleteHouse(Long id);

    /**
     * 获取所有房源列表
     *
     * @return 房源列表
     */
    List<House> getAllHouses();

    /**
     * 根据房东ID获取房源列表
     *
     * @param landlordId 房东ID
     * @return 房源列表
     */
    List<House> getHousesByLandlordId(Long landlordId);

    /**
     * 分页获取房源列表（带条件查询）
     *
     * @param keyword  关键词
     * @param type     户型
     * @param minArea  最小面积
     * @param maxArea  最大面积
     * @param minPrice 最低价格
     * @param maxPrice 最高价格
     * @param address  地址
     * @param page     页码
     * @param pageSize 每页数量
     * @return 房源分页信息
     */
    Page<House> getHouses(String keyword, String type, Double minArea, Double maxArea,
                          Double minPrice, Double maxPrice, String address, int page, int pageSize);
}