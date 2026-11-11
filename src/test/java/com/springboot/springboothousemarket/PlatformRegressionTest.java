package com.springboot.springboothousemarket;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.springboot.springboothousemarket.Controller.LandlordProfileController;
import com.springboot.springboothousemarket.Controller.PublicController;
import com.springboot.springboothousemarket.Controller.UsersController;
import com.springboot.springboothousemarket.Entity.Users;
import com.springboot.springboothousemarket.Service.HousesService;
import com.springboot.springboothousemarket.dto.BusinessException;
import com.springboot.springboothousemarket.dto.HouseListVO;
import com.springboot.springboothousemarket.dto.ResponseResult;
import com.springboot.springboothousemarket.dto.UserVO;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.cache.CacheManager;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.transaction.annotation.Transactional;

import java.sql.Timestamp;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * 平台层公共缺陷的回归测试。
 *
 * <ul>
 *   <li><b>BUG-5</b>：昵称缺少长度校验 → 超过列宽时 MySQL 报 DataIntegrityViolationException，
 *       全局异常处理器按「编程错误」返回 500 与「系统繁忙」，用户不知道自己填错了什么；</li>
 *   <li><b>BUG-6</b>：首页统计把所有预约（被拒绝 / 已取消 / 已过期）都计入，
 *       预约量严重失真；</li>
 *   <li><b>BUG-9</b>：房东资料接口直接返回 Users 实体，响应结构会随实体字段增减而漂移，
 *       且可能外泄 password / id_card_no；</li>
 *   <li><b>BUG-10</b>：房源列表只按 create_time 排序，而 create_time 大量重复时
 *       MySQL 对并列行的返回顺序未定义 → 翻页时出现记录重复或遗漏。</li>
 * </ul>
 */
@SpringBootTest
@Transactional
class PlatformRegressionTest {

    @Autowired
    private UsersController usersController;

    @Autowired
    private LandlordProfileController landlordProfileController;

    @Autowired
    private PublicController publicController;

    @Autowired
    private HousesService housesService;

    @Autowired
    private JdbcTemplate jdbcTemplate;

    @Autowired
    private CacheManager cacheManager;

    private Users user(Long id) {
        Users users = new Users();
        users.setId(id);
        return users;
    }

    // ------------------------------------------------------------------
    // BUG-5：昵称长度校验
    // ------------------------------------------------------------------

    @Test
    void overlyLongNicknameIsRejectedWithBusinessError() {
        Map<String, String> body = Map.of("nickname", "x".repeat(200));

        BusinessException ex = assertThrows(BusinessException.class,
                () -> usersController.updateNickname(body, user(5L)));
        assertTrue(ex.getMessage().contains("昵称"), "实际：" + ex.getMessage());
    }

    @Test
    void blankNicknameIsRejectedWithBusinessError() {
        assertThrows(BusinessException.class,
                () -> usersController.updateNickname(Map.of("nickname", "   "), user(5L)));
    }

    // ------------------------------------------------------------------
    // BUG-9：房东资料接口统一返回 UserVO
    // ------------------------------------------------------------------

    @Test
    void landlordProfileReturnsUserVoInsteadOfEntity() {
        ResponseResult response = landlordProfileController.getProfile(user(2L));

        assertTrue(response.isSuccess());
        Object data = response.getData();
        assertInstanceOf(Map.class, data);
        Object profile = ((Map<?, ?>) data).get("user");
        assertInstanceOf(UserVO.class, profile, "资料接口应返回 UserVO，而不是 Users 实体");
        assertEquals(2L, ((UserVO) profile).getId());
    }

    @Test
    void landlordProfileRejectsAnonymousCaller() {
        assertThrows(SecurityException.class, () -> landlordProfileController.getProfile(null));
    }

    // ------------------------------------------------------------------
    // BUG-6：首页统计只计入有效预约
    // ------------------------------------------------------------------

    @Test
    void homeStatsCountOnlyEffectiveAppointments() {
        // 缓存必须先清空：@Cacheable("home:stats") 会复用上一次的结果
        cacheManager.getCache("home:stats").clear();

        String suffix = UUID.randomUUID().toString().replace("-", "").substring(0, 8);
        insertAppointment("pending", suffix);
        insertAppointment("rejected", suffix);
        insertAppointment("canceled", suffix);
        insertAppointment("expired", suffix);

        cacheManager.getCache("home:stats").clear();

        long expected = countAppointments("status IN ('pending','approved','completed')");
        long total = countAppointments("1 = 1");

        ResponseResult response = publicController.getStats();
        Map<?, ?> data = (Map<?, ?>) response.getData();
        long actual = ((Number) data.get("appointments")).longValue();

        assertEquals(expected, actual, "预约数应等于 pending + approved + completed");
        assertTrue(actual < total,
                "被拒绝 / 已取消 / 已过期的预约不应计入统计，实际：" + actual + " / 总数 " + total);
    }

    // ------------------------------------------------------------------
    // BUG-10：房源列表必须有稳定的次级排序键
    // ------------------------------------------------------------------

    @Test
    void houseListUsesStableSecondarySortKey() {
        // 三条 create_time 完全相同的房源：如果只用 create_time 排序，
        // MySQL 对并列行的返回顺序未定义，翻页时会出现重复 / 遗漏。
        LocalDateTime fixed = LocalDateTime.of(2019, 5, 5, 12, 0, 0);
        String marker = "排序稳定性-" + UUID.randomUUID().toString().replace("-", "").substring(0, 8);
        for (int i = 0; i < 3; i++) {
            jdbcTemplate.update(
                    "INSERT INTO house (title, type, layout, district, area, price, deposit, orientation, "
                            + "decoration, lease_term, tags, address, image, landlord_id, status, create_time, is_deleted) "
                            + "VALUES (?, '平层', '一室一厅', '朝阳区', 50.00, 3000.00, 3000.00, '南北', "
                            + "'精装', '押一付三', '[]', '测试地址', '[]', 2, 'NORMAL', ?, 0)",
                    marker + "-" + i, Timestamp.valueOf(fixed));
        }

        List<Long> insertedIds = jdbcTemplate.queryForList(
                "SELECT id FROM house WHERE title LIKE ? ORDER BY id", Long.class, marker + "-%");
        assertEquals(3, insertedIds.size());

        Page<HouseListVO> page = housesService.getHouseListVO(
                null, null, null, null, null, null, null, null, "NORMAL", 1, 50);

        List<Long> resultIds = page.getRecords().stream().map(HouseListVO::getId).toList();
        List<Long> found = resultIds.stream().filter(insertedIds::contains).toList();

        assertEquals(3, found.size(), "三条测试房源都应出现在列表中");
        assertEquals(List.of(insertedIds.get(2), insertedIds.get(1), insertedIds.get(0)), found,
                "create_time 相同时必须按 id 降序，保证分页稳定");
    }

    private void insertAppointment(String status, String suffix) {
        jdbcTemplate.update(
                "INSERT INTO appointment (house_id, tenant_id, landlord_id, time, location, request_id, "
                        + "status, version, create_time, update_time) "
                        + "VALUES (1, 5, 2, ?, ?, ?, ?, 0, NOW(), NOW())",
                Timestamp.valueOf(LocalDateTime.now().plusDays(7)), "统计测试地点",
                "stats-" + suffix + "-" + status, status);
    }

    private long countAppointments(String where) {
        Long count = jdbcTemplate.queryForObject(
                "SELECT COUNT(*) FROM appointment WHERE " + where, Long.class);
        return count == null ? 0L : count;
    }
}