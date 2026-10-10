package com.restaurant.modules.billing.service.impl;

import com.restaurant.common.code.DailyCodeGenerator;
import com.restaurant.common.config.RestaurantProperties;
import com.restaurant.common.exception.BusinessException;
import com.restaurant.common.exception.ErrorCode;
import com.restaurant.common.response.PageRequestParams;
import com.restaurant.common.util.SpecificationUtils;
import com.restaurant.modules.billing.dto.request.InvoiceCreateRequest;
import com.restaurant.modules.billing.dto.request.InvoiceSearchRequest;
import com.restaurant.modules.billing.dto.response.InvoiceResponse;
import com.restaurant.modules.billing.dto.response.InvoiceSummaryResponse;
import com.restaurant.modules.billing.dto.response.PaymentLineResponse;
import com.restaurant.modules.billing.dto.response.PaymentSummaryResponse;
import com.restaurant.modules.billing.entity.Invoice;
import com.restaurant.modules.billing.entity.InvoiceItem;
import com.restaurant.modules.billing.enums.InvoiceStatus;
import com.restaurant.modules.billing.enums.PaymentMethod;
import com.restaurant.modules.billing.helper.InvoiceAmounts;
import com.restaurant.modules.billing.helper.InvoiceCalculator;
import com.restaurant.modules.billing.mapper.InvoiceMapper;
import com.restaurant.modules.billing.repository.InvoiceItemRepository;
import com.restaurant.modules.billing.repository.InvoiceRepository;
import com.restaurant.modules.billing.service.InvoiceService;
import com.restaurant.modules.billing.validator.InvoiceValidator;
import com.restaurant.modules.order.dto.response.OrderItemResponse;
import com.restaurant.modules.order.dto.response.OrderPaymentView;
import com.restaurant.modules.order.dto.response.OrderResponse;
import com.restaurant.modules.order.dto.response.PaymentLine;
import com.restaurant.modules.order.enums.OrderItemStatus;
import com.restaurant.modules.order.enums.OrderStatus;
import com.restaurant.modules.order.service.OrderService;
import com.restaurant.modules.table.dto.response.TableBriefResponse;
import com.restaurant.modules.table.enums.TableStatus;
import com.restaurant.modules.table.service.TableService;
import com.restaurant.modules.user.dto.response.UserBriefResponse;
import com.restaurant.modules.user.enums.Role;
import com.restaurant.modules.user.service.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.data.domain.Page;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Clock;
import java.time.Instant;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.stream.IntStream;

/**
 * Điều phối thanh toán. Hóa đơn là bản chụp: tên bàn, tên thu ngân, tên món, đơn giá và tỷ lệ VAT được ghi thẳng vào hóa đơn
 * nên đổi dữ liệu gốc về sau không làm hóa đơn cũ thay đổi. Mã hóa đơn dạng {@code HD20261009-001} sinh theo ngày.
 */
@Service
@RequiredArgsConstructor
public class InvoiceServiceImpl implements InvoiceService {

    private static final String INVOICE_CODE_PREFIX = "HD";
    private static final String UNIQUE_ORDER_CONSTRAINT = "uq_invoices_order";

    // Tên field client được phép sắp xếp (snake_case) -> thuộc tính entity
    private static final Map<String, String> SORTABLE_FIELDS =
            Map.of("paid_at", "paidAt", "total_amount", "totalAmount", "code", "code");

    private final InvoiceRepository invoiceRepository;
    private final InvoiceItemRepository invoiceItemRepository;
    private final InvoiceValidator invoiceValidator;
    private final InvoiceCalculator invoiceCalculator;
    private final InvoiceMapper invoiceMapper;
    private final OrderService orderService;
    private final TableService tableService;
    private final UserService userService;
    private final DailyCodeGenerator dailyCodeGenerator;
    private final RestaurantProperties properties;
    private final Clock clock;

    @Override
    @Transactional(readOnly = true)
    public PaymentSummaryResponse getPaymentSummary(UUID orderId) {
        OrderResponse order = orderService.getOrder(orderId);
        if (order.status() != OrderStatus.OPEN) {
            throw new BusinessException(ErrorCode.ORDER_NOT_OPEN);
        }
        List<OrderItemResponse> chargeable = order.items().stream()
                .filter(item -> item.status() != OrderItemStatus.CANCELLED)
                .toList();
        List<PaymentLine> lines = chargeable.stream()
                .map(item -> new PaymentLine(item.dishId(), item.dishName(), item.unitPrice(), item.quantity(), item.status()))
                .toList();
        InvoiceAmounts amounts = invoiceCalculator.calculate(lines, properties.vatRate());
        List<PaymentLineResponse> lineResponses = chargeable.stream().map(invoiceMapper::toPaymentLineResponse).toList();
        return new PaymentSummaryResponse(order.id(), order.table(), order.openedAt(), lineResponses, amounts.subtotal(),
                properties.vatRate(), amounts.vatAmount(), amounts.totalAmount(), order.unservedCount());
    }

