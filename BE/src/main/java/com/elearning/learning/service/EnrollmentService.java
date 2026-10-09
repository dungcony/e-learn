package com.elearning.learning.service;

import com.elearning.common.response.PageRequestParams;
import com.elearning.learning.dto.response.CourseHistoryResponse;
import com.elearning.learning.dto.response.EnrolledStudentResponse;
import com.elearning.learning.dto.response.EnrollmentResponse;
import org.springframework.data.domain.Page;

import java.util.UUID;

/**
 * Ghi danh khóa học của học viên (UC016) và lịch sử khóa học, danh sách học viên của giảng viên, Quản trị viên (UC014).
 */
public interface EnrollmentService {

    /**
     * Ghi danh học viên vào khóa học. Được ghi danh trước ngày bắt đầu; bài giảng chỉ mở từ ngày bắt đầu.
     *
     * @throws com.elearning.common.exception.BusinessException {@code NOT_FOUND} nếu khóa học không tồn tại, đã xóa hoặc
     *                                                          {@code PRIVATE}; {@code ENROLLMENT_ALREADY_EXISTS}
     */
    EnrollmentResponse enroll(UUID studentId, UUID courseId);

    /**
     * Các khóa học học viên đã ghi danh, chỉ gồm khóa còn {@code PUBLIC} và chưa xóa. Sắp xếp được theo
     * {@code enrolled_at}, {@code title}; mặc định ghi danh mới nhất trước.
     *
     * @param name tên khóa học chứa chuỗi này; null hoặc trống thì không lọc
     * @param code mã khóa học chứa chuỗi này; null hoặc trống thì không lọc
     */
    Page<EnrollmentResponse> getMyCourses(UUID studentId, String name, String code, PageRequestParams params);

    /**
     * Lịch sử khóa học (UC014): Quản trị viên thấy mọi khóa học, giảng viên chỉ thấy khóa của mình.
     */
    Page<CourseHistoryResponse> getCourseHistories(UUID viewerId, boolean viewerIsAdmin, String name, String code,
                                                   PageRequestParams params);

    /**
     * Học viên đã ghi danh một khóa học (UC014). Sắp xếp được theo {@code enrolled_at}.
     *
     * @throws com.elearning.common.exception.BusinessException {@code NOT_FOUND} nếu khóa học không tồn tại hoặc
     *                                                          giảng viên không phải chủ khóa học
     */
    Page<EnrolledStudentResponse> getEnrolledStudents(UUID viewerId, boolean viewerIsAdmin, UUID courseId,
                                                      PageRequestParams params);
}
