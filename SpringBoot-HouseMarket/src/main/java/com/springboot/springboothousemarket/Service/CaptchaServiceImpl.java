package com.springboot.springboothousemarket.Service;

import org.springframework.stereotype.Service;

import java.awt.*;
import java.awt.image.BufferedImage;
import java.util.Random;

@Service
public class CaptchaServiceImpl implements CaptchaService {

    @Override
    public BufferedImage createCaptcha() {
        int width = 120;
        int height = 40;
        BufferedImage image = new BufferedImage(width, height, BufferedImage.TYPE_INT_RGB);
        Graphics2D g = image.createGraphics();

        // 背景
        g.setColor(Color.LIGHT_GRAY);
        g.fillRect(0, 0, width, height);

        // 随机验证码
        String chars = "ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz0123456789";
        StringBuilder captchaText = new StringBuilder();
        Random random = new Random();
        for (int i = 0; i < 4; i++) {
            char c = chars.charAt(random.nextInt(chars.length()));
            captchaText.append(c);
        }

        // 绘制字符
        g.setFont(new Font("Arial", Font.BOLD, 30));
        g.setColor(Color.BLACK);
        g.drawString(captchaText.toString(), 20, 30);

        g.dispose();

        // TODO: 这里可以把 captchaText 存到 session 或 Redis，用于前端验证
        return image;
    }
}
