package com.elearning.course.service.impl;

import com.elearning.common.exception.BusinessException;
import com.elearning.common.exception.ErrorCode;
import com.elearning.common.response.PageRequestParams;
import com.elearning.course.dto.request.CategoryCreateRequest;
import com.elearning.course.dto.request.CategoryUpdateRequest;
import com.elearning.course.dto.response.CategoryResponse;
import com.elearning.course.entity.Category;
import com.elearning.course.mapper.CategoryMapper;
import com.elearning.course.repository.CategoryRepository;
import com.elearning.course.repository.CategorySpecification;
import com.elearning.course.service.CategoryService;
import com.elearning.course.validator.CategoryValidator;
import lombok.RequiredArgsConstructor;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.data.domain.Page;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.Map;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class CategoryServiceImpl implements CategoryService {

    private static final Map<String, String> SORTABLE_FIELDS = Map.of("name", "name", "created_at", "createdAt");

    private final CategoryRepository categoryRepository;
    private final CategoryValidator categoryValidator;
    private final CategoryMapper categoryMapper;

    @Override
    @Transactional(readOnly = true)
    public Page<CategoryResponse> searchCategories(String name, PageRequestParams params) {
        return categoryRepository
                .findAll(CategorySpecification.nameContains(name), params.toPageable(SORTABLE_FIELDS, "createdAt"))
                .map(categoryMapper::toResponse);
    }

    @Override
    @Transactional
    public CategoryResponse createCategory(UUID teacherId, CategoryCreateRequest request) {
        String name = request.name().trim();
        categoryValidator.validateNameAvailable(name, null);
        Category category = Category.builder().id(UUID.randomUUID()).name(name).createdBy(teacherId).build();
        try {
            categoryRepository.saveAndFlush(category);
        } catch (DataIntegrityViolationException e) {
            // Hai request cùng tên chạy song song cùng qua bước kiểm tra, unique index chặn người đến sau.
            throw new BusinessException(ErrorCode.COURSE_CATEGORY_NAME_EXISTS);
        }
        return categoryMapper.toResponse(category);
    }

    @Override
    @Transactional
    public CategoryResponse updateCategory(UUID teacherId, UUID id, CategoryUpdateRequest request) {
        Category category = findOwned(teacherId, id);
        String name = request.name().trim();
        categoryValidator.validateNameAvailable(name, id);
        category.setName(name);
        try {
            categoryRepository.flush();
        } catch (DataIntegrityViolationException e) {
            throw new BusinessException(ErrorCode.COURSE_CATEGORY_NAME_EXISTS);
        }
        return categoryMapper.toResponse(category);
    }

    @Override
    @Transactional
    public void deleteCategory(UUID teacherId, UUID id) {
        Category category = findOwned(teacherId, id);
        categoryValidator.validateNotInUse(id);
        category.setDeletedAt(Instant.now());
    }

    private Category findOwned(UUID teacherId, UUID id) {
        return categoryRepository.findByIdAndCreatedBy(id, teacherId)
                .orElseThrow(() -> new BusinessException(ErrorCode.NOT_FOUND, "Không tìm thấy thể loại."));
    }
}
