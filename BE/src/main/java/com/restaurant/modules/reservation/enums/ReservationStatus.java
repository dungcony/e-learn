package com.restaurant.modules.reservation.enums;

/**
 * Vòng đời đặt bàn: chờ xác nhận → đã xác nhận → đã nhận bàn; nhánh phụ đã hủy (khách hoặc nhân viên) và không đến (quá giờ hẹn
 * mà chưa nhận bàn).
 */
public enum ReservationStatus {
    PENDING, CONFIRMED, CHECKED_IN, CANCELLED, NO_SHOW
}
