package com.restaurant.modules.billing.mapper;

import com.restaurant.common.config.CommonMapperConfig;
import com.restaurant.modules.billing.dto.response.InvoiceItemResponse;
import com.restaurant.modules.billing.dto.response.InvoiceResponse;
import com.restaurant.modules.billing.dto.response.InvoiceSummaryResponse;
import com.restaurant.modules.billing.dto.response.PaymentLineResponse;
import com.restaurant.modules.billing.entity.Invoice;
import com.restaurant.modules.billing.entity.InvoiceItem;
import com.restaurant.modules.order.dto.response.OrderItemResponse;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

import java.util.List;

@Mapper(config = CommonMapperConfig.class)
public interface InvoiceMapper {

    // Các dòng món và cờ in lại không nằm trên entity hóa đơn nên truyền riêng
    @Mapping(target = "items", source = "items")
    @Mapping(target = "reprint", source = "reprint")
    InvoiceResponse toResponse(Invoice invoice, List<InvoiceItem> items, boolean reprint);

    InvoiceItemResponse toItemResponse(InvoiceItem item);

    InvoiceSummaryResponse toSummaryResponse(Invoice invoice);

    PaymentLineResponse toPaymentLineResponse(OrderItemResponse item);
}
