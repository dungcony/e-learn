package com.elearning.course.validator;

import com.elearning.common.exception.BusinessException;
import com.elearning.common.exception.ErrorCode;
import com.elearning.course.repository.CategoryRepository;
import com.elearning.course.repository.CourseRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.UUID;

@Component
@RequiredArgsConstructor
public class CategoryValidator {

    private final CategoryRepository categoryRepository;
    private final CourseRepository courseRepository;

    /**
     * @param name      tên đã trim
     * @param excludeId thể loại được bỏ qua khi đổi tên của chính nó; null khi tạo mới
     * @throws BusinessException {@code COURSE_CATEGORY_NAME_EXISTS} nếu thể loại chưa xóa khác đã dùng tên này
     */
    public void validateNameAvailable(String name, UUID excludeId) {
        boolean taken = excludeId == null
                ? categoryRepository.existsByNameIgnoreCase(name)
                : categoryRepository.existsByNameIgnoreCaseAndIdNot(name, excludeId);
        if (taken) {
            throw new BusinessException(ErrorCode.COURSE_CATEGORY_NAME_EXISTS);
        }
    }

    /**
     * @throws BusinessException {@code COURSE_CATEGORY_IN_USE} nếu còn khóa học chưa xóa thuộc thể loại này
     */
    public void validateNotInUse(UUID categoryId) {
        if (courseRepository.existsByCategoryId(categoryId)) {
            throw new BusinessException(ErrorCode.COURSE_CATEGORY_IN_USE);
        }
    }
}
