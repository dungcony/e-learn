package com.restaurant.modules.reservation.dto.response;

import java.time.Instant;
import java.util.List;

/**
 * @param suggestions tối đa vài khung giờ gần nhất còn bàn, cách nhau 30 phút, cùng ngày; rỗng khi {@code available = true}
 */
public record AvailabilityResponse(boolean available, List<Instant> suggestions) {
}
