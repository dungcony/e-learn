package com.restaurant.modules.user.dto.response;

import com.restaurant.modules.user.enums.Role;

import java.util.UUID;

/**
 * Thông tin tối thiểu của người dùng trong phản hồi đăng nhập.
 *
 * @param role               client dùng để hiện menu đúng vai trò
 * @param mustChangePassword {@code true} khi nhân viên còn dùng mật khẩu Quản lý đặt; client điều hướng sang đổi mật khẩu
 */
public record AuthUserResponse(UUID id, String email, String fullName, Role role, boolean mustChangePassword) {
}
