-- Đơn hàng của một bàn và các dòng món. Không đặt khóa ngoại sang module khác (bàn, món, nhân viên, đặt bàn).
CREATE TABLE orders (
    id             uuid PRIMARY KEY,
    table_id       uuid        NOT NULL,
    reservation_id uuid,
    customer_id    uuid,
    waiter_id      uuid        NOT NULL,
    status         varchar(20) NOT NULL,
    opened_at      timestamptz NOT NULL,
    closed_at      timestamptz,
    created_at     timestamptz NOT NULL,
    updated_at     timestamptz NOT NULL,
    CONSTRAINT ck_orders_status CHECK (status IN ('OPEN', 'CLOSED'))
);

-- Mỗi bàn tối đa một đơn đang mở: hai người cùng mở một bàn thì người thứ hai bị chặn ở đây.
CREATE UNIQUE INDEX uq_orders_open_table ON orders (table_id) WHERE status = 'OPEN';
CREATE INDEX ix_orders_status_opened ON orders (status, opened_at DESC);
CREATE INDEX ix_orders_waiter ON orders (waiter_id);

CREATE TABLE order_items (
    id         uuid PRIMARY KEY,
    order_id   uuid         NOT NULL REFERENCES orders (id),
    dish_id    uuid         NOT NULL,
    dish_name  varchar(255) NOT NULL,
    unit_price bigint       NOT NULL,
    quantity   integer      NOT NULL,
    note       varchar(255),
    status     varchar(20)  NOT NULL,
    sent_at    timestamptz  NOT NULL,
    started_at timestamptz,
    ready_at   timestamptz,
    served_at  timestamptz,
    chef_id    uuid,
    created_at timestamptz  NOT NULL,
    updated_at timestamptz  NOT NULL,
    CONSTRAINT ck_order_items_status CHECK (status IN ('PENDING', 'COOKING', 'READY', 'SERVED', 'CANCELLED')),
    CONSTRAINT ck_order_items_price CHECK (unit_price > 0),
    CONSTRAINT ck_order_items_quantity CHECK (quantity BETWEEN 1 AND 99)
);

CREATE INDEX ix_order_items_order ON order_items (order_id);
CREATE INDEX ix_order_items_queue ON order_items (sent_at) WHERE status IN ('PENDING', 'COOKING');
CREATE INDEX ix_order_items_ready ON order_items (ready_at) WHERE status = 'READY';
CREATE INDEX ix_order_items_dish ON order_items (dish_id);
CREATE INDEX ix_order_items_chef ON order_items (chef_id) WHERE chef_id IS NOT NULL;
