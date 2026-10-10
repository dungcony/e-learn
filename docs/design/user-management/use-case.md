# Kịch bản Use Case & Test Cases: Quản lý Người dùng

Tác nhân của cả module là Quản lý (`MANAGER`); vai trò khác gọi vào → 403 `FORBIDDEN`. "Nhân viên" là tài khoản có `role` thuộc
`WAITER`, `CASHIER`, `CHEF`, `MANAGER`; "khách hàng" là `CUSTOMER`. Quy ước chung và cách hiểu SRS: xem
[`../README.md`](../README.md).

## 1. Kịch bản Use Case

### 1.1 Tìm kiếm nhân viên, khách hàng (UC006)

- **Kịch bản chính (200)**: Quản lý nhập một hoặc nhiều tiêu chí của Bảng 2-12: `name` (chứa chuỗi, không phân biệt hoa thường),
  `email` (chứa chuỗi), `phone` (chứa chuỗi), `role` (chỉ với nhân viên), `status` (`ACTIVE` / `LOCKED`). Hệ thống trả danh sách
  phân trang những tài khoản thỏa mãn mọi tiêu chí đã nhập. Không nhập tiêu chí nào thì trả toàn bộ danh sách.
- Dùng cho cả `GET /admin/staff` (UC008) và `GET /admin/customers` (UC009); chỉ trả tài khoản đúng nhóm và chưa xóa. Danh sách
  khách hàng thêm tiêu chí `email_verified` để lọc tài khoản chưa xác thực (A21).
- **Kịch bản ngoại lệ**:
  - Không ai thỏa mãn → 200 với `items: []` (luồng 6a, client hiển thị thông báo).
  - `role`, `status` sai giá trị, hoặc `role=CUSTOMER` ở `/admin/staff` → 400 `VALIDATION_ERROR`.

### 1.2 Quản lý nhân viên (UC008)

- **Xem (R)**: danh sách `GET /admin/staff` (kèm tìm kiếm UC006) và chi tiết `GET /admin/staff/{id}`.
  - Ngoại lệ: id không tồn tại, đã xóa hoặc là khách hàng → 404 `NOT_FOUND`.
- **Thêm (C)** — `POST /admin/staff` (201): nhập các trường của Bảng 2-19. `full_name`, `email`, `role`, `password`, `status` là bắt buộc;
  `date_of_birth`, `phone`, `gender` tùy chọn. Tài khoản tạo ra có `email_verified = true`, `must_change_password = true` (A19).
  Ảnh đại diện tải lên sau khi tạo.
  - Ngoại lệ: 400 `VALIDATION_ERROR` (luồng 4a, gồm `role = CUSTOMER`); 409 `AUTH_EMAIL_ALREADY_EXISTS` (luồng 4b).
- **Sửa (U)** — `PUT /admin/staff/{id}` (200): gửi `full_name`, `email`, `role`, `date_of_birth`, `phone`, `gender`. Không có mật khẩu và
  không có `status` (A20).
  - Ảnh đại diện: `PUT /admin/staff/{id}/avatar`.
  - Ngoại lệ: 400 `VALIDATION_ERROR`; 400 `FILE_TYPE_NOT_SUPPORTED`; 404 `NOT_FOUND`; 409 `AUTH_EMAIL_ALREADY_EXISTS`;
    403 `USER_CANNOT_MODIFY_SELF` nếu Quản lý tự đổi `role` của chính mình (luồng 4b). Tự sửa các trường khác của mình vẫn được.
- **Xóa (D)** — `DELETE /admin/staff/{id}` (200): client hỏi xác nhận trước khi gọi (bước 2–3). Hệ thống chạy các `UserDeletionGuard`
  rồi xóa mềm; email được giải phóng.
  - Ngoại lệ: 409 `USER_HAS_ACTIVITY` nếu nhân viên đã có đơn hàng hoặc hóa đơn (chỉ khóa được, luồng 4a); 403
    `USER_CANNOT_MODIFY_SELF` nếu xóa chính mình; 404 `NOT_FOUND`.
- **Khóa / Mở khóa** — `PUT /admin/staff/{id}/status` (200) với `status`: `LOCKED` thì nhân viên không đăng nhập được và token đang
  dùng bị chặn ngay (A18); `ACTIVE` để mở lại.
  - Ngoại lệ: 403 `USER_CANNOT_MODIFY_SELF` nếu khóa chính mình (luồng 4a); 404 `NOT_FOUND`; 400 `VALIDATION_ERROR`.

### 1.3 Quản lý khách hàng (UC009)

- **Xem (R)**: danh sách `GET /admin/customers` (kèm tìm kiếm UC006) và chi tiết `GET /admin/customers/{id}`.
- **Khóa / Mở khóa** — `PUT /admin/customers/{id}/status` (200): `LOCKED` thì khách hàng không đăng nhập được, token đang dùng bị
  chặn ngay; `ACTIVE` để mở lại.
- **Xóa (D)** — `DELETE /admin/customers/{id}` (200): hệ thống chạy `UserDeletionGuard`, xóa mềm. Đặt bàn và hóa đơn đã có của khách
  hàng được giữ nguyên (chỉ còn `customer_id` trỏ tới tài khoản đã xóa).
  - Ngoại lệ: 409 `USER_HAS_ACTIVITY` nếu khách hàng còn đặt bàn `PENDING` hoặc `CONFIRMED` (luồng 4a).
- Không có thêm hoặc sửa khách hàng: khách tự đăng ký và tự cập nhật (mô tả UC009).
- **Kịch bản ngoại lệ chung**: 404 `NOT_FOUND` (id không tồn tại, đã xóa hoặc là nhân viên); 400 `VALIDATION_ERROR` (`status` trống hoặc
  sai giá trị).

### 1.4 Khóa và xóa có hiệu lực ngay

Khi một tài khoản bị chuyển sang `LOCKED` hoặc bị xóa, hệ thống ghi `BlacklistedUserModel` (Redis, TTL bằng
`jwt.access-token-expiry-seconds`). `JwtAuthFilter` đọc bản ghi này nên token đang dùng của người đó bị chặn ngay với 403
`AUTH_ACCOUNT_BLOCKED`. Mở khóa thì xóa bản ghi đó. Nếu Redis không ghi được thì thao tác khóa/xóa thất bại (không âm thầm bỏ qua).

## 2. Kịch bản Kiểm thử (Test Cases)

| Mã TC | Loại | UC | Kịch bản | Input | Kết quả kỳ vọng |
|---|---|---|---|---|---|
| TC_USER_01 | Luồng chuẩn | UC006 | Tìm nhân viên theo tên | `name=trần` | 200, chỉ nhân viên có tên chứa "Trần" |
| TC_USER_02 | Luồng chuẩn | UC006 | Tìm nhân viên theo vai trò và trạng thái | `role=CASHIER&status=LOCKED` | 200, chỉ thu ngân đang bị khóa |
| TC_USER_03 | Luồng chuẩn | UC006 | Tìm khách hàng không có kết quả | `email=khongco` | 200, `items: []` |
| TC_USER_04 | Ngoại lệ | UC006 | Vai trò sai | `role=CUSTOMER` ở `/admin/staff` | 400 `VALIDATION_ERROR` |
| TC_USER_05 | Luồng chuẩn | UC006 | Tìm có ký tự đặc biệt | `name=50%` | 200, `%` được coi là ký tự thường |
| TC_USER_06 | Luồng chuẩn | UC008 | Tạo nhân viên | Đủ trường bắt buộc, `role=CASHIER` | 201, `must_change_password` = `true`, `email_verified` = `true` |
| TC_USER_07 | Ngoại lệ | UC008 | Tạo nhân viên trùng email | Email đã có tài khoản | 409 `AUTH_EMAIL_ALREADY_EXISTS` |
| TC_USER_08 | Ngoại lệ | UC008 | Tạo nhân viên thiếu mật khẩu | Không có `password` | 400 `VALIDATION_ERROR` |
| TC_USER_09 | Ngoại lệ | UC008 | Tạo nhân viên với vai trò khách hàng | `role=CUSTOMER` | 400 `VALIDATION_ERROR` |
| TC_USER_10 | Luồng chuẩn | UC008 | Nhân viên mới đăng nhập được | Đăng nhập bằng mật khẩu Quản lý đặt | 200, `must_change_password` = `true` |
| TC_USER_11 | Luồng chuẩn | UC008 | Sửa nhân viên | Đổi tên, điện thoại | 200, dữ liệu mới; mật khẩu không đổi |
| TC_USER_12 | Luồng chuẩn | UC008 | Quản lý đổi vai trò nhân viên khác | `WAITER` → `CASHIER` | 200 |
| TC_USER_13 | Ngoại lệ | UC008 | Quản lý tự đổi vai trò mình | `role` khác vai trò hiện tại, `{id}` là mình | 403 `USER_CANNOT_MODIFY_SELF` |
| TC_USER_14 | Ngoại lệ | UC008 | Sửa trùng email | Email của tài khoản khác | 409 `AUTH_EMAIL_ALREADY_EXISTS` |
| TC_USER_15 | Ngoại lệ | UC008 | Xem chi tiết bằng id khách hàng | id của một khách hàng | 404 `NOT_FOUND` |
| TC_USER_16 | Luồng chuẩn | UC008 | Khóa nhân viên | `status=LOCKED` | 200; token cũ của nhân viên nhận 403 `AUTH_ACCOUNT_BLOCKED`; đăng nhập mới nhận 403 `AUTH_ACCOUNT_BLOCKED` |
| TC_USER_17 | Luồng chuẩn | UC008 | Mở khóa nhân viên | `status=ACTIVE` | 200; đăng nhập được |
| TC_USER_18 | Ngoại lệ | UC008 | Quản lý tự khóa mình | `{id}` là mình, `LOCKED` | 403 `USER_CANNOT_MODIFY_SELF` |
| TC_USER_19 | Luồng chuẩn | UC008 | Xóa nhân viên chưa phát sinh dữ liệu | id hợp lệ | 200; không còn trong danh sách; email dùng lại được |
| TC_USER_20 | Ngoại lệ | UC008 | Xóa nhân viên đã có đơn hàng | Nhân viên phục vụ đã mở đơn | 409 `USER_HAS_ACTIVITY` |
| TC_USER_21 | Ngoại lệ | UC008 | Xóa nhân viên đã lập hóa đơn | Thu ngân đã có hóa đơn | 409 `USER_HAS_ACTIVITY` |
| TC_USER_22 | Ngoại lệ | UC008 | Quản lý tự xóa mình | `{id}` là mình | 403 `USER_CANNOT_MODIFY_SELF` |
| TC_USER_23 | Ngoại lệ | UC008 | Ảnh đại diện sai định dạng | File `.bmp` | 400 `FILE_TYPE_NOT_SUPPORTED` |
| TC_USER_24 | Luồng chuẩn | UC009 | Khóa khách hàng | `status=LOCKED` | 200; khách đăng nhập nhận 403 `AUTH_ACCOUNT_BLOCKED` |
| TC_USER_25 | Luồng chuẩn | UC009 | Mở khóa khách hàng | `status=ACTIVE` | 200; khách đăng nhập được |
| TC_USER_26 | Luồng chuẩn | UC009 | Xóa khách hàng không có đặt bàn chờ | id hợp lệ | 200; đăng nhập nhận 401 `AUTH_CREDENTIALS_INVALID` |
| TC_USER_27 | Ngoại lệ | UC009 | Xóa khách hàng còn đặt bàn | Có đặt bàn `PENDING` hoặc `CONFIRMED` | 409 `USER_HAS_ACTIVITY` |
| TC_USER_28 | Luồng chuẩn | UC009 | Xóa khách hàng chỉ còn đặt bàn đã hoàn tất | Đặt bàn `CHECKED_IN`/`CANCELLED`/`NO_SHOW` | 200; đặt bàn và hóa đơn cũ vẫn còn |
| TC_USER_29 | Ngoại lệ | UC009 | Khóa khách hàng không tồn tại | id sai | 404 `NOT_FOUND` |
| TC_USER_30 | Ngoại lệ | Chung | Nhân viên khác gọi API quản lý | Token `WAITER` gọi `GET /admin/staff` | 403 `FORBIDDEN` |
| TC_USER_31 | Ngoại lệ | Chung | Khách hàng gọi API quản lý | Token `CUSTOMER` gọi `GET /admin/customers` | 403 `FORBIDDEN` |
