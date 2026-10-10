package com.restaurant.modules.table.dto.response;

import com.restaurant.modules.table.enums.TableStatus;
import com.restaurant.modules.table.enums.TableZone;

import java.util.UUID;

/** Thông tin bàn tối thiểu để module khác (đặt bàn, đơn hàng, hóa đơn) hiển thị tên và khu vực bàn. */
public record TableBriefResponse(UUID id, String name, TableZone zone, int capacity, TableStatus status) {
}
