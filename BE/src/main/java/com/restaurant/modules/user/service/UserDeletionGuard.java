package com.restaurant.modules.user.service;

import java.util.UUID;

/**
 * Cổng chặn xóa tài khoản do module sở hữu dữ liệu liên quan implement (đặt bàn, đơn hàng, hóa đơn). {@code user} không thể
 * gọi ngược lên các module đó nên đảo chiều phụ thuộc: mỗi module đăng ký guard của mình và {@code UserValidator} gọi hết.
 */
@FunctionalInterface
public interface UserDeletionGuard {

    /**
     * @param userId tài khoản định xóa
     * @return {@code true} nếu module này còn dữ liệu khiến không được xóa tài khoản (khi đó chỉ được khóa)
     */
    boolean hasBlockingData(UUID userId);
}
