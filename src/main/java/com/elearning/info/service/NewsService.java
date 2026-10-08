package com.elearning.info.service;

import com.elearning.common.response.PageRequestParams;
import com.elearning.info.dto.request.NewsCreateRequest;
import com.elearning.info.dto.request.NewsUpdateRequest;
import com.elearning.info.dto.response.NewsDetailResponse;
import com.elearning.info.dto.response.NewsSummaryResponse;
import org.springframework.data.domain.Page;

import java.util.UUID;

/**
 * Quản lý tin tức của Quản trị viên (UC012). Tin tức dùng chung cho mọi Quản trị viên nên sửa, xóa không kiểm tra người đăng.
 */
public interface NewsService {

    /**
     * Danh sách tin tức chưa xóa, lọc theo tiêu đề chứa chuỗi (Bảng 2-11). Sắp xếp được theo {@code created_at},
     * {@code title}; mặc định mới nhất trước.
     *
     * @param title tiêu đề cần tìm; null hoặc trống thì lấy tất cả
     */
    Page<NewsSummaryResponse> searchNews(String title, PageRequestParams params);

    /**
     * @throws com.elearning.common.exception.BusinessException {@code NOT_FOUND} nếu tin tức không tồn tại hoặc đã xóa
     */
    NewsDetailResponse getNews(UUID id);

    /**
     * @param adminId Quản trị viên đăng tin
     */
    NewsDetailResponse createNews(UUID adminId, NewsCreateRequest request);

    /**
     * @throws com.elearning.common.exception.BusinessException {@code NOT_FOUND}
     */
    NewsDetailResponse updateNews(UUID id, NewsUpdateRequest request);

    /**
     * Xóa mềm tin tức.
     *
     * @throws com.elearning.common.exception.BusinessException {@code NOT_FOUND}
     */
    void deleteNews(UUID id);
}
