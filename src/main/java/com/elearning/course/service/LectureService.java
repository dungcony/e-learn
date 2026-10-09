package com.elearning.course.service;

import com.elearning.common.response.PageRequestParams;
import com.elearning.course.dto.request.ExerciseCreateRequest;
import com.elearning.course.dto.request.ExerciseUpdateRequest;
import com.elearning.course.dto.request.LectureCreateRequest;
import com.elearning.course.dto.request.LectureUpdateRequest;
import com.elearning.course.dto.response.CourseLectureIds;
import com.elearning.course.dto.response.ExerciseAnswerKey;
import com.elearning.course.dto.response.ExerciseDetailResponse;
import com.elearning.course.dto.response.LearningLectureResponse;
import com.elearning.course.dto.response.LectureDetailResponse;
import com.elearning.course.dto.response.LectureSummaryResponse;
import org.springframework.data.domain.Page;

import java.util.Collection;
import java.util.List;
import java.util.UUID;

/**
 * Quản lý bài giảng và bài tập của giảng viên (UC011) và API công khai cho module {@code learning}.
 * <p>
 * Mọi thao tác của giảng viên kiểm tra quyền sở hữu qua khóa học chứa bài giảng; không phải chủ thì kết quả là
 * {@code NOT_FOUND}.
 */
public interface LectureService {

    /**
     * Tìm bài giảng của khóa học theo tên (Bảng 2-15). Sắp xếp được theo {@code created_at}, {@code title}.
     *
     * @throws com.elearning.common.exception.BusinessException {@code NOT_FOUND} nếu khóa học không phải của giảng viên
     */
    Page<LectureSummaryResponse> searchLectures(UUID teacherId, UUID courseId, String name, PageRequestParams params);

    /**
     * @throws com.elearning.common.exception.BusinessException {@code NOT_FOUND} nếu khóa học hoặc bài giảng không tồn
     *                                                          tại, không thuộc nhau, hoặc không phải của giảng viên
     */
    LectureDetailResponse getLecture(UUID teacherId, UUID courseId, UUID id);

    /**
     * @throws com.elearning.common.exception.BusinessException {@code NOT_FOUND} nếu khóa học không phải của giảng viên
     */
    LectureDetailResponse createLecture(UUID teacherId, UUID courseId, LectureCreateRequest request);

    /**
     * @throws com.elearning.common.exception.BusinessException {@code NOT_FOUND}
     */
    LectureDetailResponse updateLecture(UUID teacherId, UUID courseId, UUID id, LectureUpdateRequest request);

    /**
     * Xóa mềm bài giảng; bài tập của nó không còn truy cập được.
     *
     * @throws com.elearning.common.exception.BusinessException {@code NOT_FOUND}
     */
    void deleteLecture(UUID teacherId, UUID courseId, UUID id);

    /**
     * Thêm bài tập trắc nghiệm kèm câu hỏi và đáp án.
     *
     * @throws com.elearning.common.exception.BusinessException {@code NOT_FOUND} nếu bài giảng không phải của giảng
     *                                                          viên; {@code EXERCISE_ANSWER_INVALID} nếu có câu số
     *                                                          đáp án đúng khác 1
     */
    ExerciseDetailResponse createExercise(UUID teacherId, UUID lectureId, ExerciseCreateRequest request);

    /**
     * @throws com.elearning.common.exception.BusinessException {@code NOT_FOUND}
     */
    ExerciseDetailResponse getExercise(UUID teacherId, UUID lectureId, UUID id);

    /**
     * Thay toàn bộ bài tập: câu hỏi và đáp án cũ bị xóa mềm, thay bằng danh sách mới.
     *
     * @throws com.elearning.common.exception.BusinessException {@code NOT_FOUND}; {@code EXERCISE_ANSWER_INVALID}
     */
    ExerciseDetailResponse updateExercise(UUID teacherId, UUID lectureId, UUID id, ExerciseUpdateRequest request);

    /**
     * @throws com.elearning.common.exception.BusinessException {@code NOT_FOUND}
     */
    void deleteExercise(UUID teacherId, UUID lectureId, UUID id);

    /**
     * API công khai cho {@code learning}: các bài giảng còn tồn tại của khóa học, theo thứ tự tạo. Không kiểm tra ghi
     * danh; phía gọi tự kiểm tra.
     */
    List<LectureSummaryResponse> getLectureSummaries(UUID courseId);

    /**
     * API công khai cho {@code learning}: khóa học chứa bài giảng, để kiểm tra quyền học trước khi thao tác.
     *
     * @throws com.elearning.common.exception.BusinessException {@code NOT_FOUND} nếu bài giảng không tồn tại hoặc đã xóa
     */
    UUID getCourseIdOfLecture(UUID lectureId);

    /**
     * API công khai cho {@code learning}: nội dung bài giảng và bài tập cho học viên, không có đáp án đúng.
     *
     * @throws com.elearning.common.exception.BusinessException {@code NOT_FOUND} nếu bài giảng không tồn tại hoặc đã xóa
     */
    LearningLectureResponse getLearningLecture(UUID lectureId);

    /**
     * API công khai cho {@code learning}: đáp án chuẩn để chấm bài. Không được trả ra API.
     *
     * @throws com.elearning.common.exception.BusinessException {@code NOT_FOUND} nếu bài tập hoặc bài giảng chứa nó
     *                                                          không tồn tại hoặc đã xóa
     */
    ExerciseAnswerKey getExerciseAnswerKey(UUID exerciseId);

    /**
     * API công khai cho {@code learning}: id các bài giảng còn tồn tại của từng khóa học, để tính tiến độ.
     * Khóa học không có bài giảng vẫn có một phần tử với danh sách rỗng.
     */
    List<CourseLectureIds> getLectureIds(Collection<UUID> courseIds);
}
