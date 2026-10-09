CREATE TABLE users (
    id            uuid PRIMARY KEY,
    email         varchar(255) NOT NULL,
    password_hash varchar(100) NOT NULL,
    full_name     varchar(255),
    phone         varchar(20),
    gender        varchar(10),
    date_of_birth date,
    avatar_url    varchar(500),
    role          varchar(20)  NOT NULL,
    status        varchar(20)  NOT NULL,
    created_at    timestamptz  NOT NULL,
    updated_at    timestamptz  NOT NULL,
    deleted_at    timestamptz,
    CONSTRAINT ck_users_role CHECK (role IN ('ADMIN', 'TEACHER', 'STUDENT')),
    CONSTRAINT ck_users_status CHECK (status IN ('ACTIVE', 'LOCKED')),
    CONSTRAINT ck_users_gender CHECK (gender IN ('MALE', 'FEMALE', 'OTHER'))
);

-- Email chỉ phải duy nhất trong các tài khoản chưa xóa mềm.
CREATE UNIQUE INDEX uq_users_email ON users (email) WHERE deleted_at IS NULL;
CREATE INDEX ix_users_role_created ON users (role, created_at DESC) WHERE deleted_at IS NULL;

CREATE TABLE password_reset_tokens (
    id         uuid PRIMARY KEY,
    user_id    uuid         NOT NULL,
    token_hash varchar(64)  NOT NULL,
    expires_at timestamptz  NOT NULL,
    used_at    timestamptz,
    created_at timestamptz  NOT NULL,
    CONSTRAINT uq_password_reset_token_hash UNIQUE (token_hash)
);

CREATE INDEX ix_password_reset_tokens_user ON password_reset_tokens (user_id);
