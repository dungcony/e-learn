package com.restaurant.modules.billing.validator;

import com.restaurant.common.exception.BusinessException;
import com.restaurant.common.exception.ErrorCode;
import com.restaurant.modules.billing.enums.PaymentMethod;
import org.springframework.stereotype.Component;

import java.time.LocalDate;
import java.util.Map;

@Component
public class InvoiceValidator {

    /**
     * @param chargeableCount số dòng món không bị hủy
     * @param unservedCount   số dòng chưa phục vụ
     * @throws BusinessException {@code INVOICE_ORDER_EMPTY} nếu không có dòng nào; {@code INVOICE_UNSERVED_ITEMS} (kèm
     *                           {@code detail.unserved_count}) nếu còn dòng chưa phục vụ mà Thu ngân chưa xác nhận
     */
    public void validateOrderPayable(int chargeableCount, int unservedCount, boolean confirmUnserved) {
        if (chargeableCount == 0) {
            throw new BusinessException(ErrorCode.INVOICE_ORDER_EMPTY);
        }
        if (unservedCount > 0 && !confirmUnserved) {
            throw new BusinessException(ErrorCode.INVOICE_UNSERVED_ITEMS, ErrorCode.INVOICE_UNSERVED_ITEMS.getDefaultMessage(),
                    Map.of("unserved_count", unservedCount));
        }
    }

    /**
     * Chỉ tiền mặt cần số tiền khách đưa; thẻ và chuyển khoản bỏ qua giá trị gửi lên.
     *
     * @throws BusinessException {@code VALIDATION_ERROR} nếu tiền mặt thiếu số tiền; {@code INVOICE_AMOUNT_INSUFFICIENT} nếu nhỏ hơn
     *                           tổng tiền
     */
    public void validatePayment(PaymentMethod method, Long amountReceived, long totalAmount) {
        if (method != PaymentMethod.CASH) {
            return;
        }
        if (amountReceived == null) {
            throw new BusinessException(ErrorCode.VALIDATION_ERROR, "Vui lòng nhập số tiền khách đưa.");
        }
        if (amountReceived < totalAmount) {
            throw new BusinessException(ErrorCode.INVOICE_AMOUNT_INSUFFICIENT);
        }
    }

    /**
     * @throws BusinessException {@code VALIDATION_ERROR} nếu ngày bắt đầu sau ngày kết thúc
     */
    public void validateSearchRange(LocalDate fromDate, LocalDate toDate) {
        if (fromDate != null && toDate != null && fromDate.isAfter(toDate)) {
            throw new BusinessException(ErrorCode.VALIDATION_ERROR, "Ngày bắt đầu không được sau ngày kết thúc.");
        }
    }
}
