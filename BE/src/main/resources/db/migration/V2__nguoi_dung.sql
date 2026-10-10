-- Tài khoản mọi vai trò: khách hàng tự đăng ký, nhân viên và quản lý do Quản lý tạo.
CREATE TABLE users (
    id                   uuid PRIMARY KEY,
    email                varchar(255) NOT NULL,
    password_hash        varchar(100) NOT NULL,
    full_name            varchar(255) NOT NULL,
    phone                varchar(10),
    gender               varchar(10),
    date_of_birth        date,
    avatar_url           varchar(500),
    role                 varchar(20)  NOT NULL,
    status               varchar(20)  NOT NULL,
    email_verified       boolean      NOT NULL DEFAULT false,
    must_change_password boolean      NOT NULL DEFAULT false,
    created_at           timestamptz  NOT NULL,
    updated_at           timestamptz  NOT NULL,
    deleted_at           timestamptz,
    CONSTRAINT ck_users_role CHECK (role IN ('CUSTOMER', 'WAITER', 'CASHIER', 'CHEF', 'MANAGER')),
    CONSTRAINT ck_users_status CHECK (status IN ('ACTIVE', 'LOCKED')),
    CONSTRAINT ck_users_gender CHECK (gender IN ('MALE', 'FEMALE', 'OTHER'))
);

-- Email chỉ phải duy nhất trong các tài khoản chưa xóa mềm, không phân biệt hoa thường.
CREATE UNIQUE INDEX uq_users_email ON users (lower(email)) WHERE deleted_at IS NULL;
CREATE INDEX ix_users_role_created ON users (role, created_at DESC) WHERE deleted_at IS NULL;

-- Token một lần cho xác thực email (24 giờ) và đặt lại mật khẩu (60 phút); chỉ lưu băm SHA-256.
CREATE TABLE user_tokens (
    id         uuid PRIMARY KEY,
    user_id    uuid         NOT NULL,
    type       varchar(30)  NOT NULL,
    token_hash varchar(64)  NOT NULL,
    expires_at timestamptz  NOT NULL,
    used_at    timestamptz,
    created_at timestamptz  NOT NULL,
    CONSTRAINT uq_user_tokens_hash UNIQUE (token_hash),
    CONSTRAINT ck_user_tokens_type CHECK (type IN ('EMAIL_VERIFICATION', 'PASSWORD_RESET'))
);

CREATE INDEX ix_user_tokens_user_type ON user_tokens (user_id, type);
