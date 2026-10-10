package com.restaurant.modules.billing.enums;

/** Phương thức thanh toán; chỉ {@code CASH} cần nhập số tiền khách đưa, hai loại còn lại coi như trả đúng tổng tiền. */
public enum PaymentMethod {
    CASH, CARD, BANK_TRANSFER
}
