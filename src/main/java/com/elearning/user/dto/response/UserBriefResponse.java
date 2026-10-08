package com.elearning.user.dto.response;

import java.util.UUID;

/**
 * Thông tin tối thiểu của một người dùng để module khác hiển thị tên (giảng viên của khóa học, tác giả bình luận...).
 */
public record UserBriefResponse(UUID id, String fullName, String email) {
}
