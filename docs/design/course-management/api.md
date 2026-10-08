# Thiết kế API & Biểu đồ tuần tự: Quản lý Đào tạo

Quy ước URL, response, phân trang và mã lỗi: xem [`../README.md`](../README.md).

## 1. Mô tả API Endpoints

### Thể loại khóa học (UC015)

| Method | URL | Vai trò | Request | Response (`data`) |
|---|---|---|---|---|
| GET | `/categories?name=` | `TEACHER` | Query + phân trang | 200 `{items: CategoryResponse[], meta}` |
| POST | `/categories` | `TEACHER` | `CategoryCreateRequest` | 201 `CategoryResponse` |
| PUT | `/categories/{id}` | `TEACHER` người tạo | `CategoryUpdateRequest` | 200 `CategoryResponse` |
| DELETE | `/categories/{id}` | `TEACHER` người tạo | — | 200 `null` |

### Khóa học (UC007, UC009)

| Method | URL | Vai trò | Request | Response (`data`) |
|---|---|---|---|---|
| GET | `/courses` | Khách, `STUDENT` | Query `CourseSearchRequest` + phân trang | 200 `{items: CourseSummaryResponse[], meta}`, chỉ `PUBLIC` |
| GET | `/courses/{id}` | Khách, `STUDENT`, `TEACHER` chủ | — | 200 `CourseDetailResponse` |
| GET | `/teachers/me/courses` | `TEACHER` | Query `CourseSearchRequest` (có `status`) + phân trang | 200 `{items: CourseSummaryResponse[], meta}` |
| POST | `/courses` | `TEACHER` | `CourseCreateRequest` | 201 `CourseDetailResponse` |
| PUT | `/courses/{id}` | `TEACHER` chủ | `CourseUpdateRequest` | 200 `CourseDetailResponse` |
| PUT | `/courses/{id}/image` | `TEACHER` chủ | multipart, field `file` | 200 `CourseDetailResponse` |
| DELETE | `/courses/{id}` | `TEACHER` chủ | — | 200 `null` |

`GET /courses` và `GET /courses/{id}` cho Khách gọi không cần token, phải thêm `permitAll` cho hai đường dẫn này trong
`SecurityConfig` khi code. Ví dụ tìm kiếm: `GET /courses?name=math&start_date=2026-01-01&page=1&page_size=20`.

### Bài giảng và bài tập (UC011)

| Method | URL | Vai trò | Request | Response (`data`) |
|---|---|---|---|---|
| GET | `/courses/{courseId}/lectures?name=` | `TEACHER` chủ | Query + phân trang | 200 `{items: LectureSummaryResponse[], meta}` |
| GET | `/courses/{courseId}/lectures/{id}` | `TEACHER` chủ | — | 200 `LectureDetailResponse` |
| POST | `/courses/{courseId}/lectures` | `TEACHER` chủ | `LectureCreateRequest` | 201 `LectureDetailResponse` |
| PUT | `/courses/{courseId}/lectures/{id}` | `TEACHER` chủ | `LectureUpdateRequest` | 200 `LectureDetailResponse` |
| DELETE | `/courses/{courseId}/lectures/{id}` | `TEACHER` chủ | — | 200 `null` |
| POST | `/lectures/{lectureId}/exercises` | `TEACHER` chủ | `ExerciseCreateRequest` | 201 `ExerciseDetailResponse` |
| GET | `/lectures/{lectureId}/exercises/{id}` | `TEACHER` chủ | — | 200 `ExerciseDetailResponse` |
| PUT | `/lectures/{lectureId}/exercises/{id}` | `TEACHER` chủ | `ExerciseUpdateRequest` | 200 `ExerciseDetailResponse` |
| DELETE | `/lectures/{lectureId}/exercises/{id}` | `TEACHER` chủ | — | 200 `null` |

Học viên xem bài giảng, làm bài tập qua các endpoint của module `learning`, không gọi các endpoint ở bảng này.

Ví dụ `POST /lectures/{lectureId}/exercises`:

```json
{
  "title": "Bài tập 1: Hệ tuyến tính",
  "description": "Ôn tập chương 1",
  "questions": [
    {
      "content": "Hệ PT tuyến tính là gì?",
      "answers": [
        { "content": "Hệ PT tuyến tính là A", "is_correct": true },
        { "content": "Hệ PT tuyến tính là B", "is_correct": false },
        { "content": "Hệ PT tuyến tính là C", "is_correct": false },
        { "content": "Hệ PT tuyến tính là D", "is_correct": false }
      ]
    }
  ]
}
```

## 2. Biểu đồ Tuần tự (Sequence Diagram) - Tạo Khóa học (UC009)

```mermaid
sequenceDiagram
    actor Teacher
    participant Ctrl as CourseController
    participant Svc as CourseServiceImpl
    participant Val as CourseValidator
    participant CatRepo as CategoryRepository
    participant Repo as CourseRepository
    participant Handler as GlobalExceptionHandler

    Teacher->>Ctrl: POST /courses (CourseCreateRequest)
    Ctrl->>Ctrl: @PreAuthorize TEACHER, @Valid kiểm tra request
    Ctrl->>Svc: createCourse(teacherId, request)
    Svc->>Val: validateDateRange(startDate, endDate)

    alt end_date không sau start_date
        Val->>Handler: BusinessException(COURSE_DATE_RANGE_INVALID)
        Handler-->>Teacher: 400 COURSE_DATE_RANGE_INVALID
    else Ngày hợp lệ
        Svc->>CatRepo: existsById(categoryId)
        alt Thể loại không tồn tại
            Svc->>Handler: BusinessException(NOT_FOUND)
            Handler-->>Teacher: 404 NOT_FOUND
        else Hợp lệ
            Svc->>Svc: Sinh code không trùng, gán teacherId
            Svc->>Repo: save(course)
            Repo-->>Svc: Course đã lưu
            Svc-->>Ctrl: CourseDetailResponse
            Ctrl-->>Teacher: 201 ApiResponse(CourseDetailResponse)
        end
    end
```

## 3. Biểu đồ Tuần tự (Sequence Diagram) - Thêm Bài tập (UC011)

```mermaid
sequenceDiagram
    actor Teacher
    participant Ctrl as ExerciseController
    participant Svc as LectureServiceImpl
    participant LecRepo as LectureRepository
    participant Val as ExerciseValidator
    participant Repo as ExerciseRepository
    participant Handler as GlobalExceptionHandler

    Teacher->>Ctrl: POST /lectures/{lectureId}/exercises (ExerciseCreateRequest)
    Ctrl->>Ctrl: @Valid kiểm tra request, mỗi câu đúng 4 đáp án
    Ctrl->>Svc: createExercise(teacherId, lectureId, request)
    Svc->>LecRepo: findByIdAndTeacherId(lectureId, teacherId)

    alt Bài giảng không tồn tại hoặc không phải của giảng viên
        Svc->>Handler: BusinessException(NOT_FOUND)
        Handler-->>Teacher: 404 NOT_FOUND
    else Hợp lệ
        Svc->>Val: validateSingleCorrectAnswer(questions)
        alt Có câu số đáp án đúng khác 1
            Val->>Handler: BusinessException(EXERCISE_ANSWER_INVALID)
            Handler-->>Teacher: 400 EXERCISE_ANSWER_INVALID
        else Hợp lệ
            Svc->>Repo: save(exercise, questions, answers)
            Repo-->>Svc: Exercise đã lưu
            Svc-->>Ctrl: ExerciseDetailResponse
            Ctrl-->>Teacher: 201 ApiResponse(ExerciseDetailResponse)
        end
    end
```

`LectureRepository.findByIdAndTeacherId` join sang `courses` để kiểm tra giảng viên là chủ khóa học chứa bài giảng.
