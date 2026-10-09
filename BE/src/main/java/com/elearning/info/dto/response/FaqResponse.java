package com.elearning.info.dto.response;

import java.time.Instant;
import java.util.UUID;

public record FaqResponse(UUID id, String question, String answer, Instant createdAt, Instant updatedAt) {
}
