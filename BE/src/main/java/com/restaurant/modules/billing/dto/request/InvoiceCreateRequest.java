package com.restaurant.modules.billing.dto.request;

import com.restaurant.modules.billing.enums.PaymentMethod;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.PositiveOrZero;
import jakarta.validation.constraints.Size;

import java.util.UUID;

/**
 * Yêu cầu thanh toán và xuất hóa đơn cho một đơn đang mở.
 *
 * @param amountReceived tiền khách đưa (VND); bắt buộc khi {@code CASH}, bị bỏ qua với thẻ và chuyển khoản
 * @param confirmUnserved {@code true} khi Thu ngân đã biết còn món chưa phục vụ và vẫn muốn thanh toán
 */
public record InvoiceCreateRequest(
        @NotNull(message = "Đơn hàng không được để trống") UUID orderId,
        @NotNull(message = "Phương thức thanh toán không được để trống") PaymentMethod paymentMethod,
        @PositiveOrZero(message = "Số tiền khách đưa không được âm") Long amountReceived,
        @Size(max = 255, message = "Ghi chú tối đa 255 ký tự") String note,
        boolean confirmUnserved) {
}
