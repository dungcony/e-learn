package com.elearning.info.dto.response;

import java.time.Instant;
import java.util.UUID;

public record NewsDetailResponse(UUID id, String title, String content, UUID authorId, Instant createdAt, Instant updatedAt) {
}
