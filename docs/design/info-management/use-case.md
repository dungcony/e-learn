# Kịch bản Use Case & Test Cases: Quản lý Thông tin (Tin tức & FAQ)

Tác nhân của cả module là Quản trị viên (`ADMIN`) đã đăng nhập, đúng theo SRS (QĐ2); không có trang tin tức, FAQ công khai.
Vai trò khác gọi vào → 403 `FORBIDDEN`, không có token → 401 `UNAUTHENTICATED`. Tin tức và FAQ dùng chung cho mọi QTV, không
kiểm tra người tạo khi sửa, xóa. Quy ước chung và cách hiểu SRS: xem [`../README.md`](../README.md).

## 1. Kịch bản Use Case

### 1.1 Quản lý tin tức (UC012)

- **Tìm kiếm (S)** — `GET /admin/news?title=` (200): lọc theo tiêu đề chứa chuỗi (Bảng 2-11), mới nhất trước.
- **Xem (R)** — danh sách `GET /admin/news` và chi tiết `GET /admin/news/{id}` (200).
- **Thêm (C)** — `POST /admin/news` (201): nhập `title` (bắt buộc, ≤ 255 ký tự) và `content` (bắt buộc) theo Bảng 2-27. Người
  tạo là QTV hiện tại.
- **Sửa (U)** — `PUT /admin/news/{id}` (200): sửa `title`, `content`.
- **Xóa (D)** — `DELETE /admin/news/{id}` (200): client hỏi xác nhận trước khi gọi. Xóa mềm.
- **Kịch bản ngoại lệ**:
  - Không có tin tức nào → 200 với `items: []` (luồng 2a).
  - 400 `VALIDATION_ERROR`: thiếu tiêu đề hoặc nội dung, tiêu đề quá 255 ký tự (luồng 4a).
  - 404 `NOT_FOUND`: tin tức không tồn tại hoặc đã xóa.

### 1.2 Quản lý câu hỏi thường gặp (UC013)

SRS chép nhầm phần mô tả UC013 từ UC012 (README mục 4); các luồng dưới đây đã đọc lại theo nghĩa FAQ.

- **Tìm kiếm (S)** — `GET /admin/faqs?question=` (200): lọc theo nội dung câu hỏi chứa chuỗi (Bảng 2-12).
- **Xem (R)** — danh sách `GET /admin/faqs` và chi tiết `GET /admin/faqs/{id}` (200).
- **Thêm (C)** — `POST /admin/faqs` (201): nhập `question` và `answer`, đều bắt buộc (Bảng 2-29).
- **Sửa (U)** — `PUT /admin/faqs/{id}` (200).
- **Xóa (D)** — `DELETE /admin/faqs/{id}` (200), xóa mềm.
- **Kịch bản ngoại lệ**:
  - Không có FAQ nào → 200 với `items: []`.
  - 400 `VALIDATION_ERROR`: thiếu câu hỏi hoặc câu trả lời.
  - 404 `NOT_FOUND`: FAQ không tồn tại hoặc đã xóa.

## 2. Kịch bản Kiểm thử (Test Cases)

| Mã TC | Loại | UC | Kịch bản | Input | Kết quả kỳ vọng |
|---|---|---|---|---|---|
| TC_INFO_01 | Luồng chuẩn | UC012 | Tạo tin tức | Tiêu đề, nội dung hợp lệ | 201 |
| TC_INFO_02 | Ngoại lệ | UC012 | Tạo tin tức thiếu nội dung | `content` rỗng | 400 `VALIDATION_ERROR` |
| TC_INFO_03 | Luồng chuẩn | UC012 | Tìm tin tức theo tiêu đề | `title=linear` | 200, chỉ tin có tiêu đề chứa "linear" |
| TC_INFO_04 | Luồng chuẩn | UC012 | Sửa tin tức | id và dữ liệu hợp lệ | 200, trả nội dung mới |
| TC_INFO_05 | Ngoại lệ | UC012 | Xem chi tiết tin tức không tồn tại | id sai | 404 `NOT_FOUND` |
| TC_INFO_06 | Luồng chuẩn | UC012 | Xóa tin tức | id hợp lệ | 200; không còn trong danh sách; xem chi tiết 404 |
| TC_INFO_07 | Luồng chuẩn | UC013 | Tạo FAQ | Câu hỏi, câu trả lời hợp lệ | 201 |
| TC_INFO_08 | Ngoại lệ | UC013 | Tạo FAQ thiếu câu trả lời | `answer` rỗng | 400 `VALIDATION_ERROR` |
| TC_INFO_09 | Luồng chuẩn | UC013 | Sửa FAQ | id và dữ liệu hợp lệ | 200 |
| TC_INFO_10 | Ngoại lệ | UC013 | Sửa FAQ không tồn tại | id sai | 404 `NOT_FOUND` |
| TC_INFO_11 | Luồng chuẩn | UC013 | Tìm FAQ theo câu hỏi | `question=yêu cầu kỹ thuật` | 200, chỉ FAQ có câu hỏi chứa chuỗi đó |
| TC_INFO_12 | Ngoại lệ | Chung | Học viên xem tin tức | Token `STUDENT` gọi `GET /admin/news` | 403 `FORBIDDEN` |
| TC_INFO_13 | Ngoại lệ | Chung | Khách xem FAQ | Không có token gọi `GET /admin/faqs` | 401 `UNAUTHENTICATED` |
