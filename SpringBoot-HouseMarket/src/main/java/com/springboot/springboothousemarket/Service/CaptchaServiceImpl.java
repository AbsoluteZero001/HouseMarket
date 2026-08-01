package com.springboot.springboothousemarket.Service;

import com.springboot.springboothousemarket.dto.CaptchaResult;
import org.springframework.stereotype.Service;

import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.time.LocalDateTime;
import java.util.Map;
import java.util.Random;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

@Service
public class CaptchaServiceImpl implements CaptchaService {

    private static final int EXPIRE_MINUTES = 5;
    private static final String CHARS = "ABCDEFGHJKLMNPQRSTUVWXYZabcdefghjkmnpqrstuvwxyz23456789";

    private final Map<String, CaptchaEntry> captchaStore = new ConcurrentHashMap<>();
    private final Random random = new Random();

    @Override
    public CaptchaResult generate() {
        String captchaId = UUID.randomUUID().toString().replace("-", "");
        String code = randomCode();
        captchaStore.put(captchaId, new CaptchaEntry(code, LocalDateTime.now().plusMinutes(EXPIRE_MINUTES)));

        String base64 = encodeImage(createImage(code));
        return new CaptchaResult(captchaId, base64);
    }

    @Override
    public boolean verify(String captchaId, String captchaCode) {
        if (captchaId == null || captchaCode == null) {
            return false;
        }
        CaptchaEntry entry = captchaStore.remove(captchaId);
        if (entry == null || entry.expiresAt.isBefore(LocalDateTime.now())) {
            return false;
        }
        return entry.code.equalsIgnoreCase(captchaCode.trim());
    }

    private String randomCode() {
        StringBuilder code = new StringBuilder();
        for (int i = 0; i < 4; i++) {
            code.append(CHARS.charAt(random.nextInt(CHARS.length())));
        }
        return code.toString();
    }

    private BufferedImage createImage(String code) {
        int width = 120;
        int height = 42;
        BufferedImage image = new BufferedImage(width, height, BufferedImage.TYPE_INT_RGB);
        Graphics2D g = image.createGraphics();
        g.setColor(Color.WHITE);
        g.fillRect(0, 0, width, height);

        for (int i = 0; i < 3; i++) {
            g.setColor(new Color(150 + random.nextInt(80), 150 + random.nextInt(80), 150 + random.nextInt(80)));
            g.drawLine(0, random.nextInt(height), width, random.nextInt(height));
        }

        g.setFont(new Font("Arial", Font.BOLD, 26));
        for (int i = 0; i < code.length(); i++) {
            g.setColor(new Color(30 + random.nextInt(120), 60 + random.nextInt(100), 120 + random.nextInt(100)));
            g.drawString(String.valueOf(code.charAt(i)), 14 + i * 26, 30 + random.nextInt(5));
        }

        g.dispose();
        return image;
    }

    private String encodeImage(BufferedImage image) {
        try (ByteArrayOutputStream output = new ByteArrayOutputStream()) {
            ImageIO.write(image, "png", output);
            return java.util.Base64.getEncoder().encodeToString(output.toByteArray());
        } catch (IOException e) {
            throw new IllegalStateException("验证码图片生成失败", e);
        }
    }

    private record CaptchaEntry(String code, LocalDateTime expiresAt) {
    }
}
