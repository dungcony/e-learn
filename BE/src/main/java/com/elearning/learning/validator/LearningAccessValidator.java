package com.elearning.learning.validator;

import com.elearning.common.exception.BusinessException;
import com.elearning.common.exception.ErrorCode;
import com.elearning.course.dto.response.CourseBriefResponse;
import com.elearning.course.enums.CourseStatus;
import com.elearning.course.service.CourseService;
import com.elearning.learning.repository.EnrollmentRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.time.Clock;
import java.time.LocalDate;
import java.util.UUID;

/**
 * Kiểm tra điều kiện để học viên được học bài giảng, làm bài tập, bình luận (UC016): khóa học còn hiển thị, học viên
 * đã ghi danh và khóa học đã tới ngày bắt đầu.
 */
@Component
@RequiredArgsConstructor
public class LearningAccessValidator {

    private final CourseService courseService;
    private final EnrollmentRepository enrollmentRepository;
    private final Clock clock;

    /**
     * Kiểm tra theo thứ tự, lỗi đầu tiên được trả.
     *
     * @return khóa học, để phía gọi dùng lại khỏi truy vấn lần nữa
     * @throws BusinessException {@code NOT_FOUND} nếu khóa học không tồn tại, đã xóa hoặc {@code PRIVATE} (ẩn cả với
     *                           học viên đã ghi danh); {@code ENROLLMENT_REQUIRED} nếu chưa ghi danh;
     *                           {@code COURSE_NOT_STARTED} nếu hôm nay trước ngày bắt đầu
     */
    public CourseBriefResponse requireLearningAccess(UUID studentId, UUID courseId) {
        CourseBriefResponse course = courseService.getCourseBrief(courseId);
        if (course.status() != CourseStatus.PUBLIC) {
            throw new BusinessException(ErrorCode.NOT_FOUND, "Không tìm thấy khóa học.");
        }
        if (!enrollmentRepository.existsByStudentIdAndCourseId(studentId, courseId)) {
            throw new BusinessException(ErrorCode.ENROLLMENT_REQUIRED);
        }
        if (LocalDate.now(clock).isBefore(course.startDate())) {
            throw new BusinessException(ErrorCode.COURSE_NOT_STARTED);
        }
        return course;
    }
}
