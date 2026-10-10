-- Bàn của nhà hàng. Tên bảng là dining_tables vì "table" là từ khóa SQL. Xóa cứng, chỉ khi không còn dữ liệu liên quan.
CREATE TABLE dining_tables (
    id         uuid PRIMARY KEY,
    name       varchar(20)  NOT NULL,
    zone       varchar(20)  NOT NULL,
    capacity   integer      NOT NULL,
    status     varchar(20)  NOT NULL DEFAULT 'AVAILABLE',
    note       varchar(255),
    version    bigint       NOT NULL DEFAULT 0,
    created_at timestamptz  NOT NULL,
    updated_at timestamptz  NOT NULL,
    CONSTRAINT ck_dining_tables_zone CHECK (zone IN ('INDOOR', 'OUTDOOR', 'VIP_ROOM', 'SECOND_FLOOR')),
    CONSTRAINT ck_dining_tables_status CHECK (status IN ('AVAILABLE', 'RESERVED', 'OCCUPIED', 'OUT_OF_SERVICE')),
    CONSTRAINT ck_dining_tables_capacity CHECK (capacity BETWEEN 1 AND 50)
);

-- Tên/số bàn duy nhất, không phân biệt hoa thường.
CREATE UNIQUE INDEX uq_dining_tables_name ON dining_tables (lower(name));
CREATE INDEX ix_dining_tables_zone_status ON dining_tables (zone, status);
