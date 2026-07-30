package com.springboot.springboothousemarket.Controller;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.springboot.springboothousemarket.Entity.Houses;
import com.springboot.springboothousemarket.Entity.Users;
import com.springboot.springboothousemarket.Service.HousesService;
import com.springboot.springboothousemarket.Service.UsersService;
import com.springboot.springboothousemarket.dto.ResponseResult;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

import java.io.File;
import java.io.IOException;
import java.util.Map;

@Tag(name = "房源信息API")
@RestController
@RequestMapping("/api/houses")
public class HousesController {

    private final HousesService houseService;
    private final UsersService usersService;

    @Value("${upload.dir:./uploads}")
    private String uploadDir;

    public HousesController(HousesService houseService, UsersService usersService) {
        this.houseService = houseService;
        this.usersService = usersService;
    }

    @GetMapping("/landlord/{landlordId}")
    public ResponseResult getHousesByLandlordId(@PathVariable Long landlordId) {
        return ResponseResult.ok(null, Map.of("houses", houseService.getHousesByLandlordId(landlordId)));
    }

    @PostMapping("/upload-image")
    public String uploadImage(@RequestParam("image") MultipartFile imageFile) throws IOException {
        String fileName = imageFile.getOriginalFilename();
        if (fileName == null || fileName.isEmpty()) {
            throw new IOException("文件名不能为空");
        }

        File uploadDirFile = new File(uploadDir);
        if (!uploadDirFile.exists()) {
            uploadDirFile.mkdirs();
        }

        fileName = new File(fileName).getName();

        String fileExtension = "";
        int lastDotIndex = fileName.lastIndexOf('.');
        if (lastDotIndex > 0) {
            fileExtension = fileName.substring(lastDotIndex);
            fileName = fileName.substring(0, lastDotIndex);
        }
        String uniqueFileName = fileName + "_" + System.currentTimeMillis() + fileExtension;

        File targetFile = new File(uploadDirFile, uniqueFileName);
        imageFile.transferTo(targetFile);

        return ServletUriComponentsBuilder.fromCurrentContextPath()
                .path("/uploads/")
                .path(uniqueFileName)
                .toUriString();
    }

    @PostMapping("/add")
    public ResponseResult createHouse(@RequestBody Houses house) {
        Long userId = getCurrentUserId();
        if (userId == null) {
            throw new SecurityException("未登录或登录状态失效");
        }

        house.setId(null);
        return ResponseResult.ok(null, Map.of("house", houseService.createHouse(house, userId)));
    }

    @GetMapping("/{id}")
    public ResponseResult getHouseById(@PathVariable Long id) {
        Houses house = houseService.getHouseById(id);
        if (house == null) {
            throw new RuntimeException("房源不存在");
        }
        return ResponseResult.ok(null, Map.of("house", house));
    }

    @PutMapping("/{id}")
    public ResponseResult updateHouse(@PathVariable Long id, @RequestBody Houses house) {
        Long currentUserId = getCurrentUserId();
        if (currentUserId == null) {
            throw new SecurityException("未登录或登录状态失效");
        }

        Houses dbHouse = houseService.getHouseById(id);
        if (dbHouse == null) {
            throw new RuntimeException("房源不存在");
        }

        return ResponseResult.ok(null, Map.of("house", houseService.updateHouse(id, house, currentUserId)));
    }

    @DeleteMapping("/{id}")
    public ResponseResult deleteHouse(@PathVariable Long id) {
        Long currentUserId = getCurrentUserId();
        if (currentUserId == null) {
            throw new SecurityException("未登录或登录状态失效");
        }

        Houses dbHouse = houseService.getHouseById(id);
        if (dbHouse == null) {
            throw new RuntimeException("房源不存在");
        }

        return ResponseResult.ok(null, Map.of("deleted", houseService.deleteHouse(id, currentUserId)));
    }

    @GetMapping
    public ResponseResult getHouses(
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

        return ResponseResult.ok(null, Map.of(
                "houses", pageInfo.getRecords(),
                "total", pageInfo.getTotal(),
                "page", pageInfo.getCurrent(),
                "pageSize", pageInfo.getSize()));
    }

    @GetMapping("/my")
    public ResponseResult getMyHouses() {
        Long currentUserId = getCurrentUserId();
        if (currentUserId == null) {
            throw new SecurityException("未登录或登录状态失效");
        }
        return ResponseResult.ok(null, Map.of("houses", houseService.getHousesByLandlordId(currentUserId)));
    }

    private Long getCurrentUserId() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null || !authentication.isAuthenticated() || "anonymousUser".equals(authentication.getName())) {
            return null;
        }
        Users user = usersService.getUserByUsername(authentication.getName());
        return user != null ? user.getId() : null;
    }
}
