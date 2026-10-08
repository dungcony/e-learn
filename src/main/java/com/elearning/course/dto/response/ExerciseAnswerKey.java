package com.elearning.course.dto.response;

import java.util.List;
import java.util.UUID;

/**
 * Đáp án chuẩn của cả bài tập, chỉ dùng nội bộ cho module {@code learning} chấm điểm; không được trả ra API.
 *
 * @param lectureId bài giảng chứa bài tập
 * @param courseId  khóa học chứa bài giảng
 * @param questions các câu hỏi còn tồn tại theo thứ tự hiển thị
 */
public record ExerciseAnswerKey(UUID exerciseId, UUID lectureId, UUID courseId, List<QuestionAnswerKey> questions) {
}
