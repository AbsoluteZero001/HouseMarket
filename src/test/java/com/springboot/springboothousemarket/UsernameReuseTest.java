package com.springboot.springboothousemarket;

import com.springboot.springboothousemarket.Entity.Users;
import com.springboot.springboothousemarket.Service.RegisterRequestService;
import com.springboot.springboothousemarket.Service.UsersService;
import com.springboot.springboothousemarket.dto.BusinessException;
import com.springboot.springboothousemarket.dto.RegisterRequest;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * 用户名复用（BUG-1）回归测试。
 *
 * <p>背景：{@code sysuser.uk_username} 是 username 单列唯一索引，
 * 而逻辑删除（{@code is_deleted=1}）的行仍留在表里、依旧占用该用户名；
 * 同时所有按用户名查询的地方都带 {@code AND is_deleted = 0}。
 * 于是「注册前的重名检查」查不到已删除用户 → 直接 INSERT 撞唯一键 →
 * DuplicateKeyException → 全局异常处理器按「编程错误」返回 HTTP 500。
 * 结果：管理员删掉某个用户后，该用户名<b>永远</b>无法再次注册。
 *
 * <p>修复：删除前把用户名改写成「原名#del#&lt;用户ID&gt;」，腾出原用户名；
 * 同时把 INSERT 阶段撞唯一键转成可预期的业务错误（HTTP 400）。
 */
@SpringBootTest
@Transactional
class UsernameReuseTest {

    @Autowired
    private RegisterRequestService registerRequestService;

    @Autowired
    private UsersService usersService;

    @Autowired
    private JdbcTemplate jdbcTemplate;

    private RegisterRequest newRegisterRequest(String username) {
        RegisterRequest request = new RegisterRequest();
        request.setUsername(username);
        request.setPassword("secret123");
        request.setNickname("用户名复用测试");
        request.setRole("TENANT");
        return request;
    }

    private String uniqueUsername(String prefix) {
        return prefix + UUID.randomUUID().toString().replace("-", "").substring(0, 10);
    }

    /**
     * 主场景：删除用户后，该用户名可以立即被重新注册。
     */
    @Test
    void deletedUsernameCanBeRegisteredAgain() {
        String username = uniqueUsername("reuse_");
        registerRequestService.register(newRegisterRequest(username));

        Users created = usersService.getUserByUsername(username);
        assertNotNull(created, "注册后应能查到用户");
        Long oldId = created.getId();

        assertTrue(usersService.deleteUser(oldId, null), "删除用户应成功");

        // 旧行仍在（逻辑删除），但用户名已被改写，不再占用原用户名
        String storedUsername = jdbcTemplate.queryForObject(
                "SELECT username FROM sysuser WHERE id = ?", String.class, oldId);
        Integer isDeleted = jdbcTemplate.queryForObject(
                "SELECT is_deleted FROM sysuser WHERE id = ?", Integer.class, oldId);
        assertNotNull(storedUsername);
        assertEquals(1, isDeleted, "应为逻辑删除，旧行仍需保留以便审计");
        assertNotEquals(username, storedUsername, "删除后应释放原用户名");
        assertTrue(storedUsername.startsWith(username), "新用户名应保留原名前缀，实际：" + storedUsername);
        assertTrue(storedUsername.contains("#del#" + oldId), "新用户名应带上用户ID，实际：" + storedUsername);

        // 关键断言：同一用户名可以重新注册成功
        registerRequestService.register(newRegisterRequest(username));

        Users reCreated = usersService.getUserByUsername(username);
        assertNotNull(reCreated, "原用户名应可被重新注册");
        assertNotEquals(oldId, reCreated.getId(), "重新注册应生成新的用户ID");
    }

    /**
     * 历史数据场景：库里存在「逻辑删除但仍占用用户名」的老账号（修复前遗留）。
     * 修复后不应再抛 500 DuplicateKeyException，而应抛出可预期的业务异常（HTTP 400）。
     */
    @Test
    void legacyDeletedRowBlockingUsernameYieldsBusinessErrorNot500() {
        String username = uniqueUsername("legacy_");
        // 直接写库模拟历史脏数据：is_deleted=1 但仍占用唯一索引
        jdbcTemplate.update(
                "INSERT INTO sysuser (username, password, nickname, avatar, role, status, is_deleted) "
                        + "VALUES (?, ?, ?, ?, ?, ?, 1)",
                username, "legacy-hash", "历史用户", "/uploads/avatars/default.png", "TENANT", "normal");

        // 前置校验：按用户名查询（带 is_deleted=0）确实查不到这行
        assertNull(usersService.getUserByUsername(username));

        BusinessException ex = assertThrows(BusinessException.class,
                () -> registerRequestService.register(newRegisterRequest(username)));
        assertTrue(ex.getMessage().contains("用户名"), "应返回可理解的业务提示，实际：" + ex.getMessage());
    }

    /**
     * 正常场景：活跃用户重复注册同名 → 业务异常「用户已存在」。
     */
    @Test
    void activeUsernameCannotBeRegisteredTwice() {
        String username = uniqueUsername("dup_");
        registerRequestService.register(newRegisterRequest(username));

        BusinessException ex = assertThrows(BusinessException.class,
                () -> registerRequestService.register(newRegisterRequest(username)));
        assertEquals("用户已存在", ex.getMessage());
    }

    /**
     * 超长用户名（正好占满 VARCHAR(100)）在删除后改写时不得超出列宽，
     * 否则会再次撞上 DataIntegrityViolationException。
     */
    @Test
    void releasingVeryLongUsernameStaysWithinColumnWidth() {
        String username = "L" + "x".repeat(99);
        assertEquals(100, username.length());
        registerRequestService.register(newRegisterRequest(username));

        Users created = usersService.getUserByUsername(username);
        assertNotNull(created);
        assertTrue(usersService.deleteUser(created.getId(), null));

        String storedUsername = jdbcTemplate.queryForObject(
                "SELECT username FROM sysuser WHERE id = ?", String.class, created.getId());
        assertNotNull(storedUsername);
        assertTrue(storedUsername.length() <= 100,
                "改写后的用户名长度不得超过列宽 100，实际：" + storedUsername.length());
        assertTrue(storedUsername.endsWith("#del#" + created.getId()));
    }

    private static void assertNull(Object value) {
        org.junit.jupiter.api.Assertions.assertNull(value);
    }
}