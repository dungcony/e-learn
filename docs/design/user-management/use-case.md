# Kịch bản Use Case & Test Cases: Quản lý Người dùng

Tác nhân của cả module là Quản trị viên (`ADMIN`); vai trò khác gọi vào → 403 `FORBIDDEN`. Quy ước chung và cách hiểu SRS:
xem [`../README.md`](../README.md).

## 1. Kịch bản Use Case

### 1.1 Tìm kiếm giảng viên, học viên (UC006)

- **Kịch bản chính (200)**: QTV nhập một hoặc nhiều tiêu chí của Bảng 2-10: `name` (chứa chuỗi, không phân biệt hoa
  thường), `email` (chứa chuỗi), `phone` (chứa chuỗi), `gender`. Hệ thống trả danh sách phân trang những tài khoản thỏa mãn
  mọi tiêu chí đã nhập. Không nhập tiêu chí nào thì trả toàn bộ danh sách.
- Dùng cho cả `GET /admin/teachers` (UC008) và `GET /admin/students` (UC010); chỉ trả tài khoản đúng role và chưa xóa.
- **Kịch bản ngoại lệ**:
  - Không ai thỏa mãn → 200 với `items: []` (luồng 6a, client hiển thị thông báo).
  - `gender` sai giá trị → 400 `VALIDATION_ERROR`.

### 1.2 Quản lý giảng viên (UC008)

- **Xem (R)**: danh sách `GET /admin/teachers` (kèm tìm kiếm UC006) và chi tiết `GET /admin/teachers/{id}`.
  - Ngoại lệ: id không tồn tại, đã xóa hoặc không phải giảng viên → 404 `NOT_FOUND`.
- **Thêm (C)** — `POST /admin/teachers` (201): nhập các trường của Bảng 2-17. `full_name`, `email`, `password` (≥ 6 ký
  tự), `status` là bắt buộc; `date_of_birth`, `phone`, `gender` tùy chọn. Role luôn là `TEACHER` (A3). Ảnh đại diện upload
  sau khi tạo.
  - Ngoại lệ: 400 `VALIDATION_ERROR` (luồng 4a); 409 `AUTH_EMAIL_ALREADY_EXISTS`.
- **Sửa (U)** — `PUT /admin/teachers/{id}` (200): gửi các trường như khi thêm; `password` tùy chọn, bỏ trống thì giữ mật khẩu
  cũ (A4). Đổi `status` là thao tác Khóa/Mở khóa giảng viên (A7).
  - Ảnh đại diện: `PUT /admin/teachers/{id}/avatar`.
  - Ngoại lệ: 400 `VALIDATION_ERROR`; 400 `FILE_TYPE_NOT_SUPPORTED`; 404 `NOT_FOUND`; 409 `AUTH_EMAIL_ALREADY_EXISTS`.
- **Xóa (D)** — `DELETE /admin/teachers/{id}` (200): client hỏi xác nhận trước khi gọi (bước 2–3 của SRS). Hệ thống xóa mềm;
  các khóa học của giảng viên giữ nguyên.
  - Ngoại lệ: 404 `NOT_FOUND`.

### 1.3 Quản lý học viên (UC010)

- **Xem (R)**: danh sách `GET /admin/students` (kèm tìm kiếm UC006) và chi tiết `GET /admin/students/{id}`.
- **Khóa/Mở khóa** — `PUT /admin/students/{id}/status` (200): `LOCKED` thì học viên không đăng nhập và không dùng được chức
  năng nào (hậu điều kiện UC010); `ACTIVE` để mở lại.
- **Xóa (D)** — `DELETE /admin/students/{id}` (200): xóa mềm; ghi danh, bài làm, bình luận của học viên giữ nguyên.
- Không có thêm hoặc sửa học viên (A8).
- **Kịch bản ngoại lệ**: 404 `NOT_FOUND` (id không tồn tại, đã xóa hoặc không phải học viên); 400 `VALIDATION_ERROR`
  (`status` trống hoặc sai giá trị).

### 1.4 Khóa và xóa có hiệu lực ngay

Khi một giảng viên hoặc học viên bị chuyển sang `LOCKED` hoặc bị xóa, hệ thống ghi `BlacklistedUserModel` (TTL bằng thời hạn
access token). `JwtAuthFilter` đọc bản ghi này nên token đang dùng của người đó bị chặn ngay với 403 `AUTH_ACCOUNT_BLOCKED`.
Mở khóa thì xóa bản ghi đó.

## 2. Kịch bản Kiểm thử (Test Cases)

| Mã TC | Loại | UC | Kịch bản | Input | Kết quả kỳ vọng |
|---|---|---|---|---|---|
| TC_USER_01 | Luồng chuẩn | UC006 | Tìm giảng viên theo tên | `name=nguyễn` | 200, chỉ giảng viên có tên chứa "Nguyễn" |
| TC_USER_02 | Luồng chuẩn | UC006 | Tìm học viên không có kết quả | `email=khongco` | 200, `items: []` |
| TC_USER_03 | Ngoại lệ | UC006 | Giới tính sai giá trị | `gender=X` | 400 `VALIDATION_ERROR` |
| TC_USER_04 | Luồng chuẩn | UC008 | Tạo giảng viên | Đủ trường bắt buộc | 201, `role` = `TEACHER` |
| TC_USER_05 | Ngoại lệ | UC008 | Tạo giảng viên trùng email | Email đã có tài khoản | 409 `AUTH_EMAIL_ALREADY_EXISTS` |
| TC_USER_06 | Ngoại lệ | UC008 | Tạo giảng viên thiếu mật khẩu | Không có `password` | 400 `VALIDATION_ERROR` |
| TC_USER_07 | Luồng chuẩn | UC008 | Sửa giảng viên không gửi mật khẩu | Bỏ trống `password` | 200, vẫn đăng nhập được bằng mật khẩu cũ |
| TC_USER_08 | Luồng chuẩn | UC008 | Khóa giảng viên | `status` = `LOCKED` | 200; token cũ của giảng viên nhận 403 `AUTH_ACCOUNT_BLOCKED` |
| TC_USER_09 | Ngoại lệ | UC008 | Xem chi tiết bằng id học viên | id của một học viên | 404 `NOT_FOUND` |
| TC_USER_10 | Luồng chuẩn | UC008 | Xóa giảng viên | id hợp lệ | 200; không còn trong danh sách; khóa học của giảng viên vẫn còn |
| TC_USER_11 | Ngoại lệ | UC008 | Ảnh đại diện sai định dạng | File `.bmp` | 400 `FILE_TYPE_NOT_SUPPORTED` |
| TC_USER_12 | Luồng chuẩn | UC010 | Khóa học viên | `status` = `LOCKED` | 200; học viên đăng nhập nhận 403 `AUTH_ACCOUNT_BLOCKED` |
| TC_USER_13 | Luồng chuẩn | UC010 | Mở khóa học viên | `status` = `ACTIVE` | 200; học viên đăng nhập được |
| TC_USER_14 | Ngoại lệ | UC010 | Khóa học viên không tồn tại | id sai | 404 `NOT_FOUND` |
| TC_USER_15 | Luồng chuẩn | UC010 | Xóa học viên | id hợp lệ | 200; học viên đăng nhập nhận 401 `AUTH_CREDENTIALS_INVALID` |
| TC_USER_16 | Ngoại lệ | Chung | Giảng viên gọi API quản lý | Token `TEACHER` gọi `GET /admin/students` | 403 `FORBIDDEN` |