    @Override
    @Transactional
    public InvoiceResponse createInvoice(UUID cashierId, InvoiceCreateRequest request) {
        OrderPaymentView view = orderService.lockOrderForPayment(request.orderId());
        List<PaymentLine> lines = view.lines().stream()
                .filter(line -> line.status() != OrderItemStatus.CANCELLED)
                .toList();
        invoiceValidator.validateOrderPayable(lines.size(), view.unservedCount(), request.confirmUnserved());
        InvoiceAmounts amounts = invoiceCalculator.calculate(lines, properties.vatRate());
        invoiceValidator.validatePayment(request.paymentMethod(), request.amountReceived(), amounts.totalAmount());

        Invoice invoice = newInvoice(cashierId, request, view, amounts);
        saveInvoice(invoice);
        List<InvoiceItem> items = saveItems(invoice.getId(), lines);

        orderService.closeOrder(view.orderId());
        tableService.transitionStatus(view.tableId(), Set.of(TableStatus.OCCUPIED), TableStatus.AVAILABLE);
        return invoiceMapper.toResponse(invoice, items, false);
    }

    @Override
    @Transactional(readOnly = true)
    public Page<InvoiceSummaryResponse> searchInvoices(Role viewerRole, InvoiceSearchRequest request, PageRequestParams params) {
        LocalDate fromDate = request.fromDate();
        LocalDate toDate = request.toDate();
        if (viewerRole == Role.CASHIER) {
            // Thu ngân chỉ làm việc với hóa đơn trong ngày nên khoảng ngày tự chọn bị cắt về hôm nay
            fromDate = LocalDate.now(clock);
            toDate = fromDate;
        }
        invoiceValidator.validateSearchRange(fromDate, toDate);

        Specification<Invoice> spec = Specification.<Invoice>where(SpecificationUtils.containsIgnoreCase("code", request.code()))
                .and(SpecificationUtils.containsIgnoreCase("tableName", request.table()));
        if (request.paymentMethod() != null) {
            PaymentMethod method = request.paymentMethod();
            spec = spec.and((root, query, cb) -> cb.equal(root.get("paymentMethod"), method));
        }
        if (fromDate != null) {
            Instant from = startOf(fromDate);
            spec = spec.and((root, query, cb) -> cb.greaterThanOrEqualTo(root.<Instant>get("paidAt"), from));
        }
        if (toDate != null) {
            Instant to = startOf(toDate.plusDays(1));
            spec = spec.and((root, query, cb) -> cb.lessThan(root.<Instant>get("paidAt"), to));
        }
        return invoiceRepository.findAll(spec, params.toPageable(SORTABLE_FIELDS, "paidAt")).map(invoiceMapper::toSummaryResponse);
    }

    @Override
    @Transactional(readOnly = true)
    public InvoiceResponse getInvoice(Role viewerRole, UUID id) {
        return toResponse(findVisible(viewerRole, id), false);
    }

    @Override
    @Transactional
    public InvoiceResponse reprintInvoice(Role viewerRole, UUID id) {
        findVisible(viewerRole, id);
        invoiceRepository.incrementPrintCount(id);
        // Đọc lại sau khi tăng để print_count trả về là số thật, kể cả khi có lần in song song
        return toResponse(findVisible(viewerRole, id), true);
    }

    @Override
    @Transactional(readOnly = true)
    public Page<InvoiceSummaryResponse> listMine(UUID customerId, PageRequestParams params) {
        Specification<Invoice> spec = (root, query, cb) -> cb.equal(root.get("customerId"), customerId);
        // Khách hàng luôn xem mới nhất trước, không cho đổi cách sắp xếp
        PageRequestParams newestFirst = new PageRequestParams(params.page(), params.pageSize(), null, "desc");
        return invoiceRepository.findAll(spec, newestFirst.toPageable(SORTABLE_FIELDS, "paidAt")).map(invoiceMapper::toSummaryResponse);
    }

