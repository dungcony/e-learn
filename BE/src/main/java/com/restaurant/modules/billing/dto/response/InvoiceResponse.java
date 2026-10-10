package com.restaurant.modules.billing.dto.response;

import com.restaurant.modules.billing.enums.InvoiceStatus;
import com.restaurant.modules.billing.enums.PaymentMethod;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;
import java.util.UUID;

/**
 * Hóa đơn đầy đủ để in. Số tiền là VND.
 *
 * @param vatRate        tỷ lệ VAT tại thời điểm thanh toán, vd {@code 0.0800}
 * @param amountReceived tiền khách đưa; với thẻ và chuyển khoản bằng {@code totalAmount}
 * @param changeAmount   tiền thừa trả khách
 * @param printCount     số lần in lại
 * @param reprint        {@code true} chỉ ở response của thao tác in lại, để client in kèm dấu "Bản in lại"
 */
public record InvoiceResponse(
        UUID id,
        String code,
        InvoiceStatus status,
        String tableName,
        String cashierName,
        Instant paidAt,
        List<InvoiceItemResponse> items,
        long subtotal,
        BigDecimal vatRate,
        long vatAmount,
        long totalAmount,
        PaymentMethod paymentMethod,
        long amountReceived,
        long changeAmount,
        String note,
        int printCount,
        boolean reprint) {
}
