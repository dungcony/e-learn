package com.restaurant.modules.reservation.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.annotation.EnableScheduling;

/** Bật job nền của đặt bàn (giữ bàn khi gần giờ, đánh dấu không đến). Giả định chạy một instance backend. */
@Configuration
@EnableScheduling
public class SchedulingConfig {
}
