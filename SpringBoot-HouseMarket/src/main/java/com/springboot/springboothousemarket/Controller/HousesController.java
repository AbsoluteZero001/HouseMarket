package com.springboot.springboothousemarket.Controller;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.springboot.springboothousemarket.Entity.Houses;
import com.springboot.springboothousemarket.Entity.Users;
import com.springboot.springboothousemarket.Service.HousesService;
import com.springboot.springboothousemarket.Service.UsersService;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Tag(name = "房源信息API")
@RestController
@RequestMapping("/api/houses")
public class HousesController {

    private final HousesService houseService;
    private final UsersService sysUserService;

    public HousesController(HousesService houseService, UsersService sysUserService) {
        this.houseService = houseService;
        this.sysUserService = sysUserService;
    }

    /**
     * 根据房东ID获取房源列表
     * 为前端landlord.html提供兼容接口
     */
    @GetMapping("/landlord/{landlordId}")
    public List<Houses> getHousesByLandlordId(@PathVariable Long landlordId) {
        return houseService.getHousesByLandlordId(landlordId);
    }

    /**
     * 新增房源
     * 这里不直接设置 landlordId，由 service 层根据当前用户处理关联
     */
    @PostMapping
    public Houses createHouse(@RequestBody Houses house) {
        Long userId = getCurrentUserId();
        if (userId == null) {
            throw new SecurityException("未登录或登录状态失效");
        }

        house.setId(null);  // 防止前端传 id
        // 如果需要记录房东信息，请在 houseService.createHouse 内处理
        return houseService.createHouse(house, userId);
    }

    /**
     * 根据 ID 获取房源详情
     */
    @GetMapping("/{id}")
    public Map<String, Object> getHouseById(@PathVariable Long id) {
        Houses house = houseService.getHouseById(id);
        if (house == null) {
            throw new RuntimeException("房源不存在");
        }

        Map<String, Object> data = new HashMap<>();
        data.put("house", house);

        Map<String, Object> response = new HashMap<>();
        response.put("success", true);
        response.put("data", data);
        return response;
    }

    /**
     * 更新房源（只能更新自己的）
     */
    @PutMapping("/{id}")
    public Houses updateHouse(@PathVariable Long id, @RequestBody Houses house) {
        Long currentUserId = getCurrentUserId();
        if (currentUserId == null) {
            throw new SecurityException("未登录或登录状态失效");
        }

        Houses dbHouse = houseService.getHouseById(id);
        if (dbHouse == null) {
            throw new RuntimeException("房源不存在");
        }

        // 权限校验交给 service 层，根据 userId 验证是否可修改
        return houseService.updateHouse(id, house, currentUserId);
    }

    /**
     * 删除房源（逻辑删除，且只能删除自己的）
     */
    @DeleteMapping("/{id}")
    public boolean deleteHouse(@PathVariable Long id) {
        Long currentUserId = getCurrentUserId();
        if (currentUserId == null) {
            throw new SecurityException("未登录或登录状态失效");
        }

        Houses dbHouse = houseService.getHouseById(id);
        if (dbHouse == null) {
            throw new RuntimeException("房源不存在");
        }

        // 权限校验交给 service 层
        return houseService.deleteHouse(id, currentUserId);
    }

    /**
     * 房源分页 + 条件查询
     */
    @GetMapping
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

        Page<Houses> pageInfo = houseService.getHouses(
                keyword, type, minArea, maxArea,
                minPrice, maxPrice, address, page, pageSize
        );

        Map<String, Object> data = new HashMap<>();
        data.put("houses", pageInfo.getRecords());
        data.put("total", pageInfo.getTotal());
        data.put("page", pageInfo.getCurrent());
        data.put("pageSize", pageInfo.getSize());

        Map<String, Object> response = new HashMap<>();
        response.put("success", true);
        response.put("data", data);
        return response;
    }

    /**
     * 获取当前房东的所有房源
     */
    @GetMapping("/my")
    public List<Houses> getMyHouses() {
        Long currentUserId = getCurrentUserId();
        if (currentUserId == null) {
            throw new SecurityException("未登录或登录状态失效");
        }

        // 由 service 层根据 userId 查询
        return houseService.getHousesByLandlordId(currentUserId);
    }

    /**
     * 获取当前登录用户 ID
     */
    private Long getCurrentUserId() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null ||
                !authentication.isAuthenticated() ||
                "anonymousUser".equals(authentication.getName())) {
            return null;
        }

        Users user = sysUserService.getUserByUsername(authentication.getName());
        return user != null ? user.getId() : null;
    }
}
