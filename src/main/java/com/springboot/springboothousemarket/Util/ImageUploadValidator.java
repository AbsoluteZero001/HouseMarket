package com.springboot.springboothousemarket.Util;

import com.springboot.springboothousemarket.dto.BusinessException;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.io.InputStream;
import java.util.Locale;
import java.util.UUID;

/**
 * 图片上传统一校验器。
 *
 * 背景：本项目有三条图片上传入口（头像、房源图片、通用图片），
 * 校验标准却各不相同：
 * <ul>
 *   <li>头像：校验 magic number + 2MB 上限 + UUID 命名；</li>
 *   <li>房源图片：只校验扩展名白名单 + 5MB 上限；</li>
 *   <li>通用图片：只校验扩展名白名单（且无扩展名时直接跳过校验）、无大小上限、文件名由用户可控。</li>
 * </ul>
 * 由此产生两个实际缺陷：
 * <ol>
 *   <li><b>内容可伪造</b>：只信扩展名，一个内容为 HTML/脚本、名字叫 x.jpg 的文件可以直接入库并通过
 *       {@code /uploads/**} 静态映射对外提供；</li>
 *   <li><b>反代下图片失效</b>：通用图片入口用 {@code fromCurrentContextPath()} 拼绝对 URL，
 *       应用部署在 Nginx 之后时拿到的是内网地址，前端渲染出无法访问的图片。</li>
 * </ol>
 * 这里统一为：<b>magic number 判定真实类型 → 决定扩展名</b> + 大小上限 + 服务端随机文件名 + 相对路径。
 *
 * <p>注意：字节比较必须统一用 {@code (b & 0xFF)} 转成无符号数。
 * 老代码里 {@code bytes[1] == 0xD8} 这种写法对有符号 byte 恒不成立
 * （0xD8 存为 -40），导致 JPEG 的 magic number 分支从未命中过。
 */
public final class ImageUploadValidator {

    /** 默认大小上限：5MB。 */
    public static final long DEFAULT_MAX_BYTES = 5L * 1024 * 1024;

    /** 探测 magic number 所需的最大字节数。 */
    private static final int HEADER_SIZE = 12;

    private ImageUploadValidator() {
    }

    /**
     * 校验图片并返回「按真实字节判定」的扩展名（含点）。
     *
     * @param file     上传文件
     * @param maxBytes 大小上限
     * @param label    业务名称（用于错误文案，如「头像」「房源图片」）
     * @return 形如 {@code .jpg} 的扩展名
     * @throws BusinessException 文件为空、超限或格式不支持
     */
    public static String validateAndDetectExtension(MultipartFile file, long maxBytes, String label) {
        if (file == null || file.isEmpty()) {
            throw new BusinessException(label + "不能为空");
        }
        if (file.getSize() > maxBytes) {
            throw new BusinessException(label + "大小不能超过 " + formatMb(maxBytes));
        }
        byte[] header = readHeader(file);
        String extension = detectExtension(header);
        if (extension == null) {
            throw new BusinessException(label + "仅支持 JPG/PNG/GIF/WEBP 格式");
        }
        return extension;
    }

    /** 便捷方法：使用默认 5MB 上限。 */
    public static String validateAndDetectExtension(MultipartFile file, String label) {
        return validateAndDetectExtension(file, DEFAULT_MAX_BYTES, label);
    }

    /**
     * 读取文件头部字节用于类型探测。只读前 12 字节，避免整文件读入内存。
     */
    private static byte[] readHeader(MultipartFile file) {
        try (InputStream in = file.getInputStream()) {
            byte[] buffer = new byte[HEADER_SIZE];
            int read = in.read(buffer, 0, HEADER_SIZE);
            if (read <= 0) {
                return new byte[0];
            }
            if (read == HEADER_SIZE) {
                return buffer;
            }
            byte[] exact = new byte[read];
            System.arraycopy(buffer, 0, exact, 0, read);
            return exact;
        } catch (IOException e) {
            throw new BusinessException("文件读取失败，请重试");
        }
    }

    /**
     * 按 magic number 判定图片类型。
     *
     * @return 扩展名（含点）；无法识别时返回 null
     */
    public static String detectExtension(byte[] header) {
        if (header == null) {
            return null;
        }
        // PNG: 89 50 4E 47
        if (header.length >= 8
                && u(header[0]) == 0x89 && u(header[1]) == 0x50 && u(header[2]) == 0x4E && u(header[3]) == 0x47) {
            return ".png";
        }
        // JPEG: FF D8 FF
        if (header.length >= 3
                && u(header[0]) == 0xFF && u(header[1]) == 0xD8 && u(header[2]) == 0xFF) {
            return ".jpg";
        }
        // GIF: "GIF"
        if (header.length >= 3
                && u(header[0]) == 'G' && u(header[1]) == 'I' && u(header[2]) == 'F') {
            return ".gif";
        }
        // WEBP: "RIFF" .... "WEBP"
        if (header.length >= 12
                && u(header[0]) == 'R' && u(header[1]) == 'I' && u(header[2]) == 'F' && u(header[3]) == 'F'
                && u(header[8]) == 'W' && u(header[9]) == 'E' && u(header[10]) == 'B' && u(header[11]) == 'P') {
            return ".webp";
        }
        return null;
    }

    /**
     * 生成服务端随机文件名，杜绝用户可控文件名带来的路径穿越、同名覆盖与超长名。
     */
    public static String randomFileName(String extension) {
        String ext = extension == null ? "" : extension.toLowerCase(Locale.ROOT);
        return UUID.randomUUID().toString().replace("-", "") + ext;
    }

    private static int u(byte b) {
        return b & 0xFF;
    }

    private static String formatMb(long maxBytes) {
        long mb = maxBytes / 1024 / 1024;
        return mb <= 0 ? (maxBytes / 1024) + "KB" : mb + "MB";
    }
}