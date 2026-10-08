# Thiết kế API & Biểu đồ tuần tự: Quản lý Thông tin

Mọi endpoint chỉ dành cho `ADMIN` (QĐ2). Quy ước URL, response, phân trang và mã lỗi: xem [`../README.md`](../README.md).

## 1. Mô tả API Endpoints

### Tin tức (UC012)

| Method | URL | Request | Response (`data`) |
|---|---|---|---|
| GET | `/admin/news?title=` | Query + phân trang | 200 `{items: NewsSummaryResponse[], meta}` |
| GET | `/admin/news/{id}` | — | 200 `NewsDetailResponse` |
| POST | `/admin/news` | `NewsCreateRequest` | 201 `NewsDetailResponse` |
| PUT | `/admin/news/{id}` | `NewsUpdateRequest` | 200 `NewsDetailResponse` |
| DELETE | `/admin/news/{id}` | — | 200 `null` |

### Câu hỏi thường gặp (UC013)

| Method | URL | Request | Response (`data`) |
|---|---|---|---|
| GET | `/admin/faqs?question=` | Query + phân trang | 200 `{items: FaqResponse[], meta}` |
| GET | `/admin/faqs/{id}` | — | 200 `FaqResponse` |
| POST | `/admin/faqs` | `FaqCreateRequest` | 201 `FaqResponse` |
| PUT | `/admin/faqs/{id}` | `FaqUpdateRequest` | 200 `FaqResponse` |
| DELETE | `/admin/faqs/{id}` | — | 200 `null` |

## 2. Biểu đồ Tuần tự (Sequence Diagram) - Thêm Tin tức (UC012)

```mermaid
sequenceDiagram
    actor Admin
    participant Ctrl as NewsController
    participant Svc as NewsServiceImpl
    participant Repo as NewsRepository
    participant Handler as GlobalExceptionHandler

    Admin->>Ctrl: POST /admin/news (NewsCreateRequest)
    Ctrl->>Ctrl: @PreAuthorize ADMIN, @Valid kiểm tra request

    alt Request không hợp lệ (thiếu tiêu đề hoặc nội dung)
        Ctrl->>Handler: MethodArgumentNotValidException
        Handler-->>Admin: 400 VALIDATION_ERROR
    else Request hợp lệ
        Ctrl->>Svc: createNews(adminId, request)
        Svc->>Svc: Map sang News, gán authorId
        Svc->>Repo: save(news)
        Repo-->>Svc: News đã lưu
        Svc-->>Ctrl: NewsDetailResponse
        Ctrl-->>Admin: 201 ApiResponse(NewsDetailResponse)
    end
```

## 3. Biểu đồ Tuần tự (Sequence Diagram) - Sửa FAQ (UC013)

```mermaid
sequenceDiagram
    actor Admin
    participant Ctrl as FaqController
    participant Svc as FaqServiceImpl
    participant Repo as FaqRepository
    participant Handler as GlobalExceptionHandler

    Admin->>Ctrl: PUT /admin/faqs/{id} (FaqUpdateRequest)
    Ctrl->>Ctrl: @PreAuthorize ADMIN, @Valid kiểm tra request
    Ctrl->>Svc: updateFaq(id, request)
    Svc->>Repo: findById(id)

    alt Không tồn tại hoặc đã xóa
        Svc->>Handler: BusinessException(NOT_FOUND)
        Handler-->>Admin: 404 NOT_FOUND
    else Tìm thấy
        Svc->>Svc: Cập nhật question, answer
        Svc-->>Ctrl: FaqResponse
        Ctrl-->>Admin: 200 ApiResponse(FaqResponse)
    end
```
