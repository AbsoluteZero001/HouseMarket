package com.springboot.springboothousemarket.Config;

import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;
import org.springframework.web.servlet.config.annotation.ViewControllerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@Configuration
public class WebMvcConfig implements WebMvcConfigurer {

    @Override
    public void addResourceHandlers(ResourceHandlerRegistry registry) {
        // 移除了静态资源处理器配置，因为要改为纯后端API模式
    }

    @Override
    public void addViewControllers(ViewControllerRegistry registry) {
        // 移除了视图控制器配置，因为要改为纯后端API模式
    }
}