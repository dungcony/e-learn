package com.elearning.course.validator;

import com.elearning.common.exception.BusinessException;
import com.elearning.course.repository.CategoryRepository;
import com.elearning.course.repository.CourseRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatCode;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class CategoryValidatorTest {

    @Mock
    private CategoryRepository categoryRepository;
    @Mock
    private CourseRepository courseRepository;
    @InjectMocks
    private CategoryValidator validator;

    @Test
    void validateNameAvailable_createRejectsExistingName() {
        when(categoryRepository.existsByNameIgnoreCase("Toán")).thenReturn(true);

        assertThatThrownBy(() -> validator.validateNameAvailable("Toán", null))
                .isInstanceOfSatisfying(BusinessException.class,
                        e -> assertThat(e.getCode()).isEqualTo("COURSE_CATEGORY_NAME_EXISTS"));
    }

    @Test
    void validateNameAvailable_renameExcludesOwnRecord() {
        UUID id = UUID.randomUUID();
        when(categoryRepository.existsByNameIgnoreCaseAndIdNot("Toán", id)).thenReturn(false);

        assertThatCode(() -> validator.validateNameAvailable("Toán", id)).doesNotThrowAnyException();
    }

    @Test
    void validateNotInUse_rejectsWhenCoursesRemain() {
        UUID id = UUID.randomUUID();
        when(courseRepository.existsByCategoryId(id)).thenReturn(true);

        assertThatThrownBy(() -> validator.validateNotInUse(id))
                .isInstanceOfSatisfying(BusinessException.class,
                        e -> assertThat(e.getCode()).isEqualTo("COURSE_CATEGORY_IN_USE"));
    }

    @Test
    void validateNotInUse_passesWhenNoCourses() {
        UUID id = UUID.randomUUID();
        when(courseRepository.existsByCategoryId(id)).thenReturn(false);

        assertThatCode(() -> validator.validateNotInUse(id)).doesNotThrowAnyException();
    }
}
