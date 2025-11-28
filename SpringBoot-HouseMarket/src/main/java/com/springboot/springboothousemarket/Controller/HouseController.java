package com.springboot.springboothousemarket.Controller;

import com.springboot.springboothousemarket.Entitiy.House;
import com.springboot.springboothousemarket.Service.HouseService;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Tag(name = "房源信息API")
@RequestMapping("/house")
@RestController
public class HouseController {

    @Autowired
    private HouseService houseService;

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
    public House getHouseById(@PathVariable Long id) {
        return houseService.getHouseById(id);
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
     * 获取所有房源列表
     * @return 房源列表
     */
    @GetMapping
    public List<House> getAllHouses() {
        return houseService.getAllHouses();
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