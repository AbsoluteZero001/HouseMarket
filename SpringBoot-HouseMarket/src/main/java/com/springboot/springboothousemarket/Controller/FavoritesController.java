package com.springboot.springboothousemarket.Controller;

import com.springboot.springboothousemarket.Entity.Favorites;
import com.springboot.springboothousemarket.Service.FavoritesService;
import com.springboot.springboothousemarket.Service.HouseService;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Tag(name = "房源收藏API")
@RequestMapping("/api/favorites")
@RestController
public class FavoritesController {

    private final FavoritesService favoritesService;
    private final HouseService houseService;

    public FavoritesController(FavoritesService favoritesService, HouseService houseService) {
        this.favoritesService = favoritesService;
        this.houseService = houseService;
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
     * @param userId  用户ID
     * @param houseId 房源ID
     * @return 删除结果
     */
    @DeleteMapping
    public Map<String, Object> removeFavorite(@RequestParam Long userId, @RequestParam Long houseId) {
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
     * @param userId 用户ID
     * @return 收藏列表
     */
    @GetMapping("/{userId}")
    public Map<String, Object> getFavoritesByUserId(@PathVariable Long userId) {
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
}