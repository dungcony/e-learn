package com.elearning.learning.dto.response;

import com.elearning.course.dto.response.LearningQuestionResponse;
import com.elearning.learning.enums.SubmissionStatus;

import java.util.List;
import java.util.UUID;

/**
 * Bài tập trong nội dung bài giảng của học viên.
 *
 * @param submissionStatus trạng thái bài làm; null nếu học viên chưa làm
 */
public record MyExerciseResponse(
        UUID id,
        String title,
        String description,
        SubmissionStatus submissionStatus,
        List<LearningQuestionResponse> questions
) {
}
