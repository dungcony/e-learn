-- Món ăn của thực đơn. Xóa cứng, nhưng chỉ khi chưa từng được gọi (kiểm ở DishDeletionGuard).
CREATE TABLE dishes (
    id                uuid PRIMARY KEY,
    name              varchar(255)  NOT NULL,
    category          varchar(20)   NOT NULL,
    price             bigint        NOT NULL,
    unit              varchar(20)   NOT NULL,
    description       varchar(1000),
    prep_time_minutes integer,
    image_url         varchar(500),
    status            varchar(20)   NOT NULL,
    created_at        timestamptz   NOT NULL,
    updated_at        timestamptz   NOT NULL,
    CONSTRAINT ck_dishes_category CHECK (category IN ('APPETIZER', 'MAIN_COURSE', 'DRINK', 'DESSERT')),
    CONSTRAINT ck_dishes_status CHECK (status IN ('AVAILABLE', 'OUT_OF_STOCK', 'DISCONTINUED')),
    CONSTRAINT ck_dishes_price CHECK (price > 0),
    CONSTRAINT ck_dishes_prep_time CHECK (prep_time_minutes IS NULL OR prep_time_minutes > 0)
);

-- Tên món duy nhất trong cùng danh mục, không phân biệt hoa thường.
CREATE UNIQUE INDEX uq_dishes_category_name ON dishes (category, lower(name));
CREATE INDEX ix_dishes_status_category ON dishes (status, category);
