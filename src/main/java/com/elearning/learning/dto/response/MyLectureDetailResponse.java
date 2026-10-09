package com.elearning.learning.dto.response;

import java.util.List;
import java.util.UUID;

/**
 * Nội dung một bài giảng cho học viên đã ghi danh; không lộ đáp án đúng của bài tập.
 */
public record MyLectureDetailResponse(
        UUID id,
        UUID courseId,
        String title,
        String description,
        String contentUrl,
        boolean completed,
        List<MyExerciseResponse> exercises
) {
}
