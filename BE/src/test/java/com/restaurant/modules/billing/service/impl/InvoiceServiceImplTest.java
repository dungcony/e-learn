package com.restaurant.modules.billing.service.impl;

import com.restaurant.common.code.DailyCodeGenerator;
import com.restaurant.common.config.RestaurantProperties;
import com.restaurant.common.exception.BusinessException;
import com.restaurant.common.exception.ErrorCode;
import com.restaurant.common.response.PageRequestParams;
import com.restaurant.modules.billing.dto.request.InvoiceCreateRequest;
import com.restaurant.modules.billing.dto.request.InvoiceSearchRequest;
import com.restaurant.modules.billing.dto.response.InvoiceResponse;
import com.restaurant.modules.billing.dto.response.PaymentSummaryResponse;
import com.restaurant.modules.billing.entity.Invoice;
import com.restaurant.modules.billing.entity.InvoiceItem;
import com.restaurant.modules.billing.enums.InvoiceStatus;
import com.restaurant.modules.billing.enums.PaymentMethod;
import com.restaurant.modules.billing.helper.InvoiceCalculator;
import com.restaurant.modules.billing.mapper.InvoiceMapperImpl;
import com.restaurant.modules.billing.repository.InvoiceItemRepository;
import com.restaurant.modules.billing.repository.InvoiceRepository;
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
import com.restaurant.modules.table.enums.TableZone;
import com.restaurant.modules.table.service.TableService;
import com.restaurant.modules.user.dto.response.UserBriefResponse;
import com.restaurant.modules.user.enums.Role;
import com.restaurant.modules.user.service.UserService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InOrder;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;

