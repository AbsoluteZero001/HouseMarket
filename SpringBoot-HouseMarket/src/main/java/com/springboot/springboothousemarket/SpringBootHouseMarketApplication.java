package com.springboot.springboothousemarket;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

import java.io.IOException;

@SpringBootApplication
@MapperScan("com.springboot.springboothousemarket.Mapper")
public class SpringBootHouseMarketApplication {

    public static void main(String[] args) {
        SpringApplication.run(SpringBootHouseMarketApplication.class, args);
        // 自动打开浏览器并访问登录页面
        openBrowser("http://localhost:8082/login.html");
    }

    // 自动打开浏览器的方法
    private static void openBrowser(String url) {
        String os = System.getProperty("os.name").toLowerCase();

        try {
            if (os.contains("win")) {
                // Windows
                Runtime.getRuntime().exec("rundll32 url.dll,FileProtocolHandler " + url);
            } else if (os.contains("mac")) {
                // macOS
                Runtime.getRuntime().exec("open " + url);
            } else if (os.contains("nix") || os.contains("nux") || os.contains("mac")) {
                // Linux
                String[] browsers = {"google-chrome", "firefox", "chromium"};
                for (String browser : browsers) {
                    try {
                        Runtime.getRuntime().exec(new String[]{browser, url});
                        break; // 找到一个可用浏览器就跳出循环
                    } catch (IOException e) {
                        // 如果命令失败，尝试下一个浏览器
                        continue;
                    }
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
