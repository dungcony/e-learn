# Thiết kế API & Biểu đồ tuần tự: Quản lý Người dùng

## 1. Mô tả API Endpoints

- `GET /api/v1/admin/teachers`: Lấy danh sách giảng viên (có phân trang và tìm kiếm).
- `POST /api/v1/admin/teachers`: Thêm mới tài khoản giảng viên.
- `PUT /api/v1/admin/teachers/{id}`: Cập nhật thông tin giảng viên.
- `PUT /api/v1/admin/users/{id}/status`: Khóa / Mở khóa tài khoản (Giảng viên/Học viên).
- `GET /api/v1/admin/students`: Lấy danh sách học viên.

## 2. Biểu đồ Tuần tự (Sequence Diagram) - Thêm Giảng viên mới

```mermaid
sequenceDiagram
    actor Admin
    participant Ctrl as AdminUserController
    participant Svc as UserService
    participant Repo as UserRepository
    participant DB as Database
    participant Handler as GlobalExceptionHandler

    Admin->>Ctrl: POST /api/v1/admin/teachers (TeacherCreateDTO)
    Ctrl->>Ctrl: Validate DTO
    Ctrl->>Svc: createTeacher(dto)
    Svc->>Repo: existsByEmail(dto.email)
    Repo->>DB: SELECT EXISTS
    DB-->>Repo: boolean
    
    alt Email trùng
        Repo-->>Svc: true
        Svc->>Handler: DuplicateResourceException
        Handler-->>Admin: 409 Conflict
    else Email chưa tồn tại
        Repo-->>Svc: false
        Svc->>Svc: Chuyển DTO thành UserEntity (Role=TEACHER)
        Svc->>Repo: save(entity)
        Repo->>DB: INSERT INTO users
        DB-->>Repo: success
        Repo-->>Svc: savedEntity
        Svc-->>Ctrl: UserResponseDTO
        Ctrl-->>Admin: 201 Created (UserResponseDTO)
    end
```
