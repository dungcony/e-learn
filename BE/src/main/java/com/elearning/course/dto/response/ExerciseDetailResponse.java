package com.elearning.course.dto.response;

import java.util.List;
import java.util.UUID;

/**
 * Chi tiết bài tập kèm câu hỏi và đáp án đúng, chỉ trả cho giảng viên chủ khóa học.
 */
public record ExerciseDetailResponse(
        UUID id,
        UUID lectureId,
        String title,
        String description,
        List<QuestionResponse> questions
) {
}
