package com.springboot.springboothousemarket.Config;

import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;
import org.springframework.web.servlet.config.annotation.ViewControllerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@Configuration
public class WebMvcConfig implements WebMvcConfigurer {

    @Override
    public void addResourceHandlers(ResourceHandlerRegistry registry) {
        // 添加静态资源处理器
        registry.addResourceHandler("/Web/**")
                .addResourceLocations("classpath:/Web/");
        registry.addResourceHandler("/css/**")
                .addResourceLocations("classpath:/Web/HouseMarket/css/");
        registry.addResourceHandler("/js/**")
                .addResourceLocations("classpath:/Web/HouseMarket/");
        registry.addResourceHandler("/images/**")
                .addResourceLocations("classpath:/Web/HouseMarket/images/");
        registry.addResourceHandler("/HouseMarket/**")
                .addResourceLocations("classpath:/Web/HouseMarket/");
    }

    @Override
    public void addViewControllers(ViewControllerRegistry registry) {
        // 添加视图控制器
        registry.addViewController("/").setViewName("redirect:/Web/HouseMarket/login.html");
        registry.addViewController("/index.html").setViewName("redirect:/Web/HouseMarket/login.html");
    }
}