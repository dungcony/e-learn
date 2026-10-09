package com.elearning.course.dto.response;

import java.util.UUID;

// Giảng viên rút gọn nhúng trong chi tiết khóa học; không có email vì endpoint này công khai.
public record TeacherRefResponse(UUID id, String fullName) {
}
