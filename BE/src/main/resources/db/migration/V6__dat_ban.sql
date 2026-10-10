-- btree_gist để ràng buộc loại trừ dùng được cột uuid cùng khoảng thời gian
CREATE EXTENSION IF NOT EXISTS btree_gist;

-- Bộ đếm mã đọc được theo ngày (đặt bàn DB..., hóa đơn HD...), tăng bằng một câu INSERT ... ON CONFLICT nguyên tử
CREATE TABLE daily_code_counters (
    prefix     varchar(10) NOT NULL,
    day        date        NOT NULL,
    last_value integer     NOT NULL,
    PRIMARY KEY (prefix, day)
);

CREATE TABLE reservations (
    id             uuid PRIMARY KEY,
    code           varchar(20)  NOT NULL,
    customer_id    uuid,
    guest_name     varchar(255) NOT NULL,
    phone          varchar(10)  NOT NULL,
    email          varchar(255),
    reserved_at    timestamptz  NOT NULL,
    reserved_end   timestamptz  NOT NULL,
    guest_count    integer      NOT NULL,
    preferred_zone varchar(20),
    note           varchar(255),
    status         varchar(20)  NOT NULL,
    table_id       uuid,
    cancel_reason  varchar(255),
    confirmed_by   uuid,
    created_at     timestamptz  NOT NULL,
    updated_at     timestamptz  NOT NULL,
    CONSTRAINT uq_reservations_code UNIQUE (code),
    CONSTRAINT ck_reservations_status CHECK (status IN ('PENDING', 'CONFIRMED', 'CHECKED_IN', 'CANCELLED', 'NO_SHOW')),
    CONSTRAINT ck_reservations_zone CHECK (preferred_zone IS NULL OR preferred_zone IN ('INDOOR', 'OUTDOOR', 'VIP_ROOM', 'SECOND_FLOOR')),
    CONSTRAINT ck_reservations_guest_count CHECK (guest_count BETWEEN 1 AND 50),
    CONSTRAINT ck_reservations_time CHECK (reserved_end > reserved_at)
);

CREATE INDEX ix_reservations_reserved_at ON reservations (reserved_at, status);
CREATE INDEX ix_reservations_customer ON reservations (customer_id, created_at DESC) WHERE customer_id IS NOT NULL;
CREATE INDEX ix_reservations_table ON reservations (table_id) WHERE table_id IS NOT NULL;

-- Một bàn không thể có hai đặt bàn đã xác nhận chồng khung giờ; khoảng nửa mở nên 19:00 và 21:00 nối tiếp không đụng nhau.
-- Đặt bàn chờ xác nhận chưa có bàn nên không bị kiểm.
ALTER TABLE reservations ADD CONSTRAINT ex_reservations_table_no_overlap
    EXCLUDE USING gist (table_id WITH =, tstzrange(reserved_at, reserved_end) WITH &&)
    WHERE (table_id IS NOT NULL AND status IN ('CONFIRMED', 'CHECKED_IN'));
