package com.mall.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

import java.io.File;

/**
 * Web MVC 配置
 */
@Configuration
public class WebMvcConfig implements WebMvcConfigurer {

    @Value("${file.upload-path}")
    private String uploadPath;

    /**
     * 配置静态资源映射，使上传的文件可以通过URL访问
     */
    @Override
    public void addResourceHandlers(ResourceHandlerRegistry registry) {
        // 确保路径以 / 结尾
        String path = uploadPath.replace("\\", "/");
        if (!path.endsWith("/")) {
            path = path + "/";
        }
        // 将 /uploads/** 映射到本地上传目录
        registry.addResourceHandler("/uploads/**")
                .addResourceLocations("file:" + path);
    }
}
