package com.restaurant.modules.user.dto.request;

import com.restaurant.modules.user.enums.UserStatus;
import jakarta.validation.constraints.NotNull;

/** @param status {@code LOCKED} khóa tài khoản, {@code ACTIVE} mở khóa */
public record UserStatusUpdateRequest(@NotNull(message = "Trạng thái không được để trống") UserStatus status) {
}
