package com.restaurant.common.util;

import java.util.Locale;

/**
 * Dựng mẫu {@code LIKE} cho tìm kiếm "chứa chuỗi, không phân biệt hoa thường" trong các Specification.
 */
public final class LikeUtils {

    public static final char ESCAPE = '\\';

    private LikeUtils() {
    }

    /**
     * Mẫu {@code %chuỗi%} viết thường; {@code %}, {@code _}, {@code \\} trong đầu vào được escape để
     * người dùng nhập "50%" không bị hiểu là ký tự đại diện.
     */
    public static String contains(String value) {
        String escaped = value.trim().toLowerCase(Locale.ROOT)
                .replace("\\", "\\\\")
                .replace("%", "\\%")
                .replace("_", "\\_");
        return "%" + escaped + "%";
    }
}
