package com.elearning.course.dto.response;

import java.util.List;
import java.util.UUID;

/**
 * Id các bài giảng còn tồn tại của một khóa học, để {@code learning} tính tiến độ mà không đếm bài giảng đã xóa.
 */
public record CourseLectureIds(UUID courseId, List<UUID> lectureIds) {
}
