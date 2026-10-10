package com.restaurant.modules.order.enums;

/**
 * Vòng đời một dòng món: gửi bếp ({@code PENDING}) → bếp nhận ({@code COOKING}) → bếp xong ({@code READY}) → đã phục vụ
 * ({@code SERVED}). {@code CANCELLED} có sẵn cho tương lai nhưng bản này không có API nào tạo ra.
 */
public enum OrderItemStatus {
    PENDING, COOKING, READY, SERVED, CANCELLED
}
