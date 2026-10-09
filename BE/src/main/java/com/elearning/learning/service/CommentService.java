package com.elearning.learning.service;

import com.elearning.common.response.PageRequestParams;
import com.elearning.learning.dto.request.CommentCreateRequest;
import com.elearning.learning.dto.request.CommentUpdateRequest;
import com.elearning.learning.dto.response.CommentResponse;
import org.springframework.data.domain.Page;

import java.util.UUID;

/**
 * Thảo luận dưới bài giảng (UC016). Xem và đăng bình luận cần thỏa điều kiện học; sửa, xóa chỉ cần là tác giả.
 */
public interface CommentService {

    /**
     * Bình luận gốc của bài giảng, mới nhất trước, mỗi bình luận kèm các trả lời (cũ nhất trước). Phân trang theo bình
     * luận gốc. Sắp xếp được theo {@code created_at}.
     *
     * @throws com.elearning.common.exception.BusinessException {@code NOT_FOUND}, {@code ENROLLMENT_REQUIRED},
     *                                                          {@code COURSE_NOT_STARTED} theo điều kiện học
     */
    Page<CommentResponse> getComments(UUID studentId, UUID lectureId, PageRequestParams params);

    /**
     * @throws com.elearning.common.exception.BusinessException điều kiện học như {@link #getComments};
     *                                                          {@code VALIDATION_ERROR} nếu {@code parentId} là một
     *                                                          bình luận trả lời hoặc thuộc bài giảng khác;
     *                                                          {@code NOT_FOUND} nếu {@code parentId} không tồn tại
     */
    CommentResponse createComment(UUID studentId, UUID lectureId, CommentCreateRequest request);

    /**
     * @throws com.elearning.common.exception.BusinessException {@code NOT_FOUND} nếu bình luận không tồn tại, đã xóa
     *                                                          hoặc không phải của người này
     */
    CommentResponse updateComment(UUID studentId, UUID id, CommentUpdateRequest request);

    /**
     * Xóa mềm bình luận; xóa bình luận gốc thì các trả lời của nó cũng không còn hiển thị.
     *
     * @throws com.elearning.common.exception.BusinessException {@code NOT_FOUND}
     */
    void deleteComment(UUID studentId, UUID id);
}
