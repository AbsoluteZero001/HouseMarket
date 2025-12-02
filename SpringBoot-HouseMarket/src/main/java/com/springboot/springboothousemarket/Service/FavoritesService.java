package com.springboot.springboothousemarket.Service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.springboot.springboothousemarket.Entity.Favorites;

import java.util.List;

public interface FavoritesService extends IService<Favorites> {

    /**
     * 添加收藏
     *
     * @param favorites 收藏信息
     * @return 添加结果
     */
    Favorites addFavorite(Favorites favorites);

    /**
     * 取消收藏
     *
     * @param userId  用户ID
     * @param houseId 房源ID
     * @return 删除结果
     */
    boolean removeFavorite(Long userId, Long houseId);

    /**
     * 获取用户收藏列表
     *
     * @param userId 用户ID
     * @return 收藏列表
     */
    List<Favorites> getFavoritesByUserId(Long userId);

    /**
     * 检查是否已收藏
     *
     * @param userId  用户ID
     * @param houseId 房源ID
     * @return 是否已收藏
     */
    boolean isFavorited(Long userId, Long houseId);
}