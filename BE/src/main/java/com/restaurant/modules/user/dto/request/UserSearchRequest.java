package com.restaurant.modules.user.dto.request;

import com.restaurant.modules.user.enums.Role;
import com.restaurant.modules.user.enums.UserStatus;
import org.springframework.web.bind.annotation.BindParam;

/**
 * Tiêu chí tìm tài khoản của Quản lý (UC006), nhận từ query string; mọi tiêu chí đều tùy chọn và kết hợp bằng AND.
 *
 * @param name          họ tên chứa chuỗi, không phân biệt hoa thường
 * @param email         email chứa chuỗi
 * @param phone         số điện thoại chứa chuỗi
 * @param role          chỉ dùng khi tìm nhân viên; {@code CUSTOMER} bị từ chối
 * @param status        {@code ACTIVE} (Unlocked) hoặc {@code LOCKED}
 * @param emailVerified chỉ dùng khi tìm khách hàng, để lọc tài khoản chưa xác thực email
 */
public record UserSearchRequest(
        String name,
        String email,
        String phone,
        Role role,
        UserStatus status,
        @BindParam("email_verified") Boolean emailVerified) {
}
