package com.elearning.course.service;

import com.elearning.common.response.PageRequestParams;
import com.elearning.course.dto.request.CourseCreateRequest;
import com.elearning.course.dto.request.CourseSearchRequest;
import com.elearning.course.dto.request.CourseUpdateRequest;
import com.elearning.course.dto.response.CourseBriefResponse;
import com.elearning.course.dto.response.CourseDetailResponse;
import com.elearning.course.dto.response.CourseSummaryResponse;
import org.springframework.data.domain.Page;
import org.springframework.web.multipart.MultipartFile;

import java.util.Collection;
import java.util.List;
import java.util.UUID;

/**
 * Khóa học: tìm kiếm công khai (UC007), quản lý của giảng viên (UC009) và API công khai cho module {@code learning}.
 * <p>
 * Mọi danh sách sắp xếp được theo {@code created_at}, {@code title}, {@code start_date}, {@code end_date},
 * {@code price}; mặc định mới nhất trước. Khóa học {@code PRIVATE} bị ẩn với mọi người trừ giảng viên chủ khóa học
 * (và Quản trị viên khi xem chi tiết).
 */
public interface CourseService {

    /**
     * Tìm khóa học công khai cho Khách và học viên; chỉ trả khóa {@code PUBLIC} chưa xóa, bỏ qua {@code request.status()}.
     */
    Page<CourseSummaryResponse> searchPublicCourses(CourseSearchRequest request, PageRequestParams params);

    /**
     * Xem chi tiết khóa học.
     *
     * @param viewerId      người xem; null nếu là Khách
     * @param viewerIsAdmin người xem là Quản trị viên
     * @param id            id khóa học
     * @throws com.elearning.common.exception.BusinessException {@code NOT_FOUND} nếu không tồn tại, đã xóa, hoặc
     *                                                          {@code PRIVATE} mà người xem không phải chủ khóa học
     *                                                          hay Quản trị viên
     */
    CourseDetailResponse getCourse(UUID viewerId, boolean viewerIsAdmin, UUID id);

    /**
     * Khóa học của một giảng viên, gồm cả {@code PRIVATE}; lọc thêm theo {@code request.status()}.
     */
    Page<CourseSummaryResponse> searchTeacherCourses(UUID teacherId, CourseSearchRequest request, PageRequestParams params);

    /**
     * @param teacherId giảng viên tạo, trở thành chủ khóa học
     * @throws com.elearning.common.exception.BusinessException {@code COURSE_DATE_RANGE_INVALID}; {@code NOT_FOUND}
     *                                                          nếu thể loại không tồn tại
     */
    CourseDetailResponse createCourse(UUID teacherId, CourseCreateRequest request);

    /**
     * @throws com.elearning.common.exception.BusinessException {@code NOT_FOUND} nếu khóa học không tồn tại hoặc không
     *                                                          phải của giảng viên này, hoặc thể loại không tồn tại;
     *                                                          {@code COURSE_DATE_RANGE_INVALID}
     */
    CourseDetailResponse updateCourse(UUID teacherId, UUID id, CourseUpdateRequest request);

    /**
     * Lưu ảnh minh họa (ghi một file lên ổ đĩa trong transaction).
     *
     * @throws com.elearning.common.exception.BusinessException {@code NOT_FOUND}; {@code FILE_TYPE_NOT_SUPPORTED}
     */
    CourseDetailResponse updateCourseImage(UUID teacherId, UUID id, MultipartFile file);

    /**
     * Xóa mềm khóa học; ghi danh và bài làm giữ nguyên nhưng học viên không còn thấy khóa học.
     *
     * @throws com.elearning.common.exception.BusinessException {@code NOT_FOUND}
     */
    void deleteCourse(UUID teacherId, UUID id);

    /**
     * API công khai cho {@code learning}: thông tin khóa học tối thiểu, không kiểm tra {@code PUBLIC} hay ghi danh.
     *
     * @throws com.elearning.common.exception.BusinessException {@code NOT_FOUND} nếu không tồn tại hoặc đã xóa
     */
    CourseBriefResponse getCourseBrief(UUID id);

    /**
     * API công khai cho {@code learning}: nhiều khóa học một lượt; id không tồn tại hoặc đã xóa bị bỏ qua.
     */
    List<CourseBriefResponse> getCourseBriefs(Collection<UUID> ids);

    /**
     * API công khai cho {@code learning}: lịch sử khóa học (UC014).
     *
     * @param teacherId chỉ lấy khóa học của giảng viên này; null thì lấy của mọi giảng viên (Quản trị viên)
     * @param name      tên chứa chuỗi này; null hoặc trống thì không lọc
     * @param code      mã chứa chuỗi này; null hoặc trống thì không lọc
     */
    Page<CourseBriefResponse> searchCourseBriefs(UUID teacherId, String name, String code, PageRequestParams params);
}
