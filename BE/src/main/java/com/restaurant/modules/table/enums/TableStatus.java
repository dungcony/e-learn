package com.restaurant.modules.table.enums;

/**
 * Trạng thái bàn. Quản lý chỉ đặt tay {@code AVAILABLE} và {@code OUT_OF_SERVICE}; {@code RESERVED} (còn không quá 60 phút
 * tới giờ hẹn) và {@code OCCUPIED} (đang phục vụ) do hệ thống cập nhật.
 */
public enum TableStatus {
    AVAILABLE, RESERVED, OCCUPIED, OUT_OF_SERVICE
}
