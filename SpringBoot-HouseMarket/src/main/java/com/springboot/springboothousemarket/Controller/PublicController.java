package com.springboot.springboothousemarket.Controller;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.springboot.springboothousemarket.Entity.Houses;
import com.springboot.springboothousemarket.Entity.Users;
import com.springboot.springboothousemarket.Service.AppointmentService;
import com.springboot.springboothousemarket.Service.HousesService;
import com.springboot.springboothousemarket.Service.UsersService;
import com.springboot.springboothousemarket.dto.ResponseResult;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

@Tag(name = "首页公开API")
@RestController
@RequestMapping("/api/public")
public class PublicController {

    private final HousesService housesService;
    private final UsersService usersService;
    private final AppointmentService appointmentService;

    public PublicController(HousesService housesService,
                            UsersService usersService,
                            AppointmentService appointmentService) {
        this.housesService = housesService;
        this.usersService = usersService;
        this.appointmentService = appointmentService;
    }

    @GetMapping("/houses")
    public ResponseResult getPublicHouses(
            @RequestParam(required = false) String keyword,
            @RequestParam(required = false) String type,
            @RequestParam(required = false) String district,
            @RequestParam(required = false) Double minArea,
            @RequestParam(required = false) Double maxArea,
            @RequestParam(required = false) Double minPrice,
            @RequestParam(required = false) Double maxPrice,
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "8") int pageSize) {
        var pageInfo = housesService.getHouseListVO(
                keyword, type, district, minArea, maxArea,
                minPrice, maxPrice, null, "NORMAL", page, pageSize);
        return ResponseResult.ok(null, Map.of(
                "houses", pageInfo.getRecords(),
                "total", pageInfo.getTotal(),
                "page", pageInfo.getCurrent(),
                "pageSize", pageInfo.getSize()));
    }

    @GetMapping("/stats")
    @Cacheable(cacheNames = "home:stats")
    public ResponseResult getStats() {
        long housesCount = housesService.count(
                new QueryWrapper<Houses>().eq("is_deleted", 0).eq("status", "NORMAL"));
        long landlordsCount = usersService.lambdaQuery()
                .eq(Users::getRole, "LANDLORD")
                .eq(Users::getIsDeleted, 0)
                .count();
        long tenantsCount = usersService.lambdaQuery()
                .eq(Users::getRole, "TENANT")
                .eq(Users::getIsDeleted, 0)
                .count();
        long appointmentsCount = appointmentService.count();

        return ResponseResult.ok(null, Map.of(
                "houses", housesCount,
                "landlords", landlordsCount,
                "tenants", tenantsCount,
                "appointments", appointmentsCount));
    }
}
