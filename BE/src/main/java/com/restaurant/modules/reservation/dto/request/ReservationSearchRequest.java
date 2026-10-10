package com.restaurant.modules.reservation.dto.request;

import com.restaurant.modules.reservation.enums.ReservationStatus;

import java.time.LocalDate;

/**
 * Tiêu chí tìm đặt bàn (UC007, Bảng 2-16), nhận từ query string; kết hợp bằng AND.
 *
 * @param keyword tên khách hoặc số điện thoại chứa chuỗi, không phân biệt hoa thường
 * @param code    mã đặt bàn, khớp chính xác không phân biệt hoa thường
 * @param date    ngày hẹn theo múi giờ nhà hàng; không truyền {@code date}, {@code code} lẫn {@code keyword} thì mặc định hôm nay
 */
public record ReservationSearchRequest(String keyword, String code, LocalDate date, ReservationStatus status) {
}
