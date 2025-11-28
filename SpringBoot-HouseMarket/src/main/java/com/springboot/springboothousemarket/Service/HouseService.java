package com.springboot.springboothousemarket.Service;

import com.springboot.springboothousemarket.Entitiy.House;

import java.util.List;

public interface HouseService {

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
}