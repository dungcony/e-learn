package com.restaurant.common.response;

import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;

import java.util.Map;

/**
 * Tham số phân trang/sắp xếp chuẩn hoá, theo api/00-QUY-UOC-CHUNG.md mục 7.
 * Dựng sẵn ở Phase 1 (CORE-02) cho Phase 2 dùng lại — chưa có endpoint phân trang ở Phase 1.
 */
public record PageRequestParams(int page, int pageSize, String sortBy, String sortOrder) {

    public static final int DEFAULT_PAGE = 1;
    public static final int DEFAULT_PAGE_SIZE = 20;
    public static final int MAX_PAGE_SIZE = 100;

    public static PageRequestParams of(Integer page, Integer pageSize, String sortBy, String sortOrder) {
        int p = (page == null || page < 1) ? DEFAULT_PAGE : page;
        int ps = (pageSize == null || pageSize < 1) ? DEFAULT_PAGE_SIZE : Math.min(pageSize, MAX_PAGE_SIZE);
        String order = "asc".equalsIgnoreCase(sortOrder) ? "asc" : "desc";
        return new PageRequestParams(p, ps, sortBy, order);
    }

    /**
     * Đổi sang {@link Pageable} (trang đánh số từ 0 của Spring Data).
     *
     * @param sortableFields tên field snake_case client được phép sắp xếp → thuộc tính entity; {@code sortBy}
     *                       ngoài danh sách (hoặc trống) rơi về {@code defaultProperty} thay vì báo lỗi
     * @param defaultProperty thuộc tính entity sắp xếp mặc định
     */
    public Pageable toPageable(Map<String, String> sortableFields, String defaultProperty) {
        String property = sortBy == null ? defaultProperty : sortableFields.getOrDefault(sortBy, defaultProperty);
        Sort.Direction direction = "asc".equals(sortOrder) ? Sort.Direction.ASC : Sort.Direction.DESC;
        return PageRequest.of(page - 1, pageSize, Sort.by(direction, property).and(Sort.by("id")));
    }
}
