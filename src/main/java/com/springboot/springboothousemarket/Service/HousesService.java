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

    /**
     * 创建房源：房东创建默认进入 PENDING_REVIEW（管理员创建直接 NORMAL）。
     */
    HouseDetailVO createHouse(HouseCreateDTO dto, Users currentUser);

    /**
     * 房源详情（含可见性校验：非 NORMAL 状态仅房东本人/管理员可见）。
     *
     * @param viewer 当前登录用户，可为 null（匿名访问）
     */
    HouseDetailVO getVisibleHouseDetailVO(Long id, Users viewer);

    Houses getHouseById(Long id);

    void incrementViews(Long id);

    /**
     * 更新房源：房东编辑已上架/被拒房源后自动回到 PENDING_REVIEW 重新审核。
     */
    HouseDetailVO updateHouse(Long id, HouseUpdateDTO dto, Users currentUser);

    /**
     * 删除房源（软删）：需无进行中预约，级联清理图片、收藏与已终结预约。
     */
    boolean deleteHouse(Long id, Users currentUser);

    /**
     * 房源状态流转（房东上下架 / 管理员任意流转，校验状态机合法性）。
     */
    Houses changeStatus(Long id, String targetStatus, String note, Users currentUser);

    /**
     * 管理员审核房源：通过 → NORMAL；拒绝 → REJECTED（需填写审核意见）并通知房东。
     */
    HouseDetailVO reviewHouse(Long id, boolean approve, String note, Users admin);

    List<HouseListVO> getHousesByLandlordVO(Long landlordId);

    Page<HouseListVO> getHouseListVO(String keyword, String type, String district, Double minArea, Double maxArea,
                                     Double minPrice, Double maxPrice, String address, String status, int page, int pageSize);
}
