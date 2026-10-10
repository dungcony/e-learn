package com.restaurant.modules.menu.service;

import java.util.UUID;

/**
 * Cổng chặn xóa món do module có dữ liệu liên quan (đơn hàng) implement; {@code menu} không được gọi ngược lên module đó.
 */
@FunctionalInterface
public interface DishDeletionGuard {

    /**
     * @return {@code true} nếu món đã từng được gọi (khi đó chỉ chuyển sang ngừng bán, không xóa được)
     */
    boolean isUsed(UUID dishId);
}
