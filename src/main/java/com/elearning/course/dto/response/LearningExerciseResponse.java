package com.elearning.course.dto.response;

import java.util.List;
import java.util.UUID;

public record LearningExerciseResponse(UUID id, String title, String description, List<LearningQuestionResponse> questions) {
}
