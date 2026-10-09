package com.elearning.course.dto.response;

import java.util.List;
import java.util.UUID;

/**
 * Bài giảng dành cho học viên: nội dung và bài tập nhưng không lộ đáp án đúng. Module {@code learning} chịu trách
 * nhiệm kiểm tra học viên đã ghi danh trước khi dùng.
 */
public record LearningLectureResponse(
        UUID id,
        UUID courseId,
        String title,
        String description,
        String contentUrl,
        List<LearningExerciseResponse> exercises
) {
}
