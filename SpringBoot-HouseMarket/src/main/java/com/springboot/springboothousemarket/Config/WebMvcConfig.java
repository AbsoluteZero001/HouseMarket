package com.springboot.springboothousemarket.Config;

import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;
import org.springframework.web.servlet.config.annotation.ViewControllerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;


/**
 * WebMvcConfig类
 * 用于配置Spring MVC的相关设置
 * 实现WebMvcConfigurer接口以自定义MVC配置
 * 当前配置为纯后端API模式，不处理视图和静态资源
 */
@Configuration  // 标记此类为配置类，相当于XML配置文件中的<beans>
public class WebMvcConfig implements WebMvcConfigurer {

    /**
     * 添加资源处理器
     *
     * @param registry 资源处理器注册表
     *                 <p>
     *                 此方法已被注释，因为项目采用纯后端API模式，
     *                 不需要处理静态资源请求
     */
    @Override
    public void addResourceHandlers(ResourceHandlerRegistry registry) {
        // 移除了静态资源处理器配置，因为要改为纯后端API模式
    }

    /**
     * 添加视图控制器
     * @param registry 视图控制器注册表
     *
     * 此方法已被注释，因为项目采用纯后端API模式，
     * 不需要处理页面视图请求
     */
    @Override
    public void addViewControllers(ViewControllerRegistry registry) {
        // 移除了视图控制器配置，因为要改为纯后端API模式
    }
}