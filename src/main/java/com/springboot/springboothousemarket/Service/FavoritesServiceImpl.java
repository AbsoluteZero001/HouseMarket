package com.springboot.springboothousemarket.Service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.springboot.springboothousemarket.Entity.Favorites;
import com.springboot.springboothousemarket.Entity.Houses;
import com.springboot.springboothousemarket.Mapper.FavoritesMapper;
import com.springboot.springboothousemarket.common.HouseStatus;
import com.springboot.springboothousemarket.dto.BusinessException;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class FavoritesServiceImpl extends ServiceImpl<FavoritesMapper, Favorites> implements FavoritesService {

    private final FavoritesMapper favoritesMapper;
    private final HousesService housesService;

    public FavoritesServiceImpl(FavoritesMapper favoritesMapper, HousesService housesService) {
        this.favoritesMapper = favoritesMapper;
        this.housesService = housesService;
    }

    @Override
    public Favorites addFavorite(Favorites favorites) {
        if (favorites.getHouseId() == null) {
            throw new BusinessException("房源ID不能为空");
        }
        Houses house = housesService.getHouseById(favorites.getHouseId());
        if (house == null) {
            throw new BusinessException("房源不存在或已删除");
        }
        if (!HouseStatus.NORMAL.equals(house.getStatus())) {
            throw new BusinessException("该房源已下架，无法收藏");
        }

        // 幂等：已收藏直接返回（数据库另有 (user_id, house_id) 唯一约束兜底并发）
        Favorites existing = favoritesMapper.selectOne(new LambdaQueryWrapper<Favorites>()
                .eq(Favorites::getUserId, favorites.getUserId())
                .eq(Favorites::getHouseId, favorites.getHouseId()));
        if (existing != null) {
            return existing;
        }
        favorites.setCreateTime(java.time.LocalDateTime.now());
        try {
            favoritesMapper.insert(favorites);
        } catch (DuplicateKeyException e) {
            return favoritesMapper.selectOne(new LambdaQueryWrapper<Favorites>()
                    .eq(Favorites::getUserId, favorites.getUserId())
                    .eq(Favorites::getHouseId, favorites.getHouseId()));
        }
        return favorites;
    }

    @Override
    public boolean removeFavorite(Long userId, Long houseId) {
        // 幂等：即使记录不存在也视为删除成功
        favoritesMapper.deleteByUserIdAndHouseId(userId, houseId);
        return true;
    }

    @Override
    public List<Favorites> getFavoritesByUserId(Long userId) {
        return favoritesMapper.selectByUserId(userId);
    }

    @Override
    public boolean isFavorited(Long userId, Long houseId) {
        return favoritesMapper.selectCount(new LambdaQueryWrapper<Favorites>()
                .eq(Favorites::getUserId, userId)
                .eq(Favorites::getHouseId, houseId)) > 0;
    }
}
