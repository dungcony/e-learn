# Thiết kế API & Biểu đồ tuần tự: Quản lý Thông tin

## 1. Mô tả API Endpoints

- `GET /api/v1/news`: Lấy danh sách tin tức (Public).
- `POST /api/v1/admin/news`: QTV tạo tin tức mới.
- `PUT /api/v1/admin/news/{id}`: QTV sửa tin tức.
- `DELETE /api/v1/admin/news/{id}`: QTV xóa tin tức.
- `GET /api/v1/faqs`: Lấy danh sách FAQ (Public).
- `POST /api/v1/admin/faqs`: QTV tạo FAQ.

## 2. Biểu đồ Tuần tự (Sequence Diagram) - Thêm Tin tức

```mermaid
sequenceDiagram
    actor Admin
    participant Ctrl as NewsController
    participant Svc as NewsService
    participant Repo as NewsRepository
    participant DB as Database
    participant Handler as GlobalExceptionHandler

    Admin->>Ctrl: POST /api/v1/admin/news (NewsCreateDTO)
    Ctrl->>Ctrl: Validate DTO
    
    alt DTO lỗi (Thiếu title)
        Ctrl->>Handler: Validation Error
        Handler-->>Admin: 400 Bad Request
    else DTO hợp lệ
        Ctrl->>Svc: createNews(dto, adminId)
        Svc->>Svc: Map DTO to NewsEntity
        Svc->>Repo: save(entity)
        Repo->>DB: INSERT INTO news
        DB-->>Repo: success
        Repo-->>Svc: savedEntity
        Svc-->>Ctrl: NewsResponseDTO
        Ctrl-->>Admin: 201 Created (NewsResponseDTO)
    end
```
