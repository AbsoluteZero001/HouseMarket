package com.springboot.springboothousemarket;

import com.springboot.springboothousemarket.Util.ImageUploadValidator;
import com.springboot.springboothousemarket.dto.BusinessException;
import org.junit.jupiter.api.Test;
import org.springframework.mock.web.MockMultipartFile;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * 图片上传统一校验器（{@link ImageUploadValidator}）的单元测试。
 *
 * <p>该类修复了三个真实缺陷：
 * <ol>
 *   <li><b>JPEG 永远识别不出来</b>：旧代码用 {@code bytes[1] == 0xD8} 比较，
 *       而 0xD8 以有符号 byte 存为 -40，条件恒为 false，所有 JPEG 都被判成「格式不支持」；</li>
 *   <li><b>只信扩展名</b>：内容是 HTML/脚本、文件名是 x.jpg 的文件可以直接入库，
 *       再通过 {@code /uploads/**} 静态映射对外提供；</li>
 *   <li><b>文件名完全由用户控制</b>：可造成同名覆盖、路径穿越与超长文件名。</li>
 * </ol>
 */
class ImageUploadValidatorTest {

    private static final byte[] PNG_BYTES = {
            (byte) 0x89, 0x50, 0x4E, 0x47, 0x0D, 0x0A, 0x1A, 0x0A, 0x00, 0x00, 0x00, 0x0D};
    private static final byte[] JPEG_BYTES = {
            (byte) 0xFF, (byte) 0xD8, (byte) 0xFF, (byte) 0xE0, 0x00, 0x10, 'J', 'F', 'I', 'F', 0x00};
    private static final byte[] GIF_BYTES = {
            'G', 'I', 'F', '8', '9', 'a', 0x01, 0x00, 0x01, 0x00, (byte) 0x80, 0x00};
    private static final byte[] WEBP_BYTES = {
            'R', 'I', 'F', 'F', 0x24, 0x00, 0x00, 0x00, 'W', 'E', 'B', 'P'};

    private static MockMultipartFile file(String originalFilename, String contentType, byte[] content) {
        return new MockMultipartFile("image", originalFilename, contentType, content);
    }

    @Test
    void detectExtensionRecognizesAllSupportedFormats() {
        assertEquals(".png", ImageUploadValidator.detectExtension(PNG_BYTES));
        assertEquals(".jpg", ImageUploadValidator.detectExtension(JPEG_BYTES));
        assertEquals(".gif", ImageUploadValidator.detectExtension(GIF_BYTES));
        assertEquals(".webp", ImageUploadValidator.detectExtension(WEBP_BYTES));
    }

    /**
     * 回归：JPEG 的 magic number 分支在旧实现里从未命中过。
     * 旧代码写的是 {@code bytes[1] == 0xD8}，而 0xD8 以有符号 byte 存为 -40，
     * 比较恒为 false —— 合法 JPEG 会被误判成「仅支持 JPG/PNG/GIF/WEBP 格式」。
     */
    @Test
    void jpegMagicNumberIsDetectedCorrectly() {
        assertEquals(".jpg", ImageUploadValidator.detectExtension(
                new byte[]{(byte) 0xFF, (byte) 0xD8, (byte) 0xFF}));
        assertEquals(".jpg", ImageUploadValidator.validateAndDetectExtension(
                file("photo.jpg", "image/jpeg", JPEG_BYTES), "图片"));
    }

    /**
     * 回归：不能只信用户提供的文件名。
     * 内容是 HTML/脚本、名字叫 evil.jpg 的文件必须被拒绝，
     * 否则它会通过 {@code /uploads/**} 静态映射变成可被浏览器执行的页面。
     */
    @Test
    void fakeHtmlWithImageExtensionIsRejected() {
        byte[] html = "<html><script>alert(1)</script></html>".getBytes(java.nio.charset.StandardCharsets.UTF_8);
        BusinessException ex = assertThrows(BusinessException.class,
                () -> ImageUploadValidator.validateAndDetectExtension(file("evil.jpg", "image/jpeg", html), "图片"));
        assertTrue(ex.getMessage().contains("格式"), "应提示格式不支持，实际：" + ex.getMessage());
    }

    /**
     * 回归：旧实现对「无扩展名」的文件直接跳过整个校验环节，等于放行任意内容。
     * 现在文件名完全不参与判定 —— 没有扩展名的文件照样按真实字节校验，
     * 内容不是图片就必须被拒绝。
     */
    @Test
    void fileWithoutExtensionStillGoesThroughContentCheck() {
        BusinessException ex = assertThrows(BusinessException.class,
                () -> ImageUploadValidator.validateAndDetectExtension(
                        file("no-extension", "application/octet-stream", "not an image".getBytes(
                                java.nio.charset.StandardCharsets.UTF_8)), "图片"));
        assertTrue(ex.getMessage().contains("格式"), "应提示格式不支持，实际：" + ex.getMessage());
    }

    /**
     * 文件名不参与判定：没有扩展名、但内容是合法 PNG 时应被接受，
     * 并由服务端补上正确的扩展名。
     */
    @Test
    void fileWithoutExtensionIsAcceptedWhenContentIsValidImage() {
        assertEquals(".png", ImageUploadValidator.validateAndDetectExtension(
                file("no-extension", "application/octet-stream", PNG_BYTES), "图片"));
    }

    @Test
    void emptyFileIsRejected() {
        BusinessException ex = assertThrows(BusinessException.class,
                () -> ImageUploadValidator.validateAndDetectExtension(
                        file("empty.png", "image/png", new byte[0]), "图片"));
        assertTrue(ex.getMessage().contains("不能为空"), "实际：" + ex.getMessage());
    }

    @Test
    void nullFileIsRejected() {
        assertThrows(BusinessException.class,
                () -> ImageUploadValidator.validateAndDetectExtension(null, "图片"));
    }

    @Test
    void oversizedFileIsRejected() {
        long limit = 1024L;
        byte[] tooBig = new byte[(int) limit + 1];
        System.arraycopy(PNG_BYTES, 0, tooBig, 0, PNG_BYTES.length);
        BusinessException ex = assertThrows(BusinessException.class,
                () -> ImageUploadValidator.validateAndDetectExtension(file("big.png", "image/png", tooBig), limit, "图片"));
        assertTrue(ex.getMessage().contains("大小"), "实际：" + ex.getMessage());
    }

    /**
     * 扩展名必须按「真实字节」判定，而不是按用户提供的文件名判定。
     */
    @Test
    void extensionFollowsRealContentNotFileName() {
        assertEquals(".jpg", ImageUploadValidator.validateAndDetectExtension(
                file("lie.png", "image/png", JPEG_BYTES), "图片"));
        assertEquals(".png", ImageUploadValidator.validateAndDetectExtension(
                file("lie.jpg", "image/jpeg", PNG_BYTES), "图片"));
    }

    @Test
    void randomFileNameKeepsExtensionAndIsUnique() {
        String first = ImageUploadValidator.randomFileName(".png");
        String second = ImageUploadValidator.randomFileName(".png");

        assertTrue(first.endsWith(".png"), "应保留扩展名，实际：" + first);
        assertNotEquals(first, second, "随机文件名不应重复");
        assertTrue(first.matches("[0-9a-f]{32}\\.png"), "应为 UUID 形式的服务端文件名，实际：" + first);
    }

    @Test
    void unsupportedContentReturnsNullExtension() {
        assertNull(ImageUploadValidator.detectExtension(new byte[]{0x50, 0x4B, 0x03, 0x04}));
        assertNull(ImageUploadValidator.detectExtension(null));
        assertNull(ImageUploadValidator.detectExtension(new byte[0]));
    }
}