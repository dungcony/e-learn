package com.elearning.user.dto.response;

import com.elearning.user.enums.Gender;
import com.elearning.user.enums.Role;

import java.time.LocalDate;
import java.util.UUID;

/**
 * Thông tin cá nhân của chính người đăng nhập (UC004, UC005).
 */
public record ProfileResponse(
        UUID id,
        String email,
        String fullName,
        String phone,
        Gender gender,
        LocalDate dateOfBirth,
        String avatarUrl,
        Role role
) {
}
