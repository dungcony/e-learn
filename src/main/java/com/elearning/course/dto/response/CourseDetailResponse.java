package com.elearning.course.dto.response;

import com.elearning.course.enums.CourseStatus;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

/**
 * Chi tiết khóa học (SRS mục 3.1: tên, giảng viên, danh sách bài giảng).
 *
 * @param price    giá, đơn vị VND
 * @param category thể loại; {@code null} nếu thể loại không còn
 * @param teacher  giảng viên; {@code null} nếu không tìm thấy
 * @param lectures các bài giảng còn tồn tại, theo thứ tự tạo
 */
public record CourseDetailResponse(
        UUID id,
        String code,
        String title,
        String description,
        BigDecimal price,
        LocalDate startDate,
        LocalDate endDate,
        CourseStatus status,
        String imageUrl,
        String referenceMaterials,
        CategoryRefResponse category,
        TeacherRefResponse teacher,
        List<LectureRefResponse> lectures
) {
}
