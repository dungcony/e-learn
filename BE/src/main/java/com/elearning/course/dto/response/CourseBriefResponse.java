package com.elearning.course.dto.response;

import com.elearning.course.enums.CourseStatus;

import java.time.LocalDate;
import java.util.UUID;

/**
 * Thông tin khóa học tối thiểu cho module {@code learning} kiểm tra điều kiện học và hiển thị danh sách.
 * Không kiểm tra {@code PUBLIC} hay ghi danh, việc đó do phía gọi quyết định.
 */
public record CourseBriefResponse(
        UUID id,
        String code,
        String title,
        CourseStatus status,
        LocalDate startDate,
        LocalDate endDate,
        String imageUrl,
        UUID teacherId
) {
}
