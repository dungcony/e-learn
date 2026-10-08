package com.elearning.course.repository;

import com.elearning.common.util.LikeUtils;
import com.elearning.course.entity.Category;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.util.StringUtils;

public final class CategorySpecification {

    private CategorySpecification() {
    }

    // Tên chứa chuỗi, không phân biệt hoa thường; chuỗi trống thì không lọc.
    public static Specification<Category> nameContains(String name) {
        if (!StringUtils.hasText(name)) {
            return Specification.where(null);
        }
        return (root, query, cb) -> cb.like(cb.lower(root.get("name")), LikeUtils.contains(name), LikeUtils.ESCAPE);
    }
}
