package com.elearning.course.dto.response;

import java.time.Instant;
import java.util.UUID;

public record CategoryResponse(UUID id, String name, UUID createdBy, Instant createdAt) {
}
