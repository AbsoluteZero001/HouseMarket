package com.springboot.springboothousemarket.Controller;

import com.springboot.springboothousemarket.Entitiy.HouseOrder;
import com.springboot.springboothousemarket.Service.HouseOrderService;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Tag(name = "房屋订单API")
@RequestMapping("/order")
@RestController
public class HouseOrderController {



    private final HouseOrderService houseOrderService;

    public HouseOrderController(HouseOrderService houseOrderService) {
        this.houseOrderService = houseOrderService;
    }






    /**
     * 创建订单
     * @param houseOrder 订单信息
     * @return 创建结果
     */
    @PostMapping
    public HouseOrder createOrder(@RequestBody HouseOrder houseOrder) {
        return houseOrderService.createOrder(houseOrder);
    }

    /**
     * 根据ID获取订单详情
     * @param id 订单ID
     * @return 订单信息
     */
    @GetMapping("/{id}")
    public HouseOrder getOrderById(@PathVariable Long id) {
        return houseOrderService.getOrderById(id);
    }

    /**
     * 更新订单信息
     * @param id 订单ID
     * @param houseOrder 更新的订单信息
     * @return 更新结果
     */
    @PutMapping("/{id}")
    public HouseOrder updateOrder(@PathVariable Long id, @RequestBody HouseOrder houseOrder) {
        return houseOrderService.updateOrder(id, houseOrder);
    }

    /**
     * 删除订单
     * @param id 订单ID
     * @return 删除结果
     */
    @DeleteMapping("/{id}")
    public boolean deleteOrder(@PathVariable Long id) {
        return houseOrderService.deleteOrder(id);
    }

    /**
     * 获取所有订单列表
     * @return 订单列表
     */
    @GetMapping
    public List<HouseOrder> getAllOrders() {
        return houseOrderService.getAllOrders();
    }

    /**
     * 根据用户ID获取订单列表
     * @param userId 用户ID
     * @return 订单列表
     */
    @GetMapping("/user/{userId}")
    public List<HouseOrder> getOrdersByUserId(@PathVariable Long userId) {
        return houseOrderService.getOrdersByUserId(userId);
    }
}