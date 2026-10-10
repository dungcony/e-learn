package com.restaurant.modules.user.dto.response;

import com.restaurant.modules.user.enums.Role;
import com.restaurant.modules.user.enums.UserStatus;

import java.util.UUID;

/** Một dòng trong danh sách tài khoản của Quản lý. */
public record UserSummaryResponse(
        UUID id,
        String email,
        String fullName,
        String phone,
        Role role,
        UserStatus status,
        boolean emailVerified) {
}
