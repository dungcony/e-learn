package com.elearning.course.enums;

/**
 * Trạng thái hiển thị của khóa học. {@code PRIVATE} là ẩn với Khách và học viên: không hiện khi tìm kiếm hay xem
 * chi tiết, không ghi danh được, và học viên đã ghi danh cũng không học được tới khi khóa học {@code PUBLIC} lại.
 */
public enum CourseStatus {
    PUBLIC,
    PRIVATE
}
