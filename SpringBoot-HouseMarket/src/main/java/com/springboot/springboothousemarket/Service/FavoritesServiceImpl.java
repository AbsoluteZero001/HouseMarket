package com.springboot.springboothousemarket.Service;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.springboot.springboothousemarket.Entity.Favorites;
import com.springboot.springboothousemarket.Mapper.FavoritesMapper;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class FavoritesServiceImpl extends ServiceImpl<FavoritesMapper, Favorites> implements FavoritesService {

    private final FavoritesMapper favoritesMapper;

    public FavoritesServiceImpl(FavoritesMapper favoritesMapper) {
        this.favoritesMapper = favoritesMapper;
    }

    @Override
    public Favorites addFavorite(Favorites favorites) {
        // 先检查是否已经收藏
        Favorites existing = favoritesMapper.selectByUserIdAndHouseId(favorites.getUserId(), favorites.getHouseId());
        if (existing != null) {
            return existing; // 如果已经收藏，直接返回已存在的记录
        }

        favoritesMapper.insert(favorites);
        return favorites;
    }

    @Override
    public boolean removeFavorite(Long userId, Long houseId) {
        // 即使没有找到记录，也返回true，因为目标是确保该收藏不存在
        favoritesMapper.deleteByUserIdAndHouseId(userId, houseId);
        return true;
    }

    @Override
    public List<Favorites> getFavoritesByUserId(Long userId) {
        return favoritesMapper.selectByUserId(userId);
    }

    @Override
    public boolean isFavorited(Long userId, Long houseId) {
        Favorites favorites = favoritesMapper.selectByUserIdAndHouseId(userId, houseId);
        return favorites != null;
    }
}