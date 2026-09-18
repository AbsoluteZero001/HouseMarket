package com.springboot.springboothousemarket.Controller;

import com.springboot.springboothousemarket.Entity.Favorites;
import com.springboot.springboothousemarket.Entity.Users;
import com.springboot.springboothousemarket.Service.FavoritesService;
import com.springboot.springboothousemarket.dto.ResponseResult;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@Tag(name = "房源收藏API")
@RequestMapping("/api/favorites")
@RestController
public class FavoritesController {
    private final FavoritesService favoritesService;

    public FavoritesController(FavoritesService favoritesService) {
        this.favoritesService = favoritesService;
    }

    @PostMapping
    public ResponseResult addFavorite(@RequestBody Favorites favorites,
                                      @AuthenticationPrincipal Users currentUser) {
        favorites.setId(null);
        favorites.setUserId(currentUser.getId());
        favoritesService.addFavorite(favorites);
        return ResponseResult.ok("收藏成功");
    }

    @DeleteMapping("/{houseId}")
    public ResponseResult removeFavorite(@PathVariable Long houseId,
                                         @AuthenticationPrincipal Users currentUser) {
        boolean result = favoritesService.removeFavorite(currentUser.getId(), houseId);
        return result ? ResponseResult.ok("取消收藏成功") : ResponseResult.fail("取消收藏失败");
    }

    @GetMapping
    public ResponseResult getFavorites(@AuthenticationPrincipal Users currentUser) {
        return ResponseResult.ok(null, Map.of("favorites", favoritesService.getFavoritesByUserId(currentUser.getId())));
    }

    @GetMapping("/check")
    public ResponseResult isFavorited(@RequestParam Long houseId,
                                      @AuthenticationPrincipal Users currentUser) {
        return ResponseResult.ok(null, Map.of("favorited", favoritesService.isFavorited(currentUser.getId(), houseId)));
    }
}
