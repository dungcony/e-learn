package com.elearning.user.dto.response;

import com.elearning.user.enums.Gender;
import com.elearning.user.enums.Role;
import com.elearning.user.enums.UserStatus;

import java.time.Instant;
import java.time.LocalDate;
import java.util.UUID;

/**
 * Chi tiết một giảng viên hoặc học viên cho Quản trị viên.
 */
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
        Instant createdAt
) {
}
