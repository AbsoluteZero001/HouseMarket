package com.springboot.springboothousemarket.Controller;

import com.springboot.springboothousemarket.Entity.Favorites;
import com.springboot.springboothousemarket.Entity.Users;
import com.springboot.springboothousemarket.Service.FavoritesService;
import com.springboot.springboothousemarket.Service.HousesService;
import com.springboot.springboothousemarket.Service.UsersService;
import com.springboot.springboothousemarket.dto.ResponseResult;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@Tag(name = "房源收藏API")
@RequestMapping("/api/favorites")
@RestController
public class FavoritesController {
    private final FavoritesService favoritesService;
    private final UsersService usersService;

    public FavoritesController(FavoritesService favoritesService, HousesService houseService, UsersService usersService) {
        this.favoritesService = favoritesService;
        this.usersService = usersService;
    }

    @PostMapping
    public ResponseResult addFavorite(@RequestBody Favorites favorites) {
        favoritesService.addFavorite(favorites);
        return ResponseResult.ok("收藏成功");
    }

    @DeleteMapping("/{houseId}")
    public ResponseResult removeFavorite(@PathVariable Long houseId) {
        Long userId = getCurrentUserId();
        if (userId == null) {
            return ResponseResult.fail("未登录");
        }
        boolean result = favoritesService.removeFavorite(userId, houseId);
        return result ? ResponseResult.ok("取消收藏成功") : ResponseResult.fail("取消收藏失败");
    }

    @GetMapping
    public ResponseResult getFavorites() {
        Long userId = getCurrentUserId();
        if (userId == null) {
            return ResponseResult.fail("未登录");
        }
        return ResponseResult.ok(null, Map.of("favorites", favoritesService.getFavoritesByUserId(userId)));
    }

    @GetMapping("/check")
    public ResponseResult isFavorited(@RequestParam Long userId, @RequestParam Long houseId) {
        return ResponseResult.ok(null, Map.of("favorited", favoritesService.isFavorited(userId, houseId)));
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
