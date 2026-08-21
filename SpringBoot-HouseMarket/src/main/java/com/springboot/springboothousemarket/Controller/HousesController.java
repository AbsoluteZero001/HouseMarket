package com.springboot.springboothousemarket.Controller;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.springboot.springboothousemarket.Entity.Houses;
import com.springboot.springboothousemarket.Entity.Users;
import com.springboot.springboothousemarket.Service.HouseImageService;
import com.springboot.springboothousemarket.Service.HousesService;
import com.springboot.springboothousemarket.Service.LandlordApplicationService;
import com.springboot.springboothousemarket.dto.*;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.MediaType;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

import java.io.File;
import java.io.IOException;
import java.util.List;
import java.util.Map;

@Tag(name = "房源信息API")
@RestController
@RequestMapping("/api/houses")
public class HousesController {

    private final HousesService houseService;
    private final HouseImageService houseImageService;
    private final LandlordApplicationService landlordApplicationService;

    @Value("${upload.dir:./uploads}")
    private String uploadDir;

    public HousesController(HousesService houseService,
                            HouseImageService houseImageService,
                            LandlordApplicationService landlordApplicationService) {
        this.houseService = houseService;
        this.houseImageService = houseImageService;
        this.landlordApplicationService = landlordApplicationService;
    }

    @GetMapping("/landlord/{landlordId}")
    public ResponseResult getHousesByLandlordId(@PathVariable Long landlordId) {
        return ResponseResult.ok(null, Map.of("houses", houseService.getHousesByLandlordVO(landlordId)));
    }

    @PostMapping("/upload-image")
    @PreAuthorize("hasAnyAuthority('LANDLORD','ADMIN')")
    public ResponseResult uploadImage(@RequestParam("image") MultipartFile imageFile) throws IOException {
        if (imageFile == null || imageFile.isEmpty()) {
            throw new RuntimeException("上传图片不能为空");
        }

        File uploadDirFile = new File(uploadDir);
        if (!uploadDirFile.exists() && !uploadDirFile.mkdirs()) {
            throw new IOException("无法创建上传目录");
        }

        String original = imageFile.getOriginalFilename();
        String baseName = original == null || original.isBlank() ? "image" : original;
        baseName = new File(baseName).getName();
        String extension = "";
        int dot = baseName.lastIndexOf('.');
        if (dot > 0) {
            extension = baseName.substring(dot);
            baseName = baseName.substring(0, dot);
        }
        String uniqueFileName = baseName + "_" + System.currentTimeMillis() + extension;
        File targetFile = new File(uploadDirFile, uniqueFileName);
        imageFile.transferTo(targetFile);

        String url = ServletUriComponentsBuilder.fromCurrentContextPath()
                .path("/uploads/")
                .path(uniqueFileName)
                .toUriString();
        return ResponseResult.ok(null, Map.of("url", url));
    }

    @PostMapping("/add")
    @PreAuthorize("hasAnyAuthority('LANDLORD','ADMIN')")
    public ResponseResult createHouse(@RequestBody HouseCreateDTO dto,
                                      @AuthenticationPrincipal Users currentUser) {
        requireLandlordApproved(currentUser);
        return ResponseResult.ok(null, Map.of("house", houseService.createHouse(dto, currentUser)));
    }

