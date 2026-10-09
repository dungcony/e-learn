package com.elearning.course.dto.request;

import com.elearning.course.enums.CourseStatus;

import java.math.BigDecimal;
import java.time.LocalDate;

/**
 * Tiêu chí tìm kiếm khóa học (UC007, Bảng 2-13); tiêu chí để trống thì không lọc.
 *
 * @param code      mã khóa học chứa chuỗi này, không phân biệt hoa thường
 * @param name      tên khóa học chứa chuỗi này
 * @param price     giá bằng đúng giá trị này (VND)
 * @param startDate khóa học bắt đầu từ ngày này trở đi
 * @param endDate   khóa học kết thúc trước hoặc đúng ngày này
 * @param status    trạng thái; chỉ có tác dụng ở danh sách của giảng viên, danh sách công khai luôn là {@code PUBLIC}
 */
public record CourseSearchRequest(
        String code,
        String name,
        BigDecimal price,
        LocalDate startDate,
        LocalDate endDate,
        CourseStatus status
) {
}
