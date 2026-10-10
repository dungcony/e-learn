package com.restaurant.modules.order.dto.request;

import jakarta.validation.constraints.NotNull;

import java.util.UUID;

/** Mở đơn cho khách vãng lai ở một bàn đang trống. */
public record OrderOpenRequest(@NotNull(message = "Bàn không được để trống") UUID tableId) {
}
