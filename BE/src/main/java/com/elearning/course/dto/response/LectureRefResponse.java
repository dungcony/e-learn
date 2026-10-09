package com.elearning.course.dto.response;

import java.util.UUID;

// Bài giảng rút gọn nhúng trong chi tiết khóa học: chỉ id và tên, nội dung bài giảng chỉ mở cho học viên đã ghi danh.
public record LectureRefResponse(UUID id, String title) {
}
