# Thiết kế API & Biểu đồ tuần tự: Quản lý Đào tạo

## 1. Mô tả API Endpoints

- `GET /api/v1/categories`: Lấy danh sách thể loại.
- `POST /api/v1/categories`: (GV) Tạo thể loại mới.
- `GET /api/v1/courses`: Lấy danh sách khóa học (dành cho Khách/Học viên tìm kiếm).
- `POST /api/v1/courses`: (GV) Tạo khóa học mới.
- `PUT /api/v1/courses/{id}`: (GV) Sửa khóa học.
- `POST /api/v1/courses/{courseId}/lectures`: (GV) Thêm bài giảng.
- `POST /api/v1/lectures/{lectureId}/exercises`: (GV) Thêm bài tập trắc nghiệm.

## 2. Biểu đồ Tuần tự (Sequence Diagram) - Tạo Khóa học

```mermaid
sequenceDiagram
    actor Teacher
    participant Ctrl as CourseController
    participant Svc as CourseService
    participant Repo as CourseRepository
    participant DB as Database
    participant Handler as GlobalExceptionHandler

    Teacher->>Ctrl: POST /api/v1/courses (CourseCreateDTO)
    Ctrl->>Ctrl: Validate DTO
    Ctrl->>Svc: createCourse(dto, teacherId)
    
    Svc->>Svc: Kiểm tra Category tồn tại không
    
    alt Category không tồn tại
        Svc->>Handler: ResourceNotFoundException
        Handler-->>Teacher: 404 Not Found
    else Hợp lệ
        Svc->>Svc: Gán teacherId vào CourseEntity
        Svc->>Repo: save(courseEntity)
        Repo->>DB: INSERT INTO courses
        DB-->>Repo: success
        Repo-->>Svc: savedEntity
        Svc-->>Ctrl: CourseResponseDTO
        Ctrl-->>Teacher: 201 Created (CourseResponseDTO)
    end
```