    @Override
    @Transactional(readOnly = true)
    public InvoiceResponse getMine(UUID customerId, UUID id) {
        Invoice invoice = invoiceRepository.findByIdAndCustomerId(id, customerId).orElseThrow(this::invoiceNotFound);
        return toResponse(invoice, false);
    }

    private Invoice newInvoice(UUID cashierId, InvoiceCreateRequest request, OrderPaymentView view, InvoiceAmounts amounts) {
        TableBriefResponse table = tableService.getTableBriefs(List.of(view.tableId())).stream().findFirst()
                .orElseThrow(() -> new BusinessException(ErrorCode.NOT_FOUND, "Không tìm thấy bàn."));
        UserBriefResponse cashier = userService.getUserBriefs(List.of(cashierId)).stream().findFirst()
                .orElseThrow(() -> new BusinessException(ErrorCode.NOT_FOUND, "Không tìm thấy thu ngân."));
        Instant now = clock.instant();

        Invoice invoice = new Invoice();
        invoice.setId(UUID.randomUUID());
        invoice.setCode(dailyCodeGenerator.next(INVOICE_CODE_PREFIX, LocalDate.now(clock)));
        invoice.setOrderId(view.orderId());
        invoice.setTableId(view.tableId());
        invoice.setTableName(table.name());
        invoice.setCustomerId(view.customerId());
        invoice.setCashierId(cashierId);
        invoice.setCashierName(cashier.fullName());
        invoice.setSubtotal(amounts.subtotal());
        invoice.setVatRate(properties.vatRate());
        invoice.setVatAmount(amounts.vatAmount());
        invoice.setTotalAmount(amounts.totalAmount());
        invoice.setPaymentMethod(request.paymentMethod());
        // Thẻ và chuyển khoản coi như trả đúng tổng tiền, bỏ qua số tiền client gửi
        boolean cash = request.paymentMethod() == PaymentMethod.CASH;
        invoice.setAmountReceived(cash ? request.amountReceived() : amounts.totalAmount());
        invoice.setChangeAmount(invoice.getAmountReceived() - amounts.totalAmount());
        invoice.setNote(request.note());
        invoice.setStatus(InvoiceStatus.PAID);
        invoice.setPaidAt(now);
        return invoice;
    }

    private void saveInvoice(Invoice invoice) {
        try {
            invoiceRepository.saveAndFlush(invoice);
        } catch (DataIntegrityViolationException e) {
            // Lớp chặn cuối: đơn đã có hóa đơn thì với người dùng đơn này đã đóng
            if (String.valueOf(e.getMostSpecificCause().getMessage()).contains(UNIQUE_ORDER_CONSTRAINT)) {
                throw new BusinessException(ErrorCode.ORDER_NOT_OPEN);
            }
            throw e;
        }
    }

    private List<InvoiceItem> saveItems(UUID invoiceId, List<PaymentLine> lines) {
        List<InvoiceItem> items = new ArrayList<>(IntStream.range(0, lines.size()).mapToObj(index -> {
            PaymentLine line = lines.get(index);
            InvoiceItem item = new InvoiceItem();
            item.setId(UUID.randomUUID());
            item.setInvoiceId(invoiceId);
            item.setPosition(index);
            item.setDishId(line.dishId());
            item.setDishName(line.dishName());
            item.setUnitPrice(line.unitPrice());
            item.setQuantity(line.quantity());
            item.setLineTotal(line.unitPrice() * line.quantity());
            return item;
        }).toList());
        invoiceItemRepository.saveAll(items);
        return items;
    }

    // Thu ngân chỉ thấy hóa đơn hôm nay; ngoài phạm vi thì coi như không tồn tại để không lộ dữ liệu
    private Invoice findVisible(Role viewerRole, UUID id) {
        Invoice invoice = invoiceRepository.findById(id).orElseThrow(this::invoiceNotFound);
        if (viewerRole == Role.CASHIER && !LocalDate.ofInstant(invoice.getPaidAt(), clock.getZone()).equals(LocalDate.now(clock))) {
            throw invoiceNotFound();
        }
        return invoice;
    }

    private InvoiceResponse toResponse(Invoice invoice, boolean reprint) {
        return invoiceMapper.toResponse(invoice, invoiceItemRepository.findByInvoiceIdOrderByPosition(invoice.getId()), reprint);
    }

    private BusinessException invoiceNotFound() {
        return new BusinessException(ErrorCode.NOT_FOUND, "Không tìm thấy hóa đơn.");
    }

    private Instant startOf(LocalDate date) {
        return date.atStartOfDay(clock.getZone()).toInstant();
    }
}
