package com.easymart.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

import java.io.File;

@Configuration
public class WebConfig implements WebMvcConfigurer {

    @Value("${app.upload.dir}")
    private String uploadDir;

    @Value("${app.upload.user-dir}")
    private String userUploadDir;

    @Override
    public void addResourceHandlers(ResourceHandlerRegistry registry) {
        String uploadPath = new File(uploadDir).getAbsolutePath();
        String userUploadPath = new File(userUploadDir).getAbsolutePath();

        registry.addResourceHandler("/uploads/products/**")
                .addResourceLocations("file:" + uploadPath + File.separator);

        registry.addResourceHandler("/uploads/users/**")
                .addResourceLocations("file:" + userUploadPath + File.separator);
    }
}
