package com.springboot.springboothousemarket.Controller;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.springboot.springboothousemarket.Entity.Houses;
import com.springboot.springboothousemarket.Entity.Users;
import com.springboot.springboothousemarket.Service.HouseImageService;
import com.springboot.springboothousemarket.Service.HousesService;
import com.springboot.springboothousemarket.Service.LandlordApplicationService;
import com.springboot.springboothousemarket.common.HouseStatus;
import com.springboot.springboothousemarket.dto.*;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.MediaType;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
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

    /**
     * 按房东查房源：仅本人或管理员可查（返回该房东全部状态房源用于管理）。
     */
    @GetMapping("/landlord/{landlordId}")
    public ResponseResult getHousesByLandlordId(@PathVariable Long landlordId,
                                                @AuthenticationPrincipal Users currentUser) {
        if (currentUser == null
                || (!currentUser.getId().equals(landlordId) && !"ADMIN".equals(currentUser.getRole()))) {
            throw new org.springframework.security.access.AccessDeniedException("只能查看自己的房源列表");
        }
        return ResponseResult.ok(null, Map.of("houses", houseService.getHousesByLandlordVO(landlordId)));
    }

    /**
     * 通用图片上传（房东/管理员）。
     */
    @PostMapping("/upload-image")
    @PreAuthorize("hasAnyAuthority('LANDLORD','ADMIN')")
    public ResponseResult uploadImage(@RequestParam(value = "image", required = false) MultipartFile imageFile,
                                      @RequestParam(value = "file", required = false) MultipartFile fileParam,
                                      @RequestParam(value = "imageFile", required = false) MultipartFile imageFileParam) throws IOException {
        MultipartFile uploadFile = firstNonEmpty(imageFile, fileParam, imageFileParam);
        if (uploadFile == null || uploadFile.isEmpty()) {
            throw new RuntimeException("上传图片不能为空");
        }

        Path uploadDirPath = Paths.get(uploadDir).toAbsolutePath().normalize();
        Files.createDirectories(uploadDirPath);

        String original = uploadFile.getOriginalFilename();
        String baseName = original == null || original.isBlank() ? "image" : original;
        baseName = Paths.get(baseName).getFileName().toString();
        String extension = "";
        int dot = baseName.lastIndexOf('.');
        if (dot > 0) {
            extension = baseName.substring(dot);
            baseName = baseName.substring(0, dot);
        }
        if (!extension.isBlank()
                && !List.of(".jpg", ".jpeg", ".png", ".gif", ".webp").contains(extension.toLowerCase())) {
            throw new RuntimeException("仅支持 JPG、PNG、GIF、WEBP 格式图片");
        }
        String uniqueFileName = baseName + "_" + System.currentTimeMillis() + extension;
        Path targetFile = uploadDirPath.resolve(uniqueFileName).normalize();
        if (!targetFile.startsWith(uploadDirPath)) {
            throw new IOException("非法的上传文件名");
        }
        Files.copy(uploadFile.getInputStream(), targetFile, StandardCopyOption.REPLACE_EXISTING);

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
        return ResponseResult.ok("房源已提交" + ("ADMIN".equals(currentUser.getRole()) ? "" : "，等待管理员审核后上架"),
                Map.of("house", houseService.createHouse(dto, currentUser)));
    }

    /**
     * 房源详情：非 NORMAL 状态仅房东本人/管理员可见；浏览量只对可见用户累计。
     */
    @GetMapping("/{id}")
    public ResponseResult getHouseById(@PathVariable Long id,
                                       @AuthenticationPrincipal Users currentUser) {
        HouseDetailVO house = houseService.getVisibleHouseDetailVO(id, currentUser);
        if (house == null) {
            throw new RuntimeException("房源不存在或已下架");
        }
        houseService.incrementViews(id);
        return ResponseResult.ok(null, Map.of("house", house));
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasAnyAuthority('LANDLORD','ADMIN')")
    public ResponseResult updateHouse(@PathVariable Long id,
                                      @RequestBody HouseUpdateDTO dto,
                                      @AuthenticationPrincipal Users currentUser) {
        requireLandlordApproved(currentUser);
        return ResponseResult.ok("房源已更新", Map.of("house", houseService.updateHouse(id, dto, currentUser)));
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasAnyAuthority('LANDLORD','ADMIN')")
    public ResponseResult deleteHouse(@PathVariable Long id,
                                      @AuthenticationPrincipal Users currentUser) {
        return ResponseResult.ok(null, Map.of("deleted", houseService.deleteHouse(id, currentUser)));
    }

    /**
     * 房源列表（公开浏览）。非管理员强制只看 NORMAL 已上架房源。
     */
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
            @RequestParam(defaultValue = "10") int pageSize,
            @AuthenticationPrincipal Users currentUser) {

        boolean isAdmin = currentUser != null && "ADMIN".equals(currentUser.getRole());
        String effectiveStatus = isAdmin ? status : HouseStatus.NORMAL;

        Page<HouseListVO> pageInfo = houseService.getHouseListVO(
                keyword, type, district, minArea, maxArea,
                minPrice, maxPrice, address, effectiveStatus, page, pageSize);

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

    /**
     * 房东上下架 / 管理员状态流转。
     */
    @PutMapping("/{id}/status")
    @PreAuthorize("hasAnyAuthority('LANDLORD','ADMIN')")
    public ResponseResult changeStatus(@PathVariable Long id,
                                       @RequestBody Map<String, String> body,
                                       @AuthenticationPrincipal Users currentUser) {
        String status = body == null ? null : body.get("status");
        String note = body == null ? null : body.get("note");
        houseService.changeStatus(id, status, note, currentUser);
        return ResponseResult.ok(HouseStatus.NORMAL.equals(status) ? "房源已上架" : "房源状态已更新");
    }

    /**
     * 管理员审核房源：approve=true → NORMAL 上架；false → REJECTED（需填写意见）。
     */
    @PutMapping("/{id}/review")
    @PreAuthorize("hasAuthority('ADMIN')")
    public ResponseResult reviewHouse(@PathVariable Long id,
                                      @RequestBody Map<String, Object> body,
                                      @AuthenticationPrincipal Users currentUser) {
        boolean approve = Boolean.TRUE.equals(body.get("approve"))
                || "true".equalsIgnoreCase(String.valueOf(body.get("approve")));
        String note = body.get("note") == null ? null : String.valueOf(body.get("note"));
        HouseDetailVO house = houseService.reviewHouse(id, approve, note, currentUser);
        return ResponseResult.ok(approve ? "房源审核通过，已上架" : "房源已驳回", Map.of("house", house));
    }

    @PostMapping(value = "/{houseId}/images", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    @PreAuthorize("hasAnyAuthority('LANDLORD','ADMIN')")
    public ResponseResult uploadHouseImage(@PathVariable Long houseId,
                                           @RequestParam(value = "image", required = false) MultipartFile image,
                                           @RequestParam(value = "file", required = false) MultipartFile fileParam,
                                           @RequestParam(value = "imageFile", required = false) MultipartFile imageFileParam,
                                           @RequestParam(value = "imageType", defaultValue = "OTHER") String imageType,
                                           @RequestParam(defaultValue = "0") Integer sortOrder,
                                           @RequestParam(defaultValue = "false") boolean isCover,
                                           @AuthenticationPrincipal Users currentUser) throws IOException {
        requireHouseOwner(houseId, currentUser);
        MultipartFile uploadFile = firstNonEmpty(image, fileParam, imageFileParam);
        return ResponseResult.ok(null, Map.of("image", houseImageService.uploadImage(houseId, uploadFile, imageType, sortOrder, isCover)));
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
        if ("LANDLORD".equals(currentUser.getRole())
                && !Integer.valueOf(1).equals(currentUser.getRealNameVerified())) {
            throw new RuntimeException("请先完成房东实名认证后再发布房源");
        }
    }

    private void requireHouseOwner(Long houseId, Users currentUser) {
        Houses house = houseService.getById(houseId);
        if (house == null || Integer.valueOf(1).equals(house.getIsDeleted())) {
            throw new RuntimeException("房源不存在");
        }
        if (!"ADMIN".equals(currentUser.getRole()) && !house.getLandlordId().equals(currentUser.getId())) {
            throw new org.springframework.security.access.AccessDeniedException("没有权限操作此房源");
        }
    }

    private MultipartFile firstNonEmpty(MultipartFile... files) {
        for (MultipartFile file : files) {
            if (file != null && !file.isEmpty()) {
                return file;
            }
        }
        return null;
    }
}
