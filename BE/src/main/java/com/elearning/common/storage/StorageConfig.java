package com.elearning.common.storage;

import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

import java.nio.file.Path;

/**
 * Phục vụ ảnh đã tải lên qua {@code <publicPath>/**}; đường dẫn này được mở công khai trong {@code SecurityConfig}.
 */
@Configuration
@EnableConfigurationProperties(StorageProperties.class)
public class StorageConfig implements WebMvcConfigurer {

    private final StorageProperties properties;

    public StorageConfig(StorageProperties properties) {
        this.properties = properties;
    }

    @Override
    public void addResourceHandlers(ResourceHandlerRegistry registry) {
        String location = Path.of(properties.baseDir()).toAbsolutePath().normalize().toUri().toString();
        registry.addResourceHandler(properties.publicPath() + "/**").addResourceLocations(location);
    }
}
