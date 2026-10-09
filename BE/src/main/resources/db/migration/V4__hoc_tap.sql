-- Ghi danh: mỗi học viên một lần cho mỗi khóa học.
CREATE TABLE enrollments (
    id          uuid PRIMARY KEY,
    student_id  uuid        NOT NULL,
    course_id   uuid        NOT NULL,
    enrolled_at timestamptz NOT NULL,
    CONSTRAINT uq_enrollments_student_course UNIQUE (student_id, course_id)
);
CREATE INDEX ix_enrollments_course ON enrollments (course_id, enrolled_at DESC);

-- Xác nhận hoàn thành bài giảng; tiến độ khóa học tính từ bảng này khi đọc.
CREATE TABLE lecture_completions (
    id           uuid PRIMARY KEY,
    student_id   uuid        NOT NULL,
    course_id    uuid        NOT NULL,
    lecture_id   uuid        NOT NULL,
    completed_at timestamptz NOT NULL,
    CONSTRAINT uq_lecture_completions_student_lecture UNIQUE (student_id, lecture_id)
);
CREATE INDEX ix_lecture_completions_course ON lecture_completions (course_id, student_id);

CREATE TABLE submissions (
    id              uuid PRIMARY KEY,
    student_id      uuid        NOT NULL,
    exercise_id     uuid        NOT NULL,
    status          varchar(10) NOT NULL,
    score           integer,
    total_questions integer,
    submitted_at    timestamptz,
    created_at      timestamptz NOT NULL,
    updated_at      timestamptz NOT NULL,
    CONSTRAINT uq_submissions_student_exercise UNIQUE (student_id, exercise_id),
    CONSTRAINT ck_submissions_status CHECK (status IN ('DRAFT', 'SUBMITTED'))
);

CREATE TABLE submission_answers (
    id            uuid PRIMARY KEY,
    submission_id uuid    NOT NULL REFERENCES submissions (id),
    question_id   uuid    NOT NULL,
    answer_id     uuid    NOT NULL,
    is_correct    boolean,
    CONSTRAINT uq_submission_answers_question UNIQUE (submission_id, question_id)
);

CREATE TABLE comments (
    id         uuid PRIMARY KEY,
    lecture_id uuid        NOT NULL,
    user_id    uuid        NOT NULL,
    parent_id  uuid REFERENCES comments (id),
    content    text        NOT NULL,
    created_at timestamptz NOT NULL,
    updated_at timestamptz NOT NULL,
    deleted_at timestamptz
);
CREATE INDEX ix_comments_lecture_roots ON comments (lecture_id, created_at DESC) WHERE deleted_at IS NULL AND parent_id IS NULL;
CREATE INDEX ix_comments_parent ON comments (parent_id, created_at) WHERE deleted_at IS NULL;
