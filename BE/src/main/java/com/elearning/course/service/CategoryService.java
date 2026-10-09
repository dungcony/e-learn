package com.elearning.course.service;

import com.elearning.common.response.PageRequestParams;
import com.elearning.course.dto.request.CategoryCreateRequest;
import com.elearning.course.dto.request.CategoryUpdateRequest;
import com.elearning.course.dto.response.CategoryResponse;
import org.springframework.data.domain.Page;

import java.util.UUID;

/**
 * Quản lý thể loại khóa học của giảng viên (UC015). Chỉ giảng viên tạo ra thể loại được sửa, xóa nó.
 */
public interface CategoryService {

    /**
     * Danh sách thể loại chưa xóa, lọc theo tên chứa chuỗi. Sắp xếp được theo {@code name}, {@code created_at};
     * mặc định mới nhất trước.
     *
     * @param name   tên cần tìm; null hoặc trống thì lấy tất cả
     * @param params phân trang, sắp xếp
     */
    Page<CategoryResponse> searchCategories(String name, PageRequestParams params);

    /**
     * @param teacherId giảng viên tạo, trở thành chủ sở hữu thể loại
     * @throws com.elearning.common.exception.BusinessException {@code COURSE_CATEGORY_NAME_EXISTS} nếu trùng tên
     *                                                          (không phân biệt hoa thường)
     */
    CategoryResponse createCategory(UUID teacherId, CategoryCreateRequest request);

    /**
     * @throws com.elearning.common.exception.BusinessException {@code NOT_FOUND} nếu thể loại không tồn tại hoặc không
     *                                                          phải của giảng viên này; {@code COURSE_CATEGORY_NAME_EXISTS}
     */
    CategoryResponse updateCategory(UUID teacherId, UUID id, CategoryUpdateRequest request);

    /**
     * Xóa mềm thể loại.
     *
     * @throws com.elearning.common.exception.BusinessException {@code NOT_FOUND} nếu không tồn tại hoặc không phải
     *                                                          của giảng viên này; {@code COURSE_CATEGORY_IN_USE}
     *                                                          nếu còn khóa học chưa xóa thuộc thể loại
     */
    void deleteCategory(UUID teacherId, UUID id);
}
