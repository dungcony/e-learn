package com.restaurant.modules.table.dto.request;

import com.restaurant.modules.table.enums.TableStatus;
import com.restaurant.modules.table.enums.TableZone;
import jakarta.validation.constraints.Positive;
import org.springframework.web.bind.annotation.BindParam;

/**
 * Tiêu chí tìm bàn (UC007, Bảng 2-15); mọi tiêu chí tùy chọn, kết hợp bằng AND.
 *
 * @param name        tên/số bàn chứa chuỗi, không phân biệt hoa thường
 * @param minCapacity sức chứa tối thiểu
 */
public record TableSearchRequest(
        String name,
        TableZone zone,
        @BindParam("min_capacity") @Positive(message = "Sức chứa tối thiểu phải lớn hơn 0") Integer minCapacity,
        TableStatus status) {
}
