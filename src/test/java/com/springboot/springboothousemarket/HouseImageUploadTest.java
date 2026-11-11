package com.springboot.springboothousemarket;

import com.springboot.springboothousemarket.Controller.HousesController;
import com.springboot.springboothousemarket.Entity.HouseImage;
import com.springboot.springboothousemarket.Service.HouseImageService;
import com.springboot.springboothousemarket.dto.BusinessException;
import com.springboot.springboothousemarket.dto.ResponseResult;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.test.context.TestPropertySource;
import org.springframework.transaction.annotation.Transactional;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Comparator;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * 图片上传入口（BUG-3 / BUG-4）回归测试。
 *
 * <p>修复前的三个问题：
 * <ol>
 *   <li><b>通用上传只信扩展名</b>：内容为 HTML/脚本、名字叫 x.jpg 的文件可以直接入库，
 *       并通过 {@code /uploads/**} 静态映射对外提供；</li>
 *   <li><b>通用上传返回绝对 URL</b>：用 {@code fromCurrentContextPath()} 拼地址，
 *       部署在 Nginx 反代之后时前端拿到的是内网地址，图片直接失效；</li>
 *   <li><b>房源图片数量无上限</b>：房东可无限次上传，撑爆磁盘与 house_image 表。</li>
 * </ol>
 */
@SpringBootTest
@Transactional
@TestPropertySource(properties = {"app.house.max-images=3", "upload.dir=target/test-uploads"})
class HouseImageUploadTest {

    private static final byte[] PNG_BYTES = {
            (byte) 0x89, 0x50, 0x4E, 0x47, 0x0D, 0x0A, 0x1A, 0x0A, 0x00, 0x00, 0x00, 0x0D};

    @Autowired
    private HouseImageService houseImageService;

    @Autowired
    private HousesController housesController;

    private Path uploadRoot;

    @BeforeEach
    void setUp() {
        uploadRoot = Paths.get("target", "test-uploads").toAbsolutePath().normalize();
        // 房源 1 的种子数据自带 3 张图片，先清空以便精确验证数量上限
        houseImageService.deleteByHouseId(1L);
    }

    @AfterEach
    void tearDown() {
        SecurityContextHolder.clearContext();
        deleteRecursively(uploadRoot);
    }

    private static MockMultipartFile pngFile(String originalFilename) {
        return new MockMultipartFile("image", originalFilename, "image/png", PNG_BYTES);
    }

    /**
     * 数量上限：第 4 张必须被拒绝。
     */
    @Test
    void houseImageCountLimitIsEnforced() throws IOException {
        assertEquals(0, houseImageService.listByHouseId(1L).size(), "前置条件：房源 1 初始无图片");

        HouseImage first = houseImageService.uploadImage(1L, pngFile("a.png"), "OTHER", null, false);
        HouseImage second = houseImageService.uploadImage(1L, pngFile("b.png"), "OTHER", null, false);
        HouseImage third = houseImageService.uploadImage(1L, pngFile("c.png"), "OTHER", null, false);
        assertNotNull(first.getId());
        assertNotNull(second.getId());
        assertNotNull(third.getId());

        // 第 4 张必须被拒绝：否则房东可以无限上传，撑爆磁盘与 house_image 表
        BusinessException ex = assertThrows(BusinessException.class,
                () -> houseImageService.uploadImage(1L, pngFile("d.png"), "OTHER", null, false));
        assertTrue(ex.getMessage().contains("图片"), "实际：" + ex.getMessage());

        assertEquals(3, houseImageService.listByHouseId(1L).size());
    }

    /**
     * 服务端必须生成随机文件名：不能把用户提供的文件名落盘（同名覆盖 / 路径穿越 / 超长名）。
     */
    @Test
    void uploadedFileNameIsGeneratedOnServerSide() throws IOException {
        HouseImage image = houseImageService.uploadImage(
                1L, pngFile("../../../../etc/passwd.png"), "OTHER", null, false);

        String url = image.getImageUrl();
        assertNotNull(url);
        assertTrue(url.startsWith("/uploads/houses/1/"), "应返回相对路径，实际：" + url);
        assertFalse(url.contains(".."), "文件名不得包含路径穿越片段，实际：" + url);
        assertFalse(url.contains("passwd"), "不得使用用户提供的文件名，实际：" + url);

        String fileName = url.substring(url.lastIndexOf('/') + 1);
        assertTrue(fileName.matches("[0-9a-f]{32}\\.png"),
                "应为服务端生成的 UUID 文件名，实际：" + fileName);

        // 文件确实落到了 upload.dir 下
        Path target = uploadRoot.resolve("houses").resolve("1").resolve(fileName);
        assertTrue(Files.exists(target), "图片文件应已落盘：" + target);
    }

    /**
     * 通用上传入口：伪造内容（HTML 伪装成 jpg）必须被拒绝。
     */
    @Test
    void genericUploadRejectsForgedContent() {
        authenticateAsLandlord();
        byte[] html = "<html><script>alert(document.cookie)</script></html>"
                .getBytes(java.nio.charset.StandardCharsets.UTF_8);
        MockMultipartFile forged = new MockMultipartFile("image", "x.jpg", "image/jpeg", html);

        BusinessException ex = assertThrows(BusinessException.class,
                () -> housesController.uploadImage(forged, null, null));
        assertTrue(ex.getMessage().contains("格式"), "实际：" + ex.getMessage());
    }

    /**
     * 通用上传入口：返回相对路径，不能是绝对 URL。
     * 旧实现用 {@code fromCurrentContextPath()} 拼绝对地址，
     * 部署在 Nginx 反代之后时前端拿到的是内网地址，图片无法访问。
     */
    @Test
    void genericUploadReturnsRelativeUrl() throws IOException {
        authenticateAsLandlord();
        ResponseResult result = housesController.uploadImage(pngFile("photo.png"), null, null);

        assertTrue(result.isSuccess());
        Object data = result.getData();
        assertTrue(data instanceof Map, "data 应为 Map，实际：" + data);
        String url = String.valueOf(((Map<?, ?>) data).get("url"));

        assertNotNull(url);
        assertTrue(url.startsWith("/uploads/"), "应返回相对路径，实际：" + url);
        assertFalse(url.contains("http://"), "不得返回绝对 URL，实际：" + url);
        assertFalse(url.contains("https://"), "不得返回绝对 URL，实际：" + url);
    }

    /**
     * 通用上传入口：没有实际内容时也必须被拒绝（旧实现对无扩展名文件直接跳过校验）。
     */
    @Test
    void genericUploadRejectsEmptyFile() {
        authenticateAsLandlord();
        MockMultipartFile empty = new MockMultipartFile("image", "x.png", "image/png", new byte[0]);
        assertThrows(BusinessException.class, () -> housesController.uploadImage(empty, null, null));
    }

    private void authenticateAsLandlord() {
        SecurityContextHolder.getContext().setAuthentication(
                new UsernamePasswordAuthenticationToken("landlord1", null,
                        List.of(new SimpleGrantedAuthority("LANDLORD"))));
    }

    private static void deleteRecursively(Path root) {
        if (root == null || !Files.exists(root)) {
            return;
        }
        try (var stream = Files.walk(root)) {
            stream.sorted(Comparator.reverseOrder()).forEach(path -> {
                try {
                    Files.deleteIfExists(path);
                } catch (IOException ignored) {
                    // 清理失败不影响测试结论
                }
            });
        } catch (IOException ignored) {
            // 清理失败不影响测试结论
        }
    }
}