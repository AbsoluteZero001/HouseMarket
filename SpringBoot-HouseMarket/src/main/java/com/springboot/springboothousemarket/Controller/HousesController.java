package com.springboot.springboothousemarket.Controller;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.springboot.springboothousemarket.Entity.Houses;
import com.springboot.springboothousemarket.Entity.Users;
import com.springboot.springboothousemarket.Service.HousesService;
import com.springboot.springboothousemarket.Service.UsersService;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Tag(name = "房源信息API")
@RestController
@RequestMapping("/api/houses")
public class HousesController {

    private final HousesService houseService;
    private final UsersService usersService;

    @Value("${upload.dir:./uploads}")
    private String uploadDir; // 图片保存的路径

    public HousesController(HousesService houseService, UsersService usersService) {
        this.houseService = houseService;
        this.usersService = usersService;
    }

    /**
     * 根据房东ID获取房源列表
     */
    @GetMapping("/landlord/{landlordId}")
    public Map<String, Object> getHousesByLandlordId(@PathVariable Long landlordId) {
        List<Houses> houses = houseService.getHousesByLandlordId(landlordId);
        Map<String, Object> data = new HashMap<>();
        data.put("houses", houses);

        Map<String, Object> response = new HashMap<>();
        response.put("success", true);
        response.put("data", data);
        return response;
    }

    /**
     * 图片上传接口
     */
    @PostMapping("/upload-image")
    public String uploadImage(@RequestParam("image") MultipartFile imageFile) throws IOException {
        // 获取文件名
        String fileName = imageFile.getOriginalFilename();
        if (fileName == null || fileName.isEmpty()) {
            throw new IOException("文件名不能为空");
        }

        // 确保上传目录存在
        File uploadDirFile = new File(uploadDir);
        if (!uploadDirFile.exists()) {
            uploadDirFile.mkdirs();
        }

        // 防止路径遍历攻击，只取文件名
        fileName = new File(fileName).getName();

        // 生成唯一文件名，避免重复
        String fileExtension = "";
        int lastDotIndex = fileName.lastIndexOf('.');
        if (lastDotIndex > 0) {
            fileExtension = fileName.substring(lastDotIndex);
            fileName = fileName.substring(0, lastDotIndex);
        }
        String uniqueFileName = fileName + "_" + System.currentTimeMillis() + fileExtension;

        // 保存文件到本地
        File targetFile = new File(uploadDirFile, uniqueFileName);
        imageFile.transferTo(targetFile);

        // 获取图片的访问路径
        String imageUrl = ServletUriComponentsBuilder.fromCurrentContextPath()
                .path("/uploads/")
                .path(uniqueFileName)
                .toUriString();

        return imageUrl; // 返回图片的 URL 地址
    }

    /**
     * 新增房源
     */
    @PostMapping("/add")
    public Map<String, Object> createHouse(@RequestBody Houses house) {
        Long userId = getCurrentUserId();
        if (userId == null) {
            throw new SecurityException("未登录或登录状态失效");
        }

        house.setId(null); // 防止前端传 id
        house = houseService.createHouse(house, userId);

        Map<String, Object> data = new HashMap<>();
        data.put("house", house);

        Map<String, Object> response = new HashMap<>();
        response.put("success", true);
        response.put("data", data);
        return response;
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
    public Map<String, Object> updateHouse(@PathVariable Long id, @RequestBody Houses house) {
        Long currentUserId = getCurrentUserId();
        if (currentUserId == null) {
            throw new SecurityException("未登录或登录状态失效");
        }

        Houses dbHouse = houseService.getHouseById(id);
        if (dbHouse == null) {
            throw new RuntimeException("房源不存在");
        }

        house = houseService.updateHouse(id, house, currentUserId);

        Map<String, Object> data = new HashMap<>();
        data.put("house", house);

        Map<String, Object> response = new HashMap<>();
        response.put("success", true);
        response.put("data", data);
        return response;
    }

    /**
     * 删除房源（逻辑删除，且只能删除自己的）
     */
    @DeleteMapping("/{id}")
    public Map<String, Object> deleteHouse(@PathVariable Long id) {
        Long currentUserId = getCurrentUserId();
        if (currentUserId == null) {
            throw new SecurityException("未登录或登录状态失效");
        }

        Houses dbHouse = houseService.getHouseById(id);
        if (dbHouse == null) {
            throw new RuntimeException("房源不存在");
        }

        boolean isDeleted = houseService.deleteHouse(id, currentUserId);

        Map<String, Object> data = new HashMap<>();
        data.put("deleted", isDeleted);

        Map<String, Object> response = new HashMap<>();
        response.put("success", true);
        response.put("data", data);
        return response;
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
                minPrice, maxPrice, address, page, pageSize);

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
    public Map<String, Object> getMyHouses() {
        Long currentUserId = getCurrentUserId();
        if (currentUserId == null) {
            throw new SecurityException("未登录或登录状态失效");
        }

        List<Houses> houses = houseService.getHousesByLandlordId(currentUserId);

        Map<String, Object> data = new HashMap<>();
        data.put("houses", houses);

        Map<String, Object> response = new HashMap<>();
        response.put("success", true);
        response.put("data", data);
        return response;
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

        Users user = usersService.getUserByUsername(authentication.getName());
        return user != null ? user.getId() : null;
    }
}
