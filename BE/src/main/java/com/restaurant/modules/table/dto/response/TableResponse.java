package com.restaurant.modules.table.dto.response;

import com.restaurant.modules.table.enums.TableStatus;
import com.restaurant.modules.table.enums.TableZone;

import java.util.UUID;

public record TableResponse(UUID id, String name, TableZone zone, int capacity, TableStatus status, String note) {
}
