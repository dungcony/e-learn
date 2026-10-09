package com.elearning.learning.dto.response;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

/**
 * Một bình luận. Bình luận gốc có {@code replies}; bình luận trả lời có {@code parentId} và {@code replies} rỗng.
 */
public record CommentResponse(
        UUID id,
        UUID lectureId,
        UUID parentId,
        String content,
        CommentAuthorResponse author,
        Instant createdAt,
        Instant updatedAt,
        List<CommentResponse> replies
) {
}
