-- Hóa đơn là bản chụp bất biến của đơn lúc thanh toán; không đặt khóa ngoại sang module khác (đơn, bàn, khách, thu ngân).
CREATE TABLE invoices (
    id              uuid PRIMARY KEY,
    code            varchar(20)   NOT NULL,
    order_id        uuid          NOT NULL,
    table_id        uuid          NOT NULL,
    table_name      varchar(20)   NOT NULL,
    customer_id     uuid,
    cashier_id      uuid          NOT NULL,
    cashier_name    varchar(255)  NOT NULL,
    subtotal        bigint        NOT NULL,
    vat_rate        numeric(5, 4) NOT NULL,
    vat_amount      bigint        NOT NULL,
    total_amount    bigint        NOT NULL,
    payment_method  varchar(20)   NOT NULL,
    amount_received bigint        NOT NULL,
    change_amount   bigint        NOT NULL,
    note            varchar(255),
    status          varchar(20)   NOT NULL,
    paid_at         timestamptz   NOT NULL,
    print_count     integer       NOT NULL DEFAULT 0,
    created_at      timestamptz   NOT NULL,
    updated_at      timestamptz   NOT NULL,
    CONSTRAINT uq_invoices_code UNIQUE (code),
    -- Một đơn một hóa đơn: lớp chặn cuối nếu có đường nào bỏ sót khóa đơn
    CONSTRAINT uq_invoices_order UNIQUE (order_id),
    CONSTRAINT ck_invoices_payment_method CHECK (payment_method IN ('CASH', 'CARD', 'BANK_TRANSFER')),
    CONSTRAINT ck_invoices_status CHECK (status IN ('PAID')),
    CONSTRAINT ck_invoices_total CHECK (total_amount = subtotal + vat_amount),
    CONSTRAINT ck_invoices_change CHECK (change_amount >= 0)
);

CREATE INDEX ix_invoices_paid_at ON invoices (paid_at DESC);
CREATE INDEX ix_invoices_customer ON invoices (customer_id, paid_at DESC) WHERE customer_id IS NOT NULL;
CREATE INDEX ix_invoices_cashier ON invoices (cashier_id);

-- Mỗi dòng món không bị hủy của đơn thành đúng một dòng ở đây (không gộp), position giữ thứ tự gọi món
CREATE TABLE invoice_items (
    id         uuid PRIMARY KEY,
    invoice_id uuid         NOT NULL REFERENCES invoices (id),
    position   integer      NOT NULL,
    dish_id    uuid         NOT NULL,
    dish_name  varchar(255) NOT NULL,
    unit_price bigint       NOT NULL,
    quantity   integer      NOT NULL,
    line_total bigint       NOT NULL
);

CREATE INDEX ix_invoice_items_invoice ON invoice_items (invoice_id, position);
CREATE INDEX ix_invoice_items_dish ON invoice_items (dish_id);
