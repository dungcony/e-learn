package com.restaurant.modules.order.mapper;

import com.restaurant.common.config.CommonMapperConfig;
import com.restaurant.modules.order.dto.response.OrderItemResponse;
import com.restaurant.modules.order.dto.response.PaymentLine;
import com.restaurant.modules.order.entity.OrderItem;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(config = CommonMapperConfig.class)
public interface OrderMapper {

    @Mapping(target = "lineTotal", expression = "java(item.getUnitPrice() * item.getQuantity())")
    OrderItemResponse toItemResponse(OrderItem item);

    PaymentLine toPaymentLine(OrderItem item);
}
