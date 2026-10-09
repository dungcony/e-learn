package com.elearning.course.dto.response;

import com.elearning.course.enums.CourseStatus;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.UUID;

/**
 * Một dòng trong danh sách khóa học.
 *
 * @param price giá, đơn vị VND
 */
public record CourseSummaryResponse(
        UUID id,
        String code,
        String title,
        BigDecimal price,
        LocalDate startDate,
        LocalDate endDate,
        CourseStatus status,
        String imageUrl,
        String categoryName,
        String teacherName
) {
}
