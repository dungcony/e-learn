package com.restaurant.modules.table.dto.request;

import com.restaurant.modules.table.enums.TableStatus;
import com.restaurant.modules.table.enums.TableZone;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

/**
 * Sửa bàn (UC011).
 *
 * @param status tùy chọn; Quản lý chỉ đặt tay {@code AVAILABLE} hoặc {@code OUT_OF_SERVICE} (kiểm ở {@code TableValidator}),
 *               bỏ trống thì giữ nguyên
 */
public record TableUpdateRequest(
        @NotBlank(message = "Tên/số bàn không được để trống")
        @Size(max = 20, message = "Tên/số bàn tối đa 20 ký tự")
        String name,

        @NotNull(message = "Khu vực không được để trống")
        TableZone zone,

        @NotNull(message = "Sức chứa không được để trống")
        @Min(value = 1, message = "Sức chứa tối thiểu là 1")
        @Max(value = 50, message = "Sức chứa tối đa là 50")
        Integer capacity,

        @Size(max = 255, message = "Ghi chú tối đa 255 ký tự")
        String note,

        TableStatus status) {
}
