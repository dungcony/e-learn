package com.restaurant.modules.user.repository;

import java.util.UUID;

/** Dòng kết quả của truy vấn tên người dùng kể cả tài khoản đã xóa mềm. */
public interface UserBriefProjection {

    UUID getId();

    String getFullName();

    String getEmail();

    String getPhone();

    String getRole();
}
