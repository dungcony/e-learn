package com.elearning.course.controller;

import com.elearning.common.response.ApiResponse;
import com.elearning.common.response.PageRequestParams;
import com.elearning.common.response.PageResponse;
import com.elearning.common.security.SecurityContextUtil;
import com.elearning.course.dto.request.CategoryCreateRequest;
import com.elearning.course.dto.request.CategoryUpdateRequest;
import com.elearning.course.dto.response.CategoryResponse;
import com.elearning.course.service.CategoryService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import java.util.UUID;

/**
 * Giảng viên quản lý thể loại khóa học (UC015).
 */
@RestController
@RequestMapping("/categories")
@PreAuthorize("hasRole('TEACHER')")
@RequiredArgsConstructor
public class CategoryController {

    private final CategoryService categoryService;

    @GetMapping
    public ApiResponse<PageResponse<CategoryResponse>> searchCategories(
            @RequestParam(required = false) String name, PageRequestParams params) {
        return ApiResponse.of(PageResponse.of(categoryService.searchCategories(name, params)));
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public ApiResponse<CategoryResponse> createCategory(@Valid @RequestBody CategoryCreateRequest request) {
        return ApiResponse.of(categoryService.createCategory(SecurityContextUtil.currentUserId(), request));
    }

    @PutMapping("/{id}")
    public ApiResponse<CategoryResponse> updateCategory(
            @PathVariable UUID id, @Valid @RequestBody CategoryUpdateRequest request) {
        return ApiResponse.of(categoryService.updateCategory(SecurityContextUtil.currentUserId(), id, request));
    }

    @DeleteMapping("/{id}")
    public ApiResponse<Void> deleteCategory(@PathVariable UUID id) {
        categoryService.deleteCategory(SecurityContextUtil.currentUserId(), id);
        return ApiResponse.of(null);
    }
}
