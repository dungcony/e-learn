package com.elearning.user.repository;

import java.util.UUID;

// Projection của truy vấn native đọc cả tài khoản đã xóa mềm, để module khác vẫn hiển thị được tên người cũ.
public interface UserBriefView {

    UUID getId();

    String getFullName();

    String getEmail();
}
