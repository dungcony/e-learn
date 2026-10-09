package com.elearning.course.dto.response;

import java.time.Instant;
import java.util.UUID;

public record LectureSummaryResponse(UUID id, String title, Instant createdAt) {
}
