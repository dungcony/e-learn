package com.restaurant.common.code;

import lombok.RequiredArgsConstructor;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Component;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;

/**
 * Sinh mã đọc được theo ngày cho đặt bàn ({@code DB20261009-001}) và hóa đơn ({@code HD20261009-015}).
 * Số thứ tự tăng bằng một câu {@code INSERT ... ON CONFLICT DO UPDATE ... RETURNING} nên hai request đồng thời không bao giờ
 * nhận cùng số; số bị bỏ trống khi transaction rollback là chấp nhận được.
 */
@Component
@RequiredArgsConstructor
public class DailyCodeGenerator {

    private static final DateTimeFormatter DAY_FORMAT = DateTimeFormatter.BASIC_ISO_DATE;

    private final JdbcTemplate jdbcTemplate;

    /**
     * @param prefix tiền tố mã, vd {@code DB} hoặc {@code HD}
     * @param day    ngày theo múi giờ nhà hàng
     * @return mã dạng {@code <prefix><yyyyMMdd>-<số thứ tự 3 chữ số>}; quá 999 thì mở rộng thành nhiều chữ số hơn
     */
    public String next(String prefix, LocalDate day) {
        Integer sequence = jdbcTemplate.queryForObject("""
                INSERT INTO daily_code_counters (prefix, day, last_value) VALUES (?, ?, 1)
                ON CONFLICT (prefix, day) DO UPDATE SET last_value = daily_code_counters.last_value + 1
                RETURNING last_value
                """, Integer.class, prefix, day);
        return "%s%s-%03d".formatted(prefix, DAY_FORMAT.format(day), sequence);
    }
}
