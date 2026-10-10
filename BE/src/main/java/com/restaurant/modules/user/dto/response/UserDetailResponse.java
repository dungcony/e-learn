package com.restaurant.modules.user.dto.response;

import com.restaurant.modules.user.enums.Gender;
import com.restaurant.modules.user.enums.Role;
import com.restaurant.modules.user.enums.UserStatus;

import java.time.Instant;
import java.time.LocalDate;
import java.util.UUID;

/** Chi tiết một tài khoản cho Quản lý; không chứa băm mật khẩu. */
public record UserDetailResponse(
        UUID id,
        String email,
        String fullName,
        String phone,
        Gender gender,
        LocalDate dateOfBirth,
        String avatarUrl,
        Role role,
        UserStatus status,
        boolean emailVerified,
        boolean mustChangePassword,
        Instant createdAt) {
}
