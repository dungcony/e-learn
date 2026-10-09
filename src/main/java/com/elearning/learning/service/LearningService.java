package com.elearning.learning.service;

import com.elearning.learning.dto.request.SubmissionSaveRequest;
import com.elearning.learning.dto.response.MyLectureDetailResponse;
import com.elearning.learning.dto.response.MyLectureSummaryResponse;
import com.elearning.learning.dto.response.SubmissionResponse;

import java.util.List;
import java.util.UUID;

/**
 * Học bài giảng, làm và nộp bài tập (UC016). Mọi hàm kiểm tra điều kiện học trước: khóa học còn {@code PUBLIC}, học viên
 * đã ghi danh và khóa học đã tới ngày bắt đầu; không đạt thì ném {@code BusinessException} với {@code NOT_FOUND},
 * {@code ENROLLMENT_REQUIRED} hoặc {@code COURSE_NOT_STARTED} tương ứng.
 */
public interface LearningService {

    List<MyLectureSummaryResponse> getMyLectures(UUID studentId, UUID courseId);

    /**
     * @throws com.elearning.common.exception.BusinessException {@code NOT_FOUND} nếu bài giảng không thuộc khóa học
     */
    MyLectureDetailResponse getMyLecture(UUID studentId, UUID courseId, UUID lectureId);

    /**
     * Xác nhận hoàn thành bài giảng. Gọi lại vẫn thành công và không tạo bản ghi trùng.
     */
    void completeLecture(UUID studentId, UUID lectureId);

    /**
     * Lưu tạm đáp án của bài tập, chưa cần đủ câu; câu đã có đáp án thì bị ghi đè.
     * <p>
     * <b>Locking:</b> khóa bi quan dòng bài làm ({@code FOR UPDATE}) để không đua với lúc nộp bài.
     *
     * @throws com.elearning.common.exception.BusinessException {@code VALIDATION_ERROR} nếu câu hỏi không thuộc bài tập,
     *                                                          đáp án không thuộc câu hỏi hoặc một câu bị trả lời hai
     *                                                          lần; {@code SUBMISSION_ALREADY_SUBMITTED}
     */
    SubmissionResponse saveSubmission(UUID studentId, UUID exerciseId, SubmissionSaveRequest request);

    /**
     * Nộp bài tập: chấm từng câu rồi lưu điểm. Chỉ nộp một lần.
     * <p>
     * <b>Locking:</b> khóa bi quan dòng bài làm ({@code FOR UPDATE}) để hai request nộp cùng lúc không cùng thành công.
     *
     * @return kết quả kèm đáp án đúng
     * @throws com.elearning.common.exception.BusinessException {@code SUBMISSION_INCOMPLETE} nếu còn câu chưa trả lời
     *                                                          hoặc chưa có bài làm; {@code SUBMISSION_ALREADY_SUBMITTED}
     */
    SubmissionResponse submit(UUID studentId, UUID exerciseId);

    /**
     * Xem bài làm; đáp án đúng chỉ hiện sau khi đã nộp.
     *
     * @throws com.elearning.common.exception.BusinessException {@code NOT_FOUND} nếu chưa có bài làm
     */
    SubmissionResponse getSubmission(UUID studentId, UUID exerciseId);
}
