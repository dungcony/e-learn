package com.elearning.user.dto.request;

import com.elearning.user.enums.UserStatus;
import jakarta.validation.constraints.NotNull;

/**
 * Khóa hoặc mở khóa học viên (UC010).
 *
 * @param status {@code LOCKED} để khóa, {@code ACTIVE} để mở khóa
 */
public record UserStatusUpdateRequest(
        @NotNull(message = "Trạng thái không được để trống") UserStatus status
) {
}
