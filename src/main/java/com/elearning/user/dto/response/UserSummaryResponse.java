package com.elearning.user.dto.response;

import com.elearning.user.enums.Gender;
import com.elearning.user.enums.UserStatus;

import java.util.UUID;

/**
 * Một dòng trong danh sách giảng viên, học viên của Quản trị viên.
 */
public record UserSummaryResponse(
        UUID id,
        String email,
        String fullName,
        String phone,
        Gender gender,
        UserStatus status
) {
}
