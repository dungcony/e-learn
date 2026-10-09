package com.elearning.common.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.annotation.EnableAsync;

/**
 * Bật {@code @Async}, nếu không các service gửi email gắn {@code @Async} vẫn chạy đồng bộ trong request.
 */
@Configuration
@EnableAsync
public class AsyncConfig {
}
