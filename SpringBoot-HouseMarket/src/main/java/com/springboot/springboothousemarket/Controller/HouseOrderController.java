package com.springboot.springboothousemarket.Controller;

import com.springboot.springboothousemarket.Entity.HouseOrder;
import com.springboot.springboothousemarket.Service.HouseOrderService;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Tag(name = "房屋订单API")
@RequestMapping("/api/appointments")
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
    public Map<String, Object> createOrder(@RequestBody HouseOrder houseOrder) {
        HouseOrder order = houseOrderService.createOrder(houseOrder);

        Map<String, Object> data = new HashMap<>();
        data.put("id", order.getId());

        Map<String, Object> response = new HashMap<>();
        response.put("success", true);
        response.put("message", "预约提交成功，请等待房东确认");
        response.put("data", data);

        return response;
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
    public Map<String, Object> deleteOrder(@PathVariable Long id) {
        boolean result = houseOrderService.deleteOrder(id);

        Map<String, Object> response = new HashMap<>();
        if (result) {
            response.put("success", true);
            response.put("message", "预约已删除");
        } else {
            response.put("success", false);
            response.put("message", "删除失败");
        }

        return response;
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
     * @param status 订单状态
     * @return 订单列表
     */
    @GetMapping("/user/{userId}")
    public Map<String, Object> getOrdersByUserId(
            @PathVariable Long userId,
            @RequestParam(required = false) String status) {
        List<HouseOrder> orders = houseOrderService.getOrdersByUserIdAndStatus(userId, status);

        Map<String, Object> data = new HashMap<>();
        data.put("appointments", orders);

        Map<String, Object> response = new HashMap<>();
        response.put("success", true);
        response.put("data", data);

        return response;
    }

    /**
     * 批准预约
     *
     * @param id 订单ID
     * @return 操作结果
     */
    @PutMapping("/{id}/approve")
    public Map<String, Object> approveOrder(@PathVariable Long id) {
        boolean result = houseOrderService.updateOrderStatus(id, 1); // 1-已批准

        Map<String, Object> response = new HashMap<>();
        if (result) {
            response.put("success", true);
            response.put("message", "预约已批准");
        } else {
            response.put("success", false);
            response.put("message", "操作失败");
        }

        return response;
    }

    /**
     * 拒绝预约
     *
     * @param id 订单ID
     * @return 操作结果
     */
    @PutMapping("/{id}/reject")
    public Map<String, Object> rejectOrder(@PathVariable Long id) {
        boolean result = houseOrderService.updateOrderStatus(id, 3); // 3-已拒绝

        Map<String, Object> response = new HashMap<>();
        if (result) {
            response.put("success", true);
            response.put("message", "预约已拒绝");
        } else {
            response.put("success", false);
            response.put("message", "操作失败");
        }

        return response;
    }

    /**
     * 取消预约
     *
     * @param id 订单ID
     * @return 操作结果
     */
    @PutMapping("/{id}/cancel")
    public Map<String, Object> cancelOrder(@PathVariable Long id) {
        boolean result = houseOrderService.updateOrderStatus(id, 4); // 4-已取消

        Map<String, Object> response = new HashMap<>();
        if (result) {
            response.put("success", true);
            response.put("message", "预约已取消");
        } else {
            response.put("success", false);
            response.put("message", "操作失败");
        }

        return response;
    }
}