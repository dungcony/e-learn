package com.elearning.course.dto.response;

import java.util.List;
import java.util.UUID;

public record LearningQuestionResponse(UUID id, String content, List<LearningAnswerResponse> answers) {
}
