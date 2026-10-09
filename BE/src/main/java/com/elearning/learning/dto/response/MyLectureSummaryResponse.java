package com.elearning.learning.dto.response;

import java.util.UUID;

public record MyLectureSummaryResponse(UUID id, String title, boolean completed) {
}
