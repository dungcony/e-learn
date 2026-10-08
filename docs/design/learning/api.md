# Thiết kế API & Biểu đồ tuần tự: Module Học tập

## 1. Mô tả API Endpoints

- `POST /api/v1/courses/{courseId}/enroll`: Học viên ghi danh vào khóa học.
- `GET /api/v1/users/me/enrollments`: Lấy danh sách khóa học đang theo học.
- `POST /api/v1/lectures/{lectureId}/submit`: Nộp bài tập trắc nghiệm của bài giảng.
- `GET /api/v1/lectures/{lectureId}/comments`: Xem bình luận của bài giảng.
- `POST /api/v1/lectures/{lectureId}/comments`: Đăng bình luận.

## 2. Biểu đồ Tuần tự (Sequence Diagram) - Đăng ký Khóa học

```mermaid
sequenceDiagram
    actor Student
    participant Ctrl as EnrollmentController
    participant Svc as EnrollmentService
    participant Repo as EnrollmentRepository
    participant DB as Database
    participant Handler as GlobalExceptionHandler

    Student->>Ctrl: POST /api/v1/courses/{id}/enroll
    Ctrl->>Svc: enrollCourse(studentId, courseId)
    
    Svc->>Repo: existsByStudentIdAndCourseId(studentId, courseId)
    Repo->>DB: SELECT EXISTS
    DB-->>Repo: boolean
    
    alt Đã đăng ký rồi
        Repo-->>Svc: true
        Svc->>Handler: DuplicateResourceException
        Handler-->>Student: 409 Conflict
    else Hợp lệ
        Repo-->>Svc: false
        Svc->>Svc: Tạo EnrollmentEntity
        Svc->>Repo: save(entity)
        Repo->>DB: INSERT INTO enrollments
        DB-->>Repo: success
        Repo-->>Svc: savedEntity
        Svc-->>Ctrl: EnrollmentResponseDTO
        Ctrl-->>Student: 201 Created (EnrollmentResponseDTO)
    end
```
