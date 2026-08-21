package com.springboot.springboothousemarket.Service;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.IService;
import com.springboot.springboothousemarket.Entity.Houses;
import com.springboot.springboothousemarket.Entity.Users;
import com.springboot.springboothousemarket.dto.HouseCreateDTO;
import com.springboot.springboothousemarket.dto.HouseDetailVO;
import com.springboot.springboothousemarket.dto.HouseListVO;
import com.springboot.springboothousemarket.dto.HouseUpdateDTO;

import java.util.List;

public interface HousesService extends IService<Houses> {

    HouseDetailVO createHouse(HouseCreateDTO dto, Users currentUser);

    HouseDetailVO getHouseDetailVO(Long id);

    Houses getHouseById(Long id);

    void incrementViews(Long id);

    HouseDetailVO updateHouse(Long id, HouseUpdateDTO dto, Users currentUser);

    boolean deleteHouse(Long id, Users currentUser);

    List<HouseListVO> getHousesByLandlordVO(Long landlordId);

    Page<HouseListVO> getHouseListVO(String keyword, String type, String district, Double minArea, Double maxArea,
                                     Double minPrice, Double maxPrice, String address, String status, int page, int pageSize);
}
