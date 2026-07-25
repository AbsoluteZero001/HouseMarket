package com.springboot.springboothousemarket.Controller;

import com.springboot.springboothousemarket.Entity.Favorites;
import com.springboot.springboothousemarket.Entity.Users;
import com.springboot.springboothousemarket.Service.FavoritesService;
import com.springboot.springboothousemarket.Service.HousesService;
import com.springboot.springboothousemarket.Service.UsersService;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Tag(name = "房源收藏API")
@RequestMapping("/api/favorites")
@RestController
public class FavoritesController {
    private final FavoritesService favoritesService;
    private final HousesService houseService;
    private final UsersService usersService;

    public FavoritesController(FavoritesService favoritesService, HousesService houseService, UsersService usersService) {
        this.favoritesService = favoritesService;
        this.houseService = houseService;
        this.usersService = usersService;
    }

    /**
     * 添加收藏
     *
     * @param favorites 收藏信息
     * @return 添加结果
     */
    @PostMapping
    public Map<String, Object> addFavorite(@RequestBody Favorites favorites) {
        favoritesService.addFavorite(favorites);

        Map<String, Object> response = new HashMap<>();
        response.put("success", true);
        response.put("message", "收藏成功");

        return response;
    }

    /**
     * 取消收藏
     *
     * @param houseId 房源ID
     * @return 删除结果
     */
    @DeleteMapping("/{houseId}")
    public Map<String, Object> removeFavorite(@PathVariable Long houseId) {
        Long userId = getCurrentUserId();
        if (userId == null) {
            Map<String, Object> response = new HashMap<>();
            response.put("success", false);
            response.put("message", "未登录");
            return response;
        }
        boolean result = favoritesService.removeFavorite(userId, houseId);

        Map<String, Object> response = new HashMap<>();
        if (result) {
            response.put("success", true);
            response.put("message", "取消收藏成功");
        } else {
            response.put("success", false);
            response.put("message", "取消收藏失败");
        }

        return response;
    }

    /**
     * 获取用户收藏列表
     *
     * @return 收藏列表
     */
    @GetMapping
    public Map<String, Object> getFavorites() {
        Long userId = getCurrentUserId();
        if (userId == null) {
            Map<String, Object> response = new HashMap<>();
            response.put("success", false);
            response.put("message", "未登录");
            return response;
        }
        List<Favorites> favorites = favoritesService.getFavoritesByUserId(userId);

        Map<String, Object> data = new HashMap<>();
        data.put("favorites", favorites);

        Map<String, Object> response = new HashMap<>();
        response.put("success", true);
        response.put("data", data);

        return response;
    }

    /**
     * 检查是否已收藏
     *
     * @param userId  用户ID
     * @param houseId 房源ID
     * @return 是否已收藏
     */
    @GetMapping("/check")
    public Map<String, Object> isFavorited(@RequestParam Long userId, @RequestParam Long houseId) {
        boolean favorited = favoritesService.isFavorited(userId, houseId);

        Map<String, Object> data = new HashMap<>();
        data.put("favorited", favorited);

        Map<String, Object> response = new HashMap<>();
        response.put("success", true);
        response.put("data", data);

        return response;
    }

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