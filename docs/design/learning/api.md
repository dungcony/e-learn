# Thiết kế API & Biểu đồ tuần tự: Module Học tập

Quy ước URL, response, phân trang và mã lỗi: xem [`../README.md`](../README.md). Điều kiện học tập (khóa `PUBLIC`, đã ghi
danh, đã tới ngày bắt đầu): xem [`use-case.md`](use-case.md).

## 1. Mô tả API Endpoints

### Học viên (UC016)

| Method | URL | Request | Response (`data`) |
|---|---|---|---|
| POST | `/courses/{courseId}/enrollments` | — | 201 `EnrollmentResponse` |
| GET | `/my-courses?name=&code=` | Query + phân trang | 200 `{items: EnrollmentResponse[], meta}` |
| GET | `/my-courses/{courseId}/lectures` | — | 200 `MyLectureSummaryResponse[]` |
| GET | `/my-courses/{courseId}/lectures/{id}` | — | 200 `MyLectureDetailResponse` |
| PUT | `/lectures/{lectureId}/completion` | — | 200 `null` |
| PUT | `/exercises/{exerciseId}/submission` | `SubmissionSaveRequest` | 200 `SubmissionResponse` |
| PUT | `/exercises/{exerciseId}/submission/status` | `SubmissionStatusUpdateRequest` | 200 `SubmissionResponse` |
| GET | `/exercises/{exerciseId}/submission` | — | 200 `SubmissionResponse` |
| GET | `/lectures/{lectureId}/comments` | Phân trang theo bình luận gốc | 200 `{items: CommentResponse[], meta}` |
| POST | `/lectures/{lectureId}/comments` | `CommentCreateRequest` | 201 `CommentResponse` |
| PUT | `/comments/{id}` | `CommentUpdateRequest` | 200 `CommentResponse` |
| DELETE | `/comments/{id}` | — | 200 `null` |

Mọi endpoint ở bảng trên chỉ dành cho `STUDENT`.

### Giảng viên, Quản trị viên (UC014)

| Method | URL | Vai trò | Request | Response (`data`) |
|---|---|---|---|---|
| GET | `/course-histories?name=&code=` | `TEACHER`, `ADMIN` | Query + phân trang | 200 `{items: CourseHistoryResponse[], meta}` |
| GET | `/course-histories/{courseId}/students` | `TEACHER` chủ khóa học, `ADMIN` | Phân trang | 200 `{items: EnrolledStudentResponse[], meta}` |

Ví dụ lưu tạm `PUT /exercises/{exerciseId}/submission`:

```json
{ "answers": [ { "question_id": "8b2e...", "answer_id": "c41a..." } ] }
```

Ví dụ nộp bài `PUT /exercises/{exerciseId}/submission/status`:

```json
{ "status": "SUBMITTED" }
```

## 2. Biểu đồ Tuần tự (Sequence Diagram) - Ghi danh Khóa học (UC016)

```mermaid
sequenceDiagram
    actor Student
    participant Ctrl as EnrollmentController
    participant Svc as EnrollmentServiceImpl
    participant Course as CourseService
    participant Repo as EnrollmentRepository
    participant Handler as GlobalExceptionHandler

    Student->>Ctrl: POST /courses/{courseId}/enrollments
    Ctrl->>Svc: enroll(studentId, courseId)
    Svc->>Course: getCourseBrief(courseId)

    alt Không tồn tại, đã xóa hoặc PRIVATE
        Svc->>Handler: BusinessException(NOT_FOUND)
        Handler-->>Student: 404 NOT_FOUND
    else Khóa PUBLIC
        Svc->>Repo: existsByStudentIdAndCourseId(studentId, courseId)
        alt Đã ghi danh
            Svc->>Handler: BusinessException(ENROLLMENT_ALREADY_EXISTS)
            Handler-->>Student: 409 ENROLLMENT_ALREADY_EXISTS
        else Chưa ghi danh
            Svc->>Repo: save(enrollment)
            Repo-->>Svc: Enrollment đã lưu
            Svc-->>Ctrl: EnrollmentResponse
            Ctrl-->>Student: 201 ApiResponse(EnrollmentResponse)
        end
    end
```

Hai request ghi danh cùng lúc được chặn bởi unique `(student_id, course_id)`. Lỗi trùng khóa từ DB cũng trả
`ENROLLMENT_ALREADY_EXISTS`.

## 3. Biểu đồ Tuần tự (Sequence Diagram) - Nộp bài tập (UC016)

```mermaid
sequenceDiagram
    actor Student
    participant Ctrl as LearningController
    participant Svc as LearningServiceImpl
    participant Lecture as LectureService
    participant Access as LearningAccessValidator
    participant Repo as SubmissionRepository
    participant Handler as GlobalExceptionHandler

    Student->>Ctrl: PUT /exercises/{exerciseId}/submission/status (SUBMITTED)
    Ctrl->>Svc: submit(studentId, exerciseId)
    Svc->>Lecture: getExerciseAnswerKey(exerciseId)
    Svc->>Access: requireLearningAccess(studentId, courseId)

    alt Không đạt điều kiện học tập
        Access->>Handler: BusinessException(NOT_FOUND / ENROLLMENT_REQUIRED / COURSE_NOT_STARTED)
        Handler-->>Student: 404 hoặc 403
    else Đạt
        Svc->>Repo: findByStudentIdAndExerciseId(studentId, exerciseId)
        alt Đã SUBMITTED
            Svc->>Handler: BusinessException(SUBMISSION_ALREADY_SUBMITTED)
            Handler-->>Student: 409 SUBMISSION_ALREADY_SUBMITTED
        else Chưa có bài làm hoặc còn câu chưa trả lời
            Svc->>Handler: BusinessException(SUBMISSION_INCOMPLETE)
            Handler-->>Student: 400 SUBMISSION_INCOMPLETE
        else Đủ câu
            Svc->>Svc: So với đáp án đúng, tính score
            Svc->>Repo: Lưu isCorrect từng câu, status = SUBMITTED, submittedAt
            Svc-->>Ctrl: SubmissionResponse kèm đáp án đúng
            Ctrl-->>Student: 200 ApiResponse(SubmissionResponse)
        end
    end
```
