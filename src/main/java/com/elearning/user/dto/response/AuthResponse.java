package com.elearning.user.dto.response;

import com.elearning.user.enums.Role;

import java.util.UUID;

/**
 * Kết quả đăng nhập.
 *
 * @param accessToken JWT gửi lại trong header {@code Authorization: Bearer ...}
 * @param userId      id tài khoản
 * @param email       email tài khoản
 * @param role        vai trò, để client hiển thị chức năng tương ứng
 */
public record AuthResponse(String accessToken, UUID userId, String email, Role role) {
}
