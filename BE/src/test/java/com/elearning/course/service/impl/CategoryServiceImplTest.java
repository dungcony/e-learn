package com.elearning.course.service.impl;

import com.elearning.common.exception.BusinessException;
import com.elearning.course.dto.request.CategoryCreateRequest;
import com.elearning.course.dto.request.CategoryUpdateRequest;
import com.elearning.course.dto.response.CategoryResponse;
import com.elearning.course.entity.Category;
import com.elearning.course.mapper.CategoryMapper;
import com.elearning.course.repository.CategoryRepository;
import com.elearning.course.repository.CourseRepository;
import com.elearning.course.validator.CategoryValidator;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mapstruct.factory.Mappers;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.dao.DataIntegrityViolationException;

import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class CategoryServiceImplTest {

    @Mock
    private CategoryRepository categoryRepository;
    @Mock
    private CourseRepository courseRepository;

    private CategoryServiceImpl service;

    private final UUID teacherId = UUID.randomUUID();

    @BeforeEach
    void setUp() {
        service = new CategoryServiceImpl(categoryRepository,
                new CategoryValidator(categoryRepository, courseRepository), Mappers.getMapper(CategoryMapper.class));
    }

    @Test
    void createCategory_trimsNameAndRecordsOwner() {
        CategoryResponse response = service.createCategory(teacherId, new CategoryCreateRequest("  Math and Logic  "));

        ArgumentCaptor<Category> saved = ArgumentCaptor.forClass(Category.class);
        verify(categoryRepository).saveAndFlush(saved.capture());
        assertThat(saved.getValue().getName()).isEqualTo("Math and Logic");
        assertThat(saved.getValue().getCreatedBy()).isEqualTo(teacherId);
        assertThat(saved.getValue().getId()).isNotNull();
        assertThat(response.name()).isEqualTo("Math and Logic");
    }

    @Test
    void createCategory_duplicateNameConflicts() {
        when(categoryRepository.existsByNameIgnoreCase("Math")).thenReturn(true);

        assertThatThrownBy(() -> service.createCategory(teacherId, new CategoryCreateRequest("Math")))
                .isInstanceOfSatisfying(BusinessException.class,
                        e -> assertThat(e.getCode()).isEqualTo("COURSE_CATEGORY_NAME_EXISTS"));
        verify(categoryRepository, never()).saveAndFlush(any());
    }

    @Test
    void createCategory_uniqueIndexRaceMapsToNameConflict() {
        when(categoryRepository.saveAndFlush(any())).thenThrow(new DataIntegrityViolationException("uq_categories_name"));

        assertThatThrownBy(() -> service.createCategory(teacherId, new CategoryCreateRequest("Math")))
                .isInstanceOfSatisfying(BusinessException.class,
                        e -> assertThat(e.getCode()).isEqualTo("COURSE_CATEGORY_NAME_EXISTS"));
    }

    @Test
    void updateCategory_otherTeachersCategoryIsNotFound() {
        UUID id = UUID.randomUUID();
        when(categoryRepository.findByIdAndCreatedBy(id, teacherId)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> service.updateCategory(teacherId, id, new CategoryUpdateRequest("Mới")))
                .isInstanceOfSatisfying(BusinessException.class,
                        e -> assertThat(e.getCode()).isEqualTo("NOT_FOUND"));
    }

    @Test
    void updateCategory_renamesOwnCategoryExcludingItselfFromDuplicateCheck() {
        Category category = category();
        when(categoryRepository.findByIdAndCreatedBy(category.getId(), teacherId)).thenReturn(Optional.of(category));
        when(categoryRepository.existsByNameIgnoreCaseAndIdNot("Mới", category.getId())).thenReturn(false);

        CategoryResponse response = service.updateCategory(teacherId, category.getId(), new CategoryUpdateRequest(" Mới "));

        assertThat(category.getName()).isEqualTo("Mới");
        assertThat(response.name()).isEqualTo("Mới");
    }

    @Test
    void deleteCategory_blockedWhileCoursesRemain() {
        Category category = category();
        when(categoryRepository.findByIdAndCreatedBy(category.getId(), teacherId)).thenReturn(Optional.of(category));
        when(courseRepository.existsByCategoryId(category.getId())).thenReturn(true);

        assertThatThrownBy(() -> service.deleteCategory(teacherId, category.getId()))
                .isInstanceOfSatisfying(BusinessException.class,
                        e -> assertThat(e.getCode()).isEqualTo("COURSE_CATEGORY_IN_USE"));
        assertThat(category.getDeletedAt()).isNull();
    }

    @Test
    void deleteCategory_softDeletesWhenUnused() {
        Category category = category();
        when(categoryRepository.findByIdAndCreatedBy(category.getId(), teacherId)).thenReturn(Optional.of(category));

        service.deleteCategory(teacherId, category.getId());

        assertThat(category.getDeletedAt()).isNotNull();
        verify(categoryRepository, never()).delete(any(Category.class));
    }

    private Category category() {
        return Category.builder().id(UUID.randomUUID()).name("Cũ").createdBy(teacherId).build();
    }
}
