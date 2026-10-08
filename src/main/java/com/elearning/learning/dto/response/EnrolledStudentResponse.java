package com.elearning.learning.dto.response;

import java.time.Instant;
import java.util.UUID;

/**
 * Một học viên đã ghi danh khóa học (UC014).
 *
 * @param progress tiến độ 0–100 của học viên trong khóa học này
 */
public record EnrolledStudentResponse(UUID studentId, String fullName, String email, Instant enrolledAt, int progress) {
}
