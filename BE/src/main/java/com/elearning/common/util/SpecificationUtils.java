package com.elearning.common.util;

import org.springframework.data.jpa.domain.Specification;
import org.springframework.util.StringUtils;

/**
 * Điều kiện tìm kiếm dùng lại cho nhiều module.
 */
public final class SpecificationUtils {

    private SpecificationUtils() {
    }

    /**
     * Trường chuỗi chứa {@code value}, không phân biệt hoa thường; {@code %} và {@code _} trong {@code value} được
     * coi là ký tự thường. {@code value} trống thì không lọc.
     *
     * @param field tên thuộc tính của entity
     */
    public static <T> Specification<T> containsIgnoreCase(String field, String value) {
        if (!StringUtils.hasText(value)) {
            return Specification.where(null);
        }
        return (root, query, cb) -> cb.like(cb.lower(root.get(field)), LikeUtils.contains(value), LikeUtils.ESCAPE);
    }
}
