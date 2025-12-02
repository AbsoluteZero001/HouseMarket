package com.springboot.springboothousemarket.Controller;

import com.github.pagehelper.PageInfo;
import com.springboot.springboothousemarket.Entitiy.House;
import com.springboot.springboothousemarket.Service.HouseService;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Tag(name = "房源信息API")
@RequestMapping("/house")
@RestController
public class HouseController {

    private final HouseService houseService;

    public HouseController(HouseService houseService) {
        this.houseService = houseService;
    }

    /**
     * 创建房源信息
     * @param house 房源信息
     * @return 创建结果
     */
    @PostMapping
    public House createHouse(@RequestBody House house) {
        return houseService.createHouse(house);
    }


    /**
     * 根据ID获取房源详情
     * @param id 房源ID
     * @return 房源信息
     */
    @GetMapping("/{id}")
    public Map<String, Object> getHouseById(@PathVariable Long id) {
        House house = houseService.getHouseById(id);

        Map<String, Object> data = new HashMap<>();
        data.put("house", house);

        Map<String, Object> response = new HashMap<>();
        response.put("success", true);
        response.put("data", data);

        return response;
    }

    /**
     * 更新房源信息
     * @param id 房源ID
     * @param house 更新的房源信息
     * @return 更新结果
     */
    @PutMapping("/{id}")
    public House updateHouse(@PathVariable Long id, @RequestBody House house) {
        return houseService.updateHouse(id, house);
    }

    /**
     * 删除房源信息
     * @param id 房源ID
     * @return 删除结果
     */
    @DeleteMapping("/{id}")
    public boolean deleteHouse(@PathVariable Long id) {
        return houseService.deleteHouse(id);
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
    @GetMapping("/list")
    public Map<String, Object> getHouses(
            @RequestParam(required = false) String keyword,
            @RequestParam(required = false) String type,
            @RequestParam(required = false) Double minArea,
            @RequestParam(required = false) Double maxArea,
            @RequestParam(required = false) Double minPrice,
            @RequestParam(required = false) Double maxPrice,
            @RequestParam(required = false) String address,
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "10") int pageSize) {

        PageInfo<House> pageInfo = houseService.getHouses(keyword, type, minArea, maxArea, minPrice, maxPrice, address, page, pageSize);

        Map<String, Object> data = new HashMap<>();
        data.put("houses", pageInfo.getList());
        data.put("total", pageInfo.getTotal());
        data.put("page", pageInfo.getPageNum());
        data.put("pageSize", pageInfo.getPageSize());

        Map<String, Object> response = new HashMap<>();
        response.put("success", true);
        response.put("data", data);

        return response;
    }

    /**
     * 根据房东ID获取房源列表
     * @param landlordId 房东ID
     * @return 房源列表
     */
    @GetMapping("/landlord/{landlordId}")
    public List<House> getHousesByLandlordId(@PathVariable Long landlordId) {
        return houseService.getHousesByLandlordId(landlordId);
    }
}