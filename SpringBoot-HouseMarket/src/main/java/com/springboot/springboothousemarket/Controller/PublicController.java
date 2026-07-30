package com.springboot.springboothousemarket.Controller;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.springboot.springboothousemarket.Entity.Houses;
import com.springboot.springboothousemarket.Entity.Users;
import com.springboot.springboothousemarket.Service.AppointmentService;
import com.springboot.springboothousemarket.Service.HousesService;
import com.springboot.springboothousemarket.Service.UsersService;
import com.springboot.springboothousemarket.dto.ResponseResult;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
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
    public ResponseResult getPublicHouses() {
        QueryWrapper<Houses> query = new QueryWrapper<>();
        query.eq("is_deleted", 0);
        query.orderByDesc("create_time");
        query.last("LIMIT 6");
        return ResponseResult.ok(null, Map.of("houses", housesService.list(query)));
    }

    @GetMapping("/stats")
    public ResponseResult getStats() {
        long housesCount = housesService.count(
                new QueryWrapper<Houses>().eq("is_deleted", 0));
        long landlordsCount = usersService.count(
                new QueryWrapper<Users>().eq("role", "LANDLORD").eq("isDeleted", 0));
        long tenantsCount = usersService.count(
                new QueryWrapper<Users>().eq("role", "TENANT").eq("isDeleted", 0));
        long appointmentsCount = appointmentService.count();

        return ResponseResult.ok(null, Map.of(
                "houses", housesCount,
                "landlords", landlordsCount,
                "tenants", tenantsCount,
                "appointments", appointmentsCount));
    }
}
