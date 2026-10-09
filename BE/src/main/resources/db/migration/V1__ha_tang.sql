-- Bảng chống ghi trùng cho các POST tạo mới có header Idempotency-Key (common/idempotency).
CREATE TABLE idempotency_keys (
    id              uuid PRIMARY KEY,
    idempotency_key varchar(255) NOT NULL,
    user_id         uuid         NOT NULL,
    endpoint        varchar(255) NOT NULL,
    status          varchar(20)  NOT NULL,
    response_status integer,
    response_body   jsonb,
    created_at      timestamptz  NOT NULL,
    CONSTRAINT uq_idem_scope UNIQUE (idempotency_key, user_id, endpoint),
    CONSTRAINT ck_idem_status CHECK (status IN ('processing', 'completed'))
);