    @GetMapping("/{id}")
    public ResponseResult getHouseById(@PathVariable Long id) {
        houseService.incrementViews(id);
        HouseDetailVO house = houseService.getHouseDetailVO(id);
        if (house == null) {
            throw new RuntimeException("房源不存在");
        }
        return ResponseResult.ok(null, Map.of("house", house));
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasAnyAuthority('LANDLORD','ADMIN')")
    public ResponseResult updateHouse(@PathVariable Long id,
                                      @RequestBody HouseUpdateDTO dto,
                                      @AuthenticationPrincipal Users currentUser) {
        requireLandlordApproved(currentUser);
        return ResponseResult.ok(null, Map.of("house", houseService.updateHouse(id, dto, currentUser)));
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasAnyAuthority('LANDLORD','ADMIN')")
    public ResponseResult deleteHouse(@PathVariable Long id,
                                      @AuthenticationPrincipal Users currentUser) {
        requireHouseOwner(id, currentUser);
        return ResponseResult.ok(null, Map.of("deleted", houseService.deleteHouse(id, currentUser)));
    }

    @GetMapping
    public ResponseResult getHouses(
            @RequestParam(required = false) String keyword,
            @RequestParam(required = false) String type,
            @RequestParam(required = false) Double minArea,
            @RequestParam(required = false) Double maxArea,
            @RequestParam(required = false) Double minPrice,
            @RequestParam(required = false) Double maxPrice,
            @RequestParam(required = false) String district,
            @RequestParam(required = false) String status,
            @RequestParam(required = false) String address,
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "10") int pageSize) {

        Page<HouseListVO> pageInfo = houseService.getHouseListVO(
                keyword, type, district, minArea, maxArea,
                minPrice, maxPrice, address, status, page, pageSize);

        return ResponseResult.ok(null, Map.of(
                "houses", pageInfo.getRecords(),
                "total", pageInfo.getTotal(),
                "page", pageInfo.getCurrent(),
                "pageSize", pageInfo.getSize()));
    }

    @GetMapping("/my")
    @PreAuthorize("hasAnyAuthority('LANDLORD','ADMIN')")
    public ResponseResult getMyHouses(@AuthenticationPrincipal Users currentUser) {
        return ResponseResult.ok(null, Map.of("houses", houseService.getHousesByLandlordVO(currentUser.getId())));
    }

    @PostMapping(value = "/{houseId}/images", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    @PreAuthorize("hasAnyAuthority('LANDLORD','ADMIN')")
    public ResponseResult uploadHouseImage(@PathVariable Long houseId,
                                           @RequestParam("image") MultipartFile image,
                                           @RequestParam(defaultValue = "0") Integer sortOrder,
                                           @RequestParam(defaultValue = "false") boolean isCover,
                                           @AuthenticationPrincipal Users currentUser) throws IOException {
        requireHouseOwner(houseId, currentUser);
        return ResponseResult.ok(null, Map.of("image", houseImageService.uploadImage(houseId, image, sortOrder, isCover)));
    }

    @DeleteMapping("/{houseId}/images/{imageId}")
    @PreAuthorize("hasAnyAuthority('LANDLORD','ADMIN')")
    public ResponseResult deleteHouseImage(@PathVariable Long houseId,
                                           @PathVariable Long imageId,
                                           @AuthenticationPrincipal Users currentUser) {
        requireHouseOwner(houseId, currentUser);
        houseImageService.deleteImage(houseId, imageId);
        return ResponseResult.ok("图片已删除");
    }

    @PutMapping("/{houseId}/images/{imageId}/cover")
    @PreAuthorize("hasAnyAuthority('LANDLORD','ADMIN')")
    public ResponseResult setCoverImage(@PathVariable Long houseId,
                                        @PathVariable Long imageId,
                                        @AuthenticationPrincipal Users currentUser) {
        requireHouseOwner(houseId, currentUser);
        return ResponseResult.ok(null, Map.of("image", houseImageService.setCover(houseId, imageId)));
    }

    @PutMapping("/{houseId}/images/reorder")
    @PreAuthorize("hasAnyAuthority('LANDLORD','ADMIN')")
    public ResponseResult reorderHouseImages(@PathVariable Long houseId,
                                             @RequestBody List<Long> imageIds,
                                             @AuthenticationPrincipal Users currentUser) {
        requireHouseOwner(houseId, currentUser);
        houseImageService.reorder(houseId, imageIds);
        return ResponseResult.ok("图片顺序已更新");
    }

    private void requireLandlordApproved(Users currentUser) {
        if (currentUser == null) {
            throw new SecurityException("请先登录");
        }
        if ("LANDLORD".equals(currentUser.getRole())
                && !landlordApplicationService.hasApproved(currentUser.getId())) {
            throw new RuntimeException("房东入驻审核通过后才能发布房源");
        }
    }

    private void requireHouseOwner(Long houseId, Users currentUser) {
        Houses house = houseService.getById(houseId);
        if (house == null || Integer.valueOf(1).equals(house.getIsDeleted())) {
            throw new RuntimeException("房源不存在");
        }
        if (!"ADMIN".equals(currentUser.getRole()) && !house.getLandlordId().equals(currentUser.getId())) {
            throw new SecurityException("没有权限操作此房源");
        }
    }
}
