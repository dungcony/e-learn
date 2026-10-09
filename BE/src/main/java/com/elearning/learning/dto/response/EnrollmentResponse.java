package com.elearning.learning.dto.response;

import java.time.Instant;
import java.time.LocalDate;
import java.util.UUID;

/**
 * Một khóa học học viên đã ghi danh.
 *
 * @param teacherName tên giảng viên; null nếu không tìm thấy
 * @param progress    tiến độ 0–100 = số bài giảng đã xác nhận hoàn thành / tổng số bài giảng hiện có, làm tròn xuống
 */
public record EnrollmentResponse(
        UUID courseId,
        String code,
        String title,
        String imageUrl,
        String teacherName,
        LocalDate startDate,
        LocalDate endDate,
        int progress,
        Instant enrolledAt
) {
}
