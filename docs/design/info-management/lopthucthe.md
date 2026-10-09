# Thiết kế Thực thể, DTO & Biểu đồ lớp: Quản lý Thông tin

Package `com.elearning.info`. Quy ước chung: xem [`../README.md`](../README.md). `authorId`, `createdBy` chỉ lưu id người
dùng, không có quan hệ ORM sang module `user` (rule 2.3).

## 1. Lớp Thực thể (Entity)

### `News` — bảng `news`

| Trường | Kiểu | Ràng buộc | Nguồn SRS |
|---|---|---|---|
| `id` | `UUID` | PK | |
| `title` | `String` | Bắt buộc, ≤ 255 | Bảng 2-27 |
| `content` | `String` (text) | Bắt buộc | Bảng 2-27 |
| `authorId` | `UUID` | QTV tạo tin | |
| `createdAt`, `updatedAt`, `deletedAt` | `Instant` | | |

### `Faq` — bảng `faqs`

| Trường | Kiểu | Ràng buộc | Nguồn SRS |
|---|---|---|---|
| `id` | `UUID` | PK | |
| `question` | `String` (text) | Bắt buộc | Bảng 2-29 |
| `answer` | `String` (text) | Bắt buộc | Bảng 2-29 |
| `createdBy` | `UUID` | QTV tạo FAQ | |
| `createdAt`, `updatedAt`, `deletedAt` | `Instant` | | |

## 2. Data Transfer Object (DTO)

### Request (`dto/request/`)

| DTO | Trường | Validate |
|---|---|---|
| `NewsCreateRequest`, `NewsUpdateRequest` | `title` | `@NotBlank @Size(max = 255)` |
| | `content` | `@NotBlank` |
| `FaqCreateRequest`, `FaqUpdateRequest` | `question` | `@NotBlank` |
| | `answer` | `@NotBlank` |

Tìm kiếm nhận trực tiếp query param `title` (tin tức) và `question` (FAQ).

### Response (`dto/response/`)

| DTO | Trường |
|---|---|
| `NewsSummaryResponse` | `id`, `title`, `createdAt` |
| `NewsDetailResponse` | `id`, `title`, `content`, `authorId`, `createdAt`, `updatedAt` |
| `FaqResponse` | `id`, `question`, `answer`, `createdAt`, `updatedAt` |

## 3. Biểu đồ Lớp (Class Diagram)

Mũi tên nét đứt từ service là phụ thuộc của lớp `*ServiceImpl` tương ứng.

```mermaid
classDiagram
    class NewsController {
        +searchNews(title, page, pageSize)
        +getNews(UUID id)
        +createNews(NewsCreateRequest)
        +updateNews(UUID id, NewsUpdateRequest)
        +deleteNews(UUID id)
    }

    class FaqController {
        +searchFaqs(question, page, pageSize)
        +getFaq(UUID id)
        +createFaq(FaqCreateRequest)
        +updateFaq(UUID id, FaqUpdateRequest)
        +deleteFaq(UUID id)
    }

    class NewsService {
        <<interface>>
        +searchNews(String title, PageRequestParams) Page~NewsSummaryResponse~
        +getNews(UUID id) NewsDetailResponse
        +createNews(UUID adminId, NewsCreateRequest) NewsDetailResponse
        +updateNews(UUID id, NewsUpdateRequest) NewsDetailResponse
        +deleteNews(UUID id) void
    }

    class FaqService {
        <<interface>>
        +searchFaqs(String question, PageRequestParams) Page~FaqResponse~
        +getFaq(UUID id) FaqResponse
        +createFaq(UUID adminId, FaqCreateRequest) FaqResponse
        +updateFaq(UUID id, FaqUpdateRequest) FaqResponse
        +deleteFaq(UUID id) void
    }

    class NewsRepository {
        <<interface>>
        +findAll(Specification~News~, Pageable) Page~News~
    }

    class FaqRepository {
        <<interface>>
        +findAll(Specification~Faq~, Pageable) Page~Faq~
    }

    class NewsMapper {
        <<interface>>
        +toSummaryResponse(News) NewsSummaryResponse
        +toDetailResponse(News) NewsDetailResponse
    }

    class FaqMapper {
        <<interface>>
        +toResponse(Faq) FaqResponse
    }

    NewsController --> NewsService
    FaqController --> FaqService
    NewsService ..> NewsRepository
    NewsService ..> NewsMapper
    FaqService ..> FaqRepository
    FaqService ..> FaqMapper
```

Controller trả `ApiResponse` của DTO tương ứng ở `api.md`; diagram lược bớt kiểu trả về của controller cho gọn.
Tìm kiếm dùng `SpecificationUtils.containsIgnoreCase("title" / "question", chuỗi)` của `common/util/`.
