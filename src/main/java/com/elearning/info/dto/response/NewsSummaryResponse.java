package com.elearning.info.dto.response;

import java.time.Instant;
import java.util.UUID;

public record NewsSummaryResponse(UUID id, String title, Instant createdAt) {
}
