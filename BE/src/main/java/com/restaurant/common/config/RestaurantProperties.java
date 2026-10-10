package com.restaurant.common.config;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.context.properties.bind.DefaultValue;
import org.springframework.validation.annotation.Validated;

import java.math.BigDecimal;
import java.time.Duration;
import java.time.LocalTime;

/**
 * Quy tắc vận hành nhà hàng đọc từ {@code app.restaurant.*}; mọi giá trị đều có mặc định nên cấu hình có thể bỏ trống.
 * Dùng chung cho đặt bàn (giờ mở cửa, thời lượng giữ bàn) và thanh toán (VAT, báo cáo).
 *
 * @param openTime               giờ mở cửa; giờ hẹn phải từ mốc này
 * @param closeTime              giờ đóng cửa; giờ hẹn phải trước mốc này
 * @param reservationDuration    một lượt giữ bàn; {@code reserved_end = reserved_at + duration}
 * @param reservationHoldBefore  còn nhiều nhất khoảng này tới giờ hẹn thì bàn chuyển sang "Đã đặt"
 * @param noShowGrace            quá giờ hẹn khoảng này mà chưa nhận bàn thì tính là không đến
 * @param bookingMinLead         đặt trực tuyến phải trước giờ hẹn tối thiểu khoảng này
 * @param bookingMaxAheadDays    đặt trước tối đa số ngày này
 * @param customerCancelMinHours khách hàng chỉ hủy được khi còn nhiều hơn hoặc bằng số giờ này tới giờ hẹn
 * @param availabilitySuggestions số khung giờ gợi ý tối đa khi hết bàn
 * @param vatRate                tỷ lệ VAT áp trên tổng thành tiền, vd 0.08
 * @param reportMaxDays          khoảng thời gian tối đa của một báo cáo
 */
@Validated
@ConfigurationProperties(prefix = "app.restaurant")
public record RestaurantProperties(
        @DefaultValue("10:00") @NotNull LocalTime openTime,
        @DefaultValue("22:00") @NotNull LocalTime closeTime,
        @DefaultValue("120m") @NotNull Duration reservationDuration,
        @DefaultValue("60m") @NotNull Duration reservationHoldBefore,
        @DefaultValue("15m") @NotNull Duration noShowGrace,
        @DefaultValue("2h") @NotNull Duration bookingMinLead,
        @DefaultValue("30") @Min(1) int bookingMaxAheadDays,
        @DefaultValue("2") @Min(0) int customerCancelMinHours,
        @DefaultValue("4") @Min(0) @Max(12) int availabilitySuggestions,
        @DefaultValue("0.08") @NotNull BigDecimal vatRate,
        @DefaultValue("366") @Min(1) int reportMaxDays) {
}
