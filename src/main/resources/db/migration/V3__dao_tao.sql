-- Thể loại: tên duy nhất (không phân biệt hoa thường) trong các bản ghi chưa xóa mềm.
CREATE TABLE categories (
    id         uuid PRIMARY KEY,
    name       varchar(255) NOT NULL,
    created_by uuid         NOT NULL,
    created_at timestamptz  NOT NULL,
    updated_at timestamptz  NOT NULL,
    deleted_at timestamptz
);
CREATE UNIQUE INDEX uq_categories_name ON categories (lower(name)) WHERE deleted_at IS NULL;

CREATE TABLE courses (
    id                 uuid PRIMARY KEY,
    code               varchar(8)    NOT NULL,
    title              varchar(255)  NOT NULL,
    description        text          NOT NULL,
    price              numeric(12,2) NOT NULL DEFAULT 0,
    start_date         date          NOT NULL,
    end_date           date          NOT NULL,
    status             varchar(10)   NOT NULL,
    image_url          varchar(500),
    reference_materials text,
    category_id        uuid          NOT NULL REFERENCES categories (id),
    teacher_id         uuid          NOT NULL,
    created_at         timestamptz   NOT NULL,
    updated_at         timestamptz   NOT NULL,
    deleted_at         timestamptz,
    CONSTRAINT uq_courses_code UNIQUE (code),
    CONSTRAINT ck_courses_status CHECK (status IN ('PUBLIC', 'PRIVATE')),
    CONSTRAINT ck_courses_price CHECK (price >= 0),
    CONSTRAINT ck_courses_dates CHECK (end_date > start_date)
);
CREATE INDEX ix_courses_teacher ON courses (teacher_id) WHERE deleted_at IS NULL;
CREATE INDEX ix_courses_category ON courses (category_id) WHERE deleted_at IS NULL;
CREATE INDEX ix_courses_public ON courses (created_at DESC) WHERE deleted_at IS NULL AND status = 'PUBLIC';

CREATE TABLE lectures (
    id          uuid PRIMARY KEY,
    course_id   uuid          NOT NULL REFERENCES courses (id),
    title       varchar(255)  NOT NULL,
    description text,
    content_url varchar(1000) NOT NULL,
    created_by  uuid          NOT NULL,
    created_at  timestamptz   NOT NULL,
    updated_at  timestamptz   NOT NULL,
    deleted_at  timestamptz
);
CREATE INDEX ix_lectures_course ON lectures (course_id, created_at) WHERE deleted_at IS NULL;

CREATE TABLE exercises (
    id          uuid PRIMARY KEY,
    lecture_id  uuid         NOT NULL REFERENCES lectures (id),
    title       varchar(255) NOT NULL,
    description text         NOT NULL,
    created_by  uuid         NOT NULL,
    created_at  timestamptz  NOT NULL,
    updated_at  timestamptz  NOT NULL,
    deleted_at  timestamptz
);
CREATE INDEX ix_exercises_lecture ON exercises (lecture_id, created_at) WHERE deleted_at IS NULL;

CREATE TABLE questions (
    id          uuid PRIMARY KEY,
    exercise_id uuid    NOT NULL REFERENCES exercises (id),
    content     text    NOT NULL,
    position    integer NOT NULL,
    deleted_at  timestamptz
);
CREATE INDEX ix_questions_exercise ON questions (exercise_id, position) WHERE deleted_at IS NULL;

CREATE TABLE answers (
    id          uuid PRIMARY KEY,
    question_id uuid    NOT NULL REFERENCES questions (id),
    content     text    NOT NULL,
    is_correct  boolean NOT NULL,
    position    integer NOT NULL,
    deleted_at  timestamptz
);
CREATE INDEX ix_answers_question ON answers (question_id, position) WHERE deleted_at IS NULL;
