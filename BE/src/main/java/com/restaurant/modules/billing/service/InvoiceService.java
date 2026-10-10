package com.restaurant.modules.billing.service;

import com.restaurant.common.response.PageRequestParams;
import com.restaurant.modules.billing.dto.request.InvoiceCreateRequest;
import com.restaurant.modules.billing.dto.request.InvoiceSearchRequest;
import com.restaurant.modules.billing.dto.response.InvoiceResponse;
import com.restaurant.modules.billing.dto.response.InvoiceSummaryResponse;
import com.restaurant.modules.billing.dto.response.PaymentSummaryResponse;
import com.restaurant.modules.user.enums.Role;
import org.springframework.data.domain.Page;

import java.util.UUID;

/**
 * Thanh toán và xuất hóa đơn (UC018), lịch sử hóa đơn (UC019, UC007) và hóa đơn của khách hàng (UC017). Thu ngân chỉ thấy hóa
 * đơn của ngày hôm nay theo múi giờ nhà hàng; Quản lý thấy toàn bộ.
 */
public interface InvoiceService {

    /**
     * Tạm tính của đơn đang mở để Thu ngân xem trước khi trả.
     *
     * @throws com.restaurant.common.exception.BusinessException {@code NOT_FOUND} nếu không có đơn, {@code ORDER_NOT_OPEN} nếu đơn đã đóng
     */
    PaymentSummaryResponse getPaymentSummary(UUID orderId);

    /**
     * Thanh toán: lưu hóa đơn kèm các dòng chụp, đóng đơn và trả bàn về {@code AVAILABLE} trong một transaction.
     * <p>
     * <b>Khóa:</b> khóa đơn ({@code FOR UPDATE}) rồi mới tới bàn; hai thu ngân cùng tính một bàn thì người sau nhận
     * {@code ORDER_NOT_OPEN}. Không gọi I/O ngoài trong hàm này.
     * </p>
     *
     * @param cashierId id Thu ngân lập hóa đơn
     * @return hóa đơn vừa lập để in ngay
     * @throws com.restaurant.common.exception.BusinessException {@code NOT_FOUND}, {@code ORDER_NOT_OPEN}, {@code INVOICE_ORDER_EMPTY},
     *                                                          {@code INVOICE_UNSERVED_ITEMS}, {@code INVOICE_AMOUNT_INSUFFICIENT},
     *                                                          {@code VALIDATION_ERROR} (tiền mặt thiếu số tiền)
     */
    InvoiceResponse createInvoice(UUID cashierId, InvoiceCreateRequest request);

    /**
     * Tìm hóa đơn mới nhất trước; với Thu ngân khoảng ngày bị ép về hôm nay.
     *
     * @throws com.restaurant.common.exception.BusinessException {@code VALIDATION_ERROR} nếu ngày bắt đầu sau ngày kết thúc
     */
    Page<InvoiceSummaryResponse> searchInvoices(Role viewerRole, InvoiceSearchRequest request, PageRequestParams params);

    /**
     * @throws com.restaurant.common.exception.BusinessException {@code NOT_FOUND} nếu không có hóa đơn hoặc Thu ngân xem hóa đơn ngoài hôm nay
     */
    InvoiceResponse getInvoice(Role viewerRole, UUID id);

    /**
     * In lại: tăng {@code print_count} và trả hóa đơn với {@code reprint = true}. Cùng quy tắc nhìn thấy như {@link #getInvoice}.
     *
     * @throws com.restaurant.common.exception.BusinessException {@code NOT_FOUND}
     */
    InvoiceResponse reprintInvoice(Role viewerRole, UUID id);

    /** Hóa đơn của chính khách hàng, mới nhất trước. */
    Page<InvoiceSummaryResponse> listMine(UUID customerId, PageRequestParams params);

    /**
     * @throws com.restaurant.common.exception.BusinessException {@code NOT_FOUND} nếu hóa đơn không phải của khách hàng này
     */
    InvoiceResponse getMine(UUID customerId, UUID id);
}
