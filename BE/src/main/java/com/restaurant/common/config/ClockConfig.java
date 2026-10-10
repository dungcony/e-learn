package com.restaurant.common.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.time.Clock;
import java.time.ZoneId;

/**
 * {@link Clock} dùng chung cho mọi so sánh theo ngày (vd khóa học đã tới ngày bắt đầu chưa), để test thay được
 * đồng hồ và "hôm nay" tính theo múi giờ của người dùng thay vì múi giờ máy chủ.
 */
@Configuration
public class ClockConfig {

    @Bean
    public Clock clock(@Value("${app.timezone:Asia/Ho_Chi_Minh}") String zone) {
        return Clock.system(ZoneId.of(zone));
    }
}
