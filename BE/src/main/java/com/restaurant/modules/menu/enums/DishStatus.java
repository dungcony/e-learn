package com.restaurant.modules.menu.enums;

/**
 * Trạng thái bán của món. {@code OUT_OF_STOCK} vẫn hiện trong thực đơn nhưng không gọi được; {@code DISCONTINUED}
 * chỉ Quản lý thấy.
 */
public enum DishStatus {
    AVAILABLE, OUT_OF_STOCK, DISCONTINUED
}
