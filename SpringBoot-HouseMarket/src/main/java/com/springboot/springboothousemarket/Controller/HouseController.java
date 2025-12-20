package com.springboot.springboothousemarket.Controller; // 包声明，表示这是一个控制器类

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.springboot.springboothousemarket.Entity.House;
import com.springboot.springboothousemarket.Service.HouseService;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;


/**
 * 房源控制器类
 * 提供房源信息的CRUD操作和相关查询功能
 */
@Tag(name = "房源信息API") // Swagger API标签，标识这是一个房源信息相关的API
@RequestMapping("/api/houses") // 请求映射，定义基础URL路径
@RestController // 声明这是一个RESTful控制器
public class HouseController { // 房源控制器类定义

    private final HouseService houseService; // 房源服务接口，用于处理业务逻辑

    /**
     * 构造函数，通过依赖注入方式获取HouseService实例
     *
     * @param houseService 房源服务接口实现
     */
    public HouseController(HouseService houseService) {
        this.houseService = houseService;
    }

    /**
     * 创建房源信息
     * @param house 房源信息
     * @return 创建结果
     */
    @PostMapping // POST请求映射，处理创建资源的请求
    public House createHouse(@RequestBody House house) { // 接收请求体中的房源信息
        return houseService.createHouse(house); // 调用服务层方法创建房源
    }

    /**
     * 根据ID获取房源详情
     * @param id 房源ID
     * @return 房源信息
     */
    /**
     * 根据房屋ID获取房屋信息接口
     *
     * @param id 房屋ID，通过路径变量传递
     * @return 返回包含房屋信息的响应对象，包含success状态码和data数据
     */
    @GetMapping("/{id}") // GET请求映射，处理获取单个资源的请求
    public Map<String, Object> getHouseById(@PathVariable Long id) { // 从路径中获取房源ID
        // 调用服务层方法根据ID获取房屋实体对象
        House house = houseService.getHouseById(id);

        // 创建数据容器，用于存放房屋实体信息
        Map<String, Object> data = new HashMap<>();
        data.put("house", house);

        // 创建响应对象，包含请求成功状态和数据
        Map<String, Object> response = new HashMap<>();
        response.put("success", true);
        response.put("data", data);

        // 返回完整的响应信息
        return response;
    }

    /**
     * 更新房源信息
     * @param id 房源ID
     * @param house 更新的房源信息
     * @return 更新结果
     */
    @PutMapping("/{id}") // PUT请求映射，处理更新资源的请求
    public House updateHouse(@PathVariable Long id, @RequestBody House house) { // 从路径获取ID，从请求体获取更新数据
        return houseService.updateHouse(id, house); // 调用服务层方法更新房源
    }

    /**
     * 删除房源信息
     * @param id 房源ID
     * @return 删除结果
     */
    @DeleteMapping("/{id}") // DELETE请求映射，处理删除资源的请求
    public boolean deleteHouse(@PathVariable Long id) { // 从路径中获取房源ID
        return houseService.deleteHouse(id); // 调用服务层方法删除房源
    }

    /**
     * 获取所有房源列表（带分页和条件查询）
     * @param keyword 关键词
     * @param type 户型
     * @param minArea 最小面积
     * @param maxArea 最大面积
     * @param minPrice 最低价格
     * @param maxPrice 最高价格
     * @param address 地址
     * @param page 页码
     * @param pageSize 每页数量
     * @return 房源列表
     */
    @GetMapping // GET请求映射，处理获取资源列表的请求
    public Map<String, Object> getHouses( // 获取房源列表方法
                                          @RequestParam(required = false) String keyword, // 可选查询参数：关键词
                                          @RequestParam(required = false) String type, // 可选查询参数：户型
                                          @RequestParam(required = false) Double minArea, // 可选查询参数：最小面积
                                          @RequestParam(required = false) Double maxArea, // 可选查询参数：最大面积
                                          @RequestParam(required = false) Double minPrice, // 可选查询参数：最低价格
                                          @RequestParam(required = false) Double maxPrice, // 可选查询参数：最高价格
                                          @RequestParam(required = false) String address, // 可选查询参数：地址
                                          @RequestParam(defaultValue = "1") int page, // 可选查询参数：页码，默认为1
                                          @RequestParam(defaultValue = "10") int pageSize) { // 可选查询参数：每页数量，默认为10

        Page<House> pageInfo = houseService.getHouses(keyword, type, minArea, maxArea, minPrice, maxPrice, address, page, pageSize); // 调用服务层方法获取分页房源信息

        Map<String, Object> data = new HashMap<>(); // 创建数据容器
        data.put("houses", pageInfo.getRecords()); // 存放房源列表
        data.put("total", pageInfo.getTotal()); // 存放总记录数
        data.put("page", pageInfo.getCurrent()); // 存放当前页码
        data.put("pageSize", pageInfo.getSize()); // 存放每页数量

        Map<String, Object> response = new HashMap<>(); // 创建响应对象
        response.put("success", true); // 设置成功状态
        response.put("data", data); // 设置返回数据

        return response; // 返回响应结果
    }

    /**
     * 根据房东ID获取房源列表
     * @param landlordId 房东ID
     * @return 房源列表
     */
    @GetMapping("/landlord/{landlordId}") // GET请求映射，处理获取特定房东房源列表的请求
    public List<House> getHousesByLandlordId(@PathVariable Long landlordId) { // 从路径中获取房东ID
        return houseService.getHousesByLandlordId(landlordId); // 调用服务层方法获取指定房东的房源列表
    }
}