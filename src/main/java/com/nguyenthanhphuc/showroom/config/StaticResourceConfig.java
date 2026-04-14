package com.nguyenthanhphuc.showroom.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.CacheControl;
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

import java.nio.file.Path;

@Configuration
public class StaticResourceConfig implements WebMvcConfigurer {
    private final String uploadsFolder;

    public StaticResourceConfig(@Value("${uploadsFolder}") String uploadsFolder) {
        this.uploadsFolder = uploadsFolder;
    }

    @Override
    public void addResourceHandlers(ResourceHandlerRegistry registry) {
        registry
                .addResourceHandler("/files/**")
                .addResourceLocations(Path.of(uploadsFolder).toUri().toString())
                .setCacheControl(CacheControl.noCache());
    }
}
