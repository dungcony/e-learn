package com.elearning.learning.dto.response;

import com.elearning.course.enums.CourseStatus;

import java.time.LocalDate;
import java.util.UUID;

/**
 * Một dòng lịch sử khóa học của giảng viên hoặc Quản trị viên (UC014).
 */
public record CourseHistoryResponse(
        UUID courseId,
        String code,
        String title,
        CourseStatus status,
        LocalDate startDate,
        LocalDate endDate,
        long studentCount
) {
}
