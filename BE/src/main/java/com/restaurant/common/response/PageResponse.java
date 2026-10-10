package com.restaurant.common.response;

import org.springframework.data.domain.Page;

import java.util.List;
import java.util.function.Function;

/**
 * Dạng danh sách phân trang trong {@code data} của {@link ApiResponse}: {@code {items, meta}}.
 *
 * @param items các phần tử của trang hiện tại
 * @param meta  thông tin phân trang; {@code page} đánh số từ 1
 */
public record PageResponse<T>(List<T> items, PageMeta meta) {

    public static <T> PageResponse<T> of(Page<T> page) {
        return new PageResponse<>(page.getContent(), toMeta(page));
    }

    public static <E, T> PageResponse<T> of(Page<E> page, Function<E, T> mapper) {
        return new PageResponse<>(page.getContent().stream().map(mapper).toList(), toMeta(page));
    }

    private static PageMeta toMeta(Page<?> page) {
        return new PageMeta(page.getNumber() + 1, page.getSize(), page.getTotalElements(), page.getTotalPages());
    }
}