import java.math.BigDecimal;
import java.time.Clock;
import java.time.Duration;
import java.time.Instant;
import java.time.LocalDate;
import java.time.LocalTime;
import java.time.ZoneId;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.inOrder;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class InvoiceServiceImplTest {

    private static final ZoneId VN = ZoneId.of("Asia/Ho_Chi_Minh");
    // 09/10/2026 19:30 giờ Việt Nam
    private static final Instant NOW = Instant.parse("2026-10-09T12:30:00Z");

    @Mock
    private InvoiceRepository invoiceRepository;
    @Mock
    private InvoiceItemRepository invoiceItemRepository;
    @Mock
    private OrderService orderService;
    @Mock
    private TableService tableService;
    @Mock
    private UserService userService;
    @Mock
    private DailyCodeGenerator dailyCodeGenerator;

    private InvoiceServiceImpl service;
    private final UUID orderId = UUID.randomUUID();
    private final UUID tableId = UUID.randomUUID();
    private final UUID cashierId = UUID.randomUUID();
    private final UUID customerId = UUID.randomUUID();

    @BeforeEach
    void setUp() {
        RestaurantProperties properties = new RestaurantProperties(LocalTime.of(10, 0), LocalTime.of(22, 0),
                Duration.ofMinutes(120), Duration.ofMinutes(60), Duration.ofMinutes(15), Duration.ofHours(2), 30, 2, 4,
                new BigDecimal("0.08"), 366);
        service = new InvoiceServiceImpl(invoiceRepository, invoiceItemRepository, new InvoiceValidator(), new InvoiceCalculator(),
                new InvoiceMapperImpl(), orderService, tableService, userService, dailyCodeGenerator, properties, Clock.fixed(NOW, VN));
    }

    private OrderPaymentView view(int unserved, PaymentLine... lines) {
        return new OrderPaymentView(orderId, tableId, null, customerId, UUID.randomUUID(), NOW, List.of(lines), unserved);
    }

    private PaymentLine line(String name, long price, int qty, OrderItemStatus status) {
        return new PaymentLine(UUID.randomUUID(), name, price, qty, status);
    }

    private void stubSnapshotLookups() {
        when(tableService.getTableBriefs(List.of(tableId)))
                .thenReturn(List.of(new TableBriefResponse(tableId, "B05", TableZone.INDOOR, 6, TableStatus.OCCUPIED)));
        when(userService.getUserBriefs(List.of(cashierId)))
                .thenReturn(List.of(new UserBriefResponse(cashierId, "Trần Thị Bích", "bich@nhahang.vn", null, Role.CASHIER)));
        when(dailyCodeGenerator.next("HD", LocalDate.of(2026, 10, 9))).thenReturn("HD20261009-001");
    }

    private InvoiceCreateRequest cashRequest(Long received, boolean confirmUnserved) {
        return new InvoiceCreateRequest(orderId, PaymentMethod.CASH, received, "Xuất hóa đơn công ty", confirmUnserved);
    }

    private Invoice invoice(Instant paidAt, UUID customer) {
        Invoice i = new Invoice();
        i.setId(UUID.randomUUID());
        i.setCode("HD20261009-001");
        i.setOrderId(orderId);
        i.setTableId(tableId);
        i.setTableName("B05");
        i.setCustomerId(customer);
        i.setCashierId(cashierId);
        i.setCashierName("Trần Thị Bích");
        i.setSubtotal(160000);
        i.setVatRate(new BigDecimal("0.0800"));
        i.setVatAmount(12800);
        i.setTotalAmount(172800);
        i.setPaymentMethod(PaymentMethod.CASH);
        i.setAmountReceived(200000);
        i.setChangeAmount(27200);
        i.setStatus(InvoiceStatus.PAID);
        i.setPaidAt(paidAt);
        return i;
    }

    // ---- xem trước khi trả tiền ----

    @Test
    void getPaymentSummary_lists_chargeable_lines_with_vat_and_the_unserved_warning() {
        OrderItemResponse served = new OrderItemResponse(UUID.randomUUID(), UUID.randomUUID(), "Phở bò", 65000, 2, 130000, null,
                OrderItemStatus.SERVED, NOW, NOW, NOW, NOW);
        OrderItemResponse pending = new OrderItemResponse(UUID.randomUUID(), UUID.randomUUID(), "Trà đá", 30000, 1, 30000, null,
                OrderItemStatus.PENDING, NOW, null, null, null);
        OrderItemResponse cancelled = new OrderItemResponse(UUID.randomUUID(), UUID.randomUUID(), "Món hủy", 99000, 1, 99000, null,
                OrderItemStatus.CANCELLED, NOW, null, null, null);
        TableBriefResponse table = new TableBriefResponse(tableId, "B05", TableZone.INDOOR, 6, TableStatus.OCCUPIED);
        when(orderService.getOrder(orderId)).thenReturn(new OrderResponse(orderId, table, OrderStatus.OPEN, null, null, NOW, null,
                List.of(served, pending, cancelled), 160000, 1));

        PaymentSummaryResponse summary = service.getPaymentSummary(orderId);

        assertThat(summary.lines()).extracting(l -> l.dishName()).containsExactly("Phở bò", "Trà đá");
        assertThat(summary.subtotal()).isEqualTo(160000L);
        assertThat(summary.vatRate()).isEqualByComparingTo("0.08");
        assertThat(summary.vatAmount()).isEqualTo(12800L);
        assertThat(summary.totalAmount()).isEqualTo(172800L);
        assertThat(summary.unservedCount()).isEqualTo(1);
        assertThat(summary.table().name()).isEqualTo("B05");
    }

    @Test
    void getPaymentSummary_rejects_a_closed_order() {
        TableBriefResponse table = new TableBriefResponse(tableId, "B05", TableZone.INDOOR, 6, TableStatus.AVAILABLE);
        when(orderService.getOrder(orderId)).thenReturn(new OrderResponse(orderId, table, OrderStatus.CLOSED, null, null, NOW, NOW,
                List.of(), 0, 0));

        assertThatThrownBy(() -> service.getPaymentSummary(orderId))
                .isInstanceOf(BusinessException.class)
                .extracting("code").isEqualTo("ORDER_NOT_OPEN");
    }

    // ---- thanh toán ----

    @Test
    void createInvoice_cash_saves_a_snapshot_closes_the_order_and_frees_the_table() {
        when(orderService.lockOrderForPayment(orderId)).thenReturn(view(0,
                line("Phở bò", 65000, 2, OrderItemStatus.SERVED), line("Trà đá", 30000, 1, OrderItemStatus.SERVED)));
        stubSnapshotLookups();
        when(invoiceRepository.saveAndFlush(any(Invoice.class))).thenAnswer(inv -> inv.getArgument(0));

        InvoiceResponse response = service.createInvoice(cashierId, cashRequest(200000L, false));

        ArgumentCaptor<Invoice> invoiceCaptor = ArgumentCaptor.forClass(Invoice.class);
        verify(invoiceRepository).saveAndFlush(invoiceCaptor.capture());
        Invoice saved = invoiceCaptor.getValue();
        assertThat(saved.getCode()).isEqualTo("HD20261009-001");
        assertThat(saved.getOrderId()).isEqualTo(orderId);
        assertThat(saved.getTableName()).isEqualTo("B05");
        assertThat(saved.getCashierName()).isEqualTo("Trần Thị Bích");
        assertThat(saved.getCustomerId()).isEqualTo(customerId);
        assertThat(saved.getSubtotal()).isEqualTo(160000L);
        assertThat(saved.getVatRate()).isEqualByComparingTo("0.08");
        assertThat(saved.getVatAmount()).isEqualTo(12800L);
        assertThat(saved.getTotalAmount()).isEqualTo(172800L);
        assertThat(saved.getAmountReceived()).isEqualTo(200000L);
        assertThat(saved.getChangeAmount()).isEqualTo(27200L);
        assertThat(saved.getStatus()).isEqualTo(InvoiceStatus.PAID);
        assertThat(saved.getPaidAt()).isEqualTo(NOW);
        assertThat(saved.getNote()).isEqualTo("Xuất hóa đơn công ty");

        @SuppressWarnings("unchecked")
        ArgumentCaptor<List<InvoiceItem>> itemsCaptor = ArgumentCaptor.forClass(List.class);
        verify(invoiceItemRepository).saveAll(itemsCaptor.capture());
        assertThat(itemsCaptor.getValue()).extracting(InvoiceItem::getDishName).containsExactly("Phở bò", "Trà đá");
        assertThat(itemsCaptor.getValue()).extracting(InvoiceItem::getPosition).containsExactly(0, 1);
        assertThat(itemsCaptor.getValue()).extracting(InvoiceItem::getLineTotal).containsExactly(130000L, 30000L);

        InOrder order = inOrder(orderService, tableService);
        order.verify(orderService).lockOrderForPayment(orderId);
        order.verify(orderService).closeOrder(orderId);
        order.verify(tableService).transitionStatus(tableId, Set.of(TableStatus.OCCUPIED), TableStatus.AVAILABLE);
        assertThat(response.totalAmount()).isEqualTo(172800L);
        assertThat(response.reprint()).isFalse();
    }

    @Test
    void createInvoice_card_ignores_the_amount_and_records_exact_payment() {
        when(orderService.lockOrderForPayment(orderId)).thenReturn(view(0, line("Phở bò", 65000, 2, OrderItemStatus.SERVED)));
        stubSnapshotLookups();
        when(invoiceRepository.saveAndFlush(any(Invoice.class))).thenAnswer(inv -> inv.getArgument(0));

        service.createInvoice(cashierId, new InvoiceCreateRequest(orderId, PaymentMethod.CARD, 1L, null, false));

        ArgumentCaptor<Invoice> captor = ArgumentCaptor.forClass(Invoice.class);
        verify(invoiceRepository).saveAndFlush(captor.capture());
        assertThat(captor.getValue().getAmountReceived()).isEqualTo(captor.getValue().getTotalAmount());
        assertThat(captor.getValue().getChangeAmount()).isZero();
    }

    @Test
    void createInvoice_drops_cancelled_lines_from_the_invoice() {
        when(orderService.lockOrderForPayment(orderId)).thenReturn(view(0,
                line("Phở bò", 65000, 1, OrderItemStatus.SERVED), line("Món hủy", 99000, 1, OrderItemStatus.CANCELLED)));
        stubSnapshotLookups();
        when(invoiceRepository.saveAndFlush(any(Invoice.class))).thenAnswer(inv -> inv.getArgument(0));

        service.createInvoice(cashierId, cashRequest(100000L, false));

        @SuppressWarnings("unchecked")
        ArgumentCaptor<List<InvoiceItem>> captor = ArgumentCaptor.forClass(List.class);
        verify(invoiceItemRepository).saveAll(captor.capture());
        assertThat(captor.getValue()).extracting(InvoiceItem::getDishName).containsExactly("Phở bò");
    }

    @Test
    void createInvoice_rejects_an_order_without_chargeable_lines() {
        when(orderService.lockOrderForPayment(orderId)).thenReturn(view(0, line("Món hủy", 99000, 1, OrderItemStatus.CANCELLED)));

        assertThatThrownBy(() -> service.createInvoice(cashierId, cashRequest(100000L, false)))
                .isInstanceOf(BusinessException.class)
                .extracting("code").isEqualTo("INVOICE_ORDER_EMPTY");
        verify(orderService, never()).closeOrder(any());
    }

    @Test
    void createInvoice_asks_for_confirmation_when_lines_are_not_served_yet() {
        when(orderService.lockOrderForPayment(orderId)).thenReturn(view(1, line("Phở bò", 65000, 1, OrderItemStatus.READY)));

        assertThatThrownBy(() -> service.createInvoice(cashierId, cashRequest(100000L, false)))
                .isInstanceOfSatisfying(BusinessException.class, e -> {
                    assertThat(e.getCode()).isEqualTo("INVOICE_UNSERVED_ITEMS");
                    assertThat(e.getDetail()).isEqualTo(Map.of("unserved_count", 1));
                });
        verify(invoiceRepository, never()).saveAndFlush(any());
    }

    @Test
    void createInvoice_goes_ahead_once_the_cashier_confirms_unserved_lines() {
        when(orderService.lockOrderForPayment(orderId)).thenReturn(view(1, line("Phở bò", 65000, 1, OrderItemStatus.READY)));
        stubSnapshotLookups();
        when(invoiceRepository.saveAndFlush(any(Invoice.class))).thenAnswer(inv -> inv.getArgument(0));

        service.createInvoice(cashierId, cashRequest(100000L, true));

        verify(orderService).closeOrder(orderId);
    }

    @Test
    void createInvoice_rejects_cash_that_does_not_cover_the_total() {
        when(orderService.lockOrderForPayment(orderId)).thenReturn(view(0, line("Phở bò", 65000, 2, OrderItemStatus.SERVED)));

        assertThatThrownBy(() -> service.createInvoice(cashierId, cashRequest(100000L, false)))
                .isInstanceOf(BusinessException.class)
                .extracting("code").isEqualTo("INVOICE_AMOUNT_INSUFFICIENT");
        verify(invoiceRepository, never()).saveAndFlush(any());
        verify(orderService, never()).closeOrder(any());
    }

    @Test
    void createInvoice_cash_without_an_amount_is_a_validation_error() {
        when(orderService.lockOrderForPayment(orderId)).thenReturn(view(0, line("Phở bò", 65000, 2, OrderItemStatus.SERVED)));

        assertThatThrownBy(() -> service.createInvoice(cashierId, cashRequest(null, false)))
                .isInstanceOf(BusinessException.class)
                .extracting("code").isEqualTo("VALIDATION_ERROR");
    }

    @Test
    void createInvoice_propagates_a_closed_or_missing_order() {
        when(orderService.lockOrderForPayment(orderId)).thenThrow(new BusinessException(ErrorCode.ORDER_NOT_OPEN));

        assertThatThrownBy(() -> service.createInvoice(cashierId, cashRequest(100000L, false)))
                .isInstanceOf(BusinessException.class)
                .extracting("code").isEqualTo("ORDER_NOT_OPEN");
    }

    @Test
    void createInvoice_maps_a_second_invoice_for_the_same_order_to_order_not_open() {
        when(orderService.lockOrderForPayment(orderId)).thenReturn(view(0, line("Phở bò", 65000, 2, OrderItemStatus.SERVED)));
        stubSnapshotLookups();
        when(invoiceRepository.saveAndFlush(any(Invoice.class))).thenThrow(new DataIntegrityViolationException("uq_invoices_order"));

        assertThatThrownBy(() -> service.createInvoice(cashierId, cashRequest(200000L, false)))
                .isInstanceOf(BusinessException.class)
                .extracting("code").isEqualTo("ORDER_NOT_OPEN");
        verify(orderService, never()).closeOrder(any());
    }

    // ---- lịch sử và in lại ----

    @Test
    void getInvoice_cashier_only_sees_todays_invoices() {
        Invoice yesterday = invoice(Instant.parse("2026-10-08T12:00:00Z"), null);
        when(invoiceRepository.findById(yesterday.getId())).thenReturn(Optional.of(yesterday));

        assertThatThrownBy(() -> service.getInvoice(Role.CASHIER, yesterday.getId()))
                .isInstanceOf(BusinessException.class)
                .extracting("code").isEqualTo("NOT_FOUND");
    }

    @Test
    void getInvoice_cashier_sees_an_invoice_paid_earlier_today_and_manager_sees_any() {
        Invoice today = invoice(Instant.parse("2026-10-09T03:00:00Z"), null);
        Invoice old = invoice(Instant.parse("2026-09-01T03:00:00Z"), null);
        when(invoiceRepository.findById(today.getId())).thenReturn(Optional.of(today));
        when(invoiceRepository.findById(old.getId())).thenReturn(Optional.of(old));
        when(invoiceItemRepository.findByInvoiceIdOrderByPosition(any(UUID.class))).thenReturn(List.of());

        assertThat(service.getInvoice(Role.CASHIER, today.getId()).code()).isEqualTo("HD20261009-001");
        assertThat(service.getInvoice(Role.MANAGER, old.getId()).code()).isEqualTo("HD20261009-001");
    }

    @Test
    void getInvoice_unknown_id_is_not_found() {
        UUID id = UUID.randomUUID();
        when(invoiceRepository.findById(id)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> service.getInvoice(Role.MANAGER, id))
                .isInstanceOf(BusinessException.class)
                .extracting("code").isEqualTo("NOT_FOUND");
    }

    @Test
    void reprintInvoice_counts_the_print_and_marks_the_response_as_reprint() {
        Invoice invoice = invoice(Instant.parse("2026-10-09T03:00:00Z"), null);
        when(invoiceRepository.findById(invoice.getId())).thenReturn(Optional.of(invoice));
        when(invoiceItemRepository.findByInvoiceIdOrderByPosition(invoice.getId())).thenReturn(List.of());
        when(invoiceRepository.incrementPrintCount(invoice.getId())).thenReturn(1);

        InvoiceResponse response = service.reprintInvoice(Role.CASHIER, invoice.getId());

        assertThat(response.reprint()).isTrue();
        verify(invoiceRepository).incrementPrintCount(invoice.getId());
    }

    @Test
    void reprintInvoice_does_not_count_a_print_the_viewer_cannot_see() {
        Invoice yesterday = invoice(Instant.parse("2026-10-08T12:00:00Z"), null);
        when(invoiceRepository.findById(yesterday.getId())).thenReturn(Optional.of(yesterday));

        assertThatThrownBy(() -> service.reprintInvoice(Role.CASHIER, yesterday.getId()))
                .isInstanceOf(BusinessException.class)
                .extracting("code").isEqualTo("NOT_FOUND");
        verify(invoiceRepository, never()).incrementPrintCount(any());
    }

    @SuppressWarnings("unchecked")
    @Test
    void searchInvoices_returns_summaries_and_rejects_an_inverted_range() {
        Invoice invoice = invoice(NOW, null);
        when(invoiceRepository.findAll(any(Specification.class), any(Pageable.class))).thenReturn(new PageImpl<>(List.of(invoice)));

        var page = service.searchInvoices(Role.MANAGER, new InvoiceSearchRequest("hd", null, null, "b05", PaymentMethod.CASH),
                PageRequestParams.of(null, null, null, null));

        assertThat(page.getContent()).singleElement().extracting(s -> s.totalAmount()).isEqualTo(172800L);
        assertThatThrownBy(() -> service.searchInvoices(Role.MANAGER,
                new InvoiceSearchRequest(null, LocalDate.of(2026, 10, 9), LocalDate.of(2026, 10, 1), null, null),
                PageRequestParams.of(null, null, null, null)))
                .isInstanceOf(BusinessException.class)
                .extracting("code").isEqualTo("VALIDATION_ERROR");
    }

    // ---- hóa đơn của tôi ----

    @Test
    void getMine_returns_only_the_customers_invoice() {
        Invoice mine = invoice(NOW, customerId);
        when(invoiceRepository.findByIdAndCustomerId(mine.getId(), customerId)).thenReturn(Optional.of(mine));
        when(invoiceItemRepository.findByInvoiceIdOrderByPosition(mine.getId())).thenReturn(List.of());
        UUID other = UUID.randomUUID();
        when(invoiceRepository.findByIdAndCustomerId(other, customerId)).thenReturn(Optional.empty());

        assertThat(service.getMine(customerId, mine.getId()).id()).isEqualTo(mine.getId());
        assertThatThrownBy(() -> service.getMine(customerId, other))
                .isInstanceOf(BusinessException.class)
                .extracting("code").isEqualTo("NOT_FOUND");
    }
}
