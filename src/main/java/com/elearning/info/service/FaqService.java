package com.elearning.info.service;

import com.elearning.common.response.PageRequestParams;
import com.elearning.info.dto.request.FaqCreateRequest;
import com.elearning.info.dto.request.FaqUpdateRequest;
import com.elearning.info.dto.response.FaqResponse;
import org.springframework.data.domain.Page;

import java.util.UUID;

/**
 * Quản lý câu hỏi thường gặp của Quản trị viên (UC013).
 */
public interface FaqService {

    /**
     * Danh sách FAQ chưa xóa, lọc theo câu hỏi chứa chuỗi (Bảng 2-12). Sắp xếp được theo {@code created_at},
     * {@code question}; mặc định mới nhất trước.
     *
     * @param question câu hỏi cần tìm; null hoặc trống thì lấy tất cả
     */
    Page<FaqResponse> searchFaqs(String question, PageRequestParams params);

    /**
     * @throws com.elearning.common.exception.BusinessException {@code NOT_FOUND} nếu FAQ không tồn tại hoặc đã xóa
     */
    FaqResponse getFaq(UUID id);

    /**
     * @param adminId Quản trị viên tạo FAQ
     */
    FaqResponse createFaq(UUID adminId, FaqCreateRequest request);

    /**
     * @throws com.elearning.common.exception.BusinessException {@code NOT_FOUND}
     */
    FaqResponse updateFaq(UUID id, FaqUpdateRequest request);

    /**
     * Xóa mềm FAQ.
     *
     * @throws com.elearning.common.exception.BusinessException {@code NOT_FOUND}
     */
    void deleteFaq(UUID id);
}
