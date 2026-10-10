package com.restaurant.modules.user.dto.response;

import com.restaurant.modules.user.enums.Role;

import java.util.UUID;

/** Tên và liên hệ tối thiểu để module khác hiển thị người thực hiện (nhân viên phục vụ, thu ngân), kể cả tài khoản đã xóa. */
public record UserBriefResponse(UUID id, String fullName, String email, String phone, Role role) {
}
