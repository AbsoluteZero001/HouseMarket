package com.springboot.springboothousemarket.Service;

import com.springboot.springboothousemarket.Entitiy.HouseOrder;

import java.util.List;

public interface HouseOrderService {

    /**
     * 创建订单
     *
     * @param houseOrder 订单信息
     * @return 创建结果
     */
    HouseOrder createOrder(HouseOrder houseOrder);

    /**
     * 根据ID获取订单详情
     *
     * @param id 订单ID
     * @return 订单信息
     */
    HouseOrder getOrderById(Long id);

    /**
     * 更新订单信息
     *
     * @param id         订单ID
     * @param houseOrder 更新的订单信息
     * @return 更新结果
     */
    HouseOrder updateOrder(Long id, HouseOrder houseOrder);

    /**
     * 删除订单
     *
     * @param id 订单ID
     * @return 删除结果
     */
    boolean deleteOrder(Long id);

    /**
     * 获取所有订单列表
     *
     * @return 订单列表
     */
    List<HouseOrder> getAllOrders();

    /**
     * 根据用户ID获取订单列表
     *
     * @param userId 用户ID
     * @return 订单列表
     */
    List<HouseOrder> getOrdersByUserId(Long userId);

    /**
     * 根据用户ID和状态获取订单列表
     *
     * @param userId 用户ID
     * @param status 订单状态
     * @return 订单列表
     */
    List<HouseOrder> getOrdersByUserIdAndStatus(Long userId, String status);

    /**
     * 更新订单状态
     *
     * @param id     订单ID
     * @param status 新的状态
     * @return 更新结果
     */
    boolean updateOrderStatus(Long id, int status);
}