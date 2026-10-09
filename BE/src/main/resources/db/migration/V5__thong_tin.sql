CREATE TABLE news (
    id         uuid PRIMARY KEY,
    title      varchar(255) NOT NULL,
    content    text         NOT NULL,
    author_id  uuid         NOT NULL,
    created_at timestamptz  NOT NULL,
    updated_at timestamptz  NOT NULL,
    deleted_at timestamptz
);
CREATE INDEX ix_news_created ON news (created_at DESC) WHERE deleted_at IS NULL;

CREATE TABLE faqs (
    id         uuid PRIMARY KEY,
    question   text        NOT NULL,
    answer     text        NOT NULL,
    created_by uuid        NOT NULL,
    created_at timestamptz NOT NULL,
    updated_at timestamptz NOT NULL,
    deleted_at timestamptz
);
CREATE INDEX ix_faqs_created ON faqs (created_at DESC) WHERE deleted_at IS NULL;
