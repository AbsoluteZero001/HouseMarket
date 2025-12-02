package com.springboot.springboothousemarket.Service;

import com.springboot.springboothousemarket.Entitiy.HouseOrder;
import com.springboot.springboothousemarket.Mapper.HouseOrderMapper;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class HouseOrderServiceImpl implements HouseOrderService {

    private final HouseOrderMapper houseOrderMapper;

    public HouseOrderServiceImpl(HouseOrderMapper houseOrderMapper) {
        this.houseOrderMapper = houseOrderMapper;
    } //构造函数注入

    @Override
    public HouseOrder createOrder(HouseOrder houseOrder) {
        houseOrder.setStatus(0); // 0-待房东确认
        houseOrderMapper.insert(houseOrder);
        return houseOrder;
    }

    @Override
    public HouseOrder getOrderById(Long id) {
        return houseOrderMapper.selectById(id);
    }

    @Override
    public HouseOrder updateOrder(Long id, HouseOrder houseOrder) {
        houseOrder.setId(id);
        houseOrderMapper.update(houseOrder);
        return houseOrder;
    }

    @Override
    public boolean deleteOrder(Long id) {
        return houseOrderMapper.deleteById(id) > 0;
    }

    @Override
    public List<HouseOrder> getAllOrders() {
        return houseOrderMapper.selectAll();
    }

    @Override
    public List<HouseOrder> getOrdersByUserId(Long userId) {
        return houseOrderMapper.selectByUserId(userId);
    }

    @Override
    public List<HouseOrder> getOrdersByUserIdAndStatus(Long userId, String status) {
        return houseOrderMapper.selectByUserIdAndStatus(userId, status);
    }

    @Override
    public boolean updateOrderStatus(Long id, int status) {
        HouseOrder order = houseOrderMapper.selectById(id);
        if (order != null) {
            order.setStatus(status);
            return houseOrderMapper.update(order) > 0;
        }
        return false;
    }
}