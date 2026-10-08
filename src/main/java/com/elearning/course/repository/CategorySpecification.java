package com.elearning.course.repository;

import com.elearning.common.util.SpecificationUtils;
import com.elearning.course.entity.Category;
import org.springframework.data.jpa.domain.Specification;

public final class CategorySpecification {

    private CategorySpecification() {
    }

    // Tên chứa chuỗi, không phân biệt hoa thường; chuỗi trống thì không lọc.
    public static Specification<Category> nameContains(String name) {
        return SpecificationUtils.containsIgnoreCase("name", name);
    }
}
