# Kịch bản Use Case & Test Cases: Xác thực & Tài khoản

Quy ước chung, mã lỗi dùng chung và cách hiểu SRS: xem [`../README.md`](../README.md).

## 1. Kịch bản Use Case

### 1.1 Đăng nhập (UC001)

- **Tác nhân**: Khách đã có tài khoản.
- **Kịch bản chính (200)**: Khách gửi `email`, `password`. Hệ thống kiểm tra các trường bắt buộc, tìm tài khoản chưa xóa theo
  email, so khớp mật khẩu, kiểm tra trạng thái, rồi trả access token kèm `role` để client hiển thị chức năng tương ứng.
- **Kịch bản ngoại lệ**:
  - 400 `VALIDATION_ERROR`: thiếu email hoặc mật khẩu, email sai định dạng, mật khẩu dưới 6 ký tự (Bảng 2-2).
  - 401 `AUTH_CREDENTIALS_INVALID`: không có tài khoản với email này (kể cả tài khoản đã xóa) hoặc sai mật khẩu. Dùng chung
    một mã cho hai trường hợp để không lộ email nào đã đăng ký.
  - 403 `AUTH_ACCOUNT_BLOCKED`: tài khoản đang `LOCKED`.

### 1.2 Thay đổi mật khẩu (UC002)

- **Tác nhân**: Học viên, Giảng viên, Quản trị viên đã đăng nhập.
- **Học viên** — `PUT /users/me/password`:
  - Kịch bản chính (200): gửi `old_password`, `new_password`, `confirm_password`. Hệ thống kiểm tra mật khẩu cũ đúng và hai
    mật khẩu mới trùng nhau, rồi lưu mật khẩu mới.
  - Ngoại lệ: 400 `VALIDATION_ERROR` (thiếu trường, mật khẩu mới dưới 6 ký tự); 400 `AUTH_OLD_PASSWORD_INCORRECT`; 400
    `AUTH_PASSWORD_CONFIRM_MISMATCH`.
- **Giảng viên, Quản trị viên** — gộp vào UC005 theo ghi chú UC002 của SRS (cách hiểu A5): gửi thêm `password`,
  `confirm_password` trong `PUT /users/me`, không cần mật khẩu cũ.
  - Ngoại lệ: 400 `VALIDATION_ERROR` (mật khẩu dưới 6 ký tự); 400 `AUTH_PASSWORD_CONFIRM_MISMATCH`.

### 1.3 Thiết lập lại mật khẩu (UC003)

- **Tác nhân**: Học viên, Giảng viên, Quản trị viên quên mật khẩu (chưa đăng nhập).
- **Yêu cầu link** — `POST /auth/forgot-password`:
  - Kịch bản chính (200): gửi `email`. Hệ thống kiểm tra định dạng và tài khoản tồn tại, tạo token ngẫu nhiên (chỉ lưu bản
    băm) có hạn 60 phút, rồi gửi link chứa token tới email đó.
  - Ngoại lệ: 400 `VALIDATION_ERROR` (email trống hoặc sai định dạng); 404 `NOT_FOUND` (không có tài khoản với email này,
    cách hiểu A6).
- **Đặt mật khẩu mới** — `POST /auth/reset-password`:
  - Kịch bản chính (200): gửi `token`, `new_password`, `confirm_password`. Hệ thống kiểm tra token còn hạn và chưa dùng, lưu
    mật khẩu mới, đánh dấu token đã dùng.
  - Ngoại lệ: 400 `AUTH_CODE_INVALID` (token sai, quá 60 phút hoặc đã dùng); 400 `AUTH_PASSWORD_CONFIRM_MISMATCH`; 400
    `VALIDATION_ERROR` (mật khẩu dưới 6 ký tự).

### 1.4 Đăng ký (UC004)

- **Tác nhân**: Khách.
- **Kịch bản chính (201)**: gửi `email`, `password`, `confirm_password` (Bảng 2-6). Hệ thống kiểm tra các trường bắt buộc,
  định dạng email, hai mật khẩu trùng nhau và đủ 6 ký tự, rồi tạo tài khoản `STUDENT` trạng thái `ACTIVE`. Không tự đăng
  nhập; người dùng đăng nhập bằng UC001.
- **Kịch bản ngoại lệ**:
  - 400 `VALIDATION_ERROR`: thiếu trường, email sai định dạng, mật khẩu dưới 6 ký tự.
  - 400 `AUTH_PASSWORD_CONFIRM_MISMATCH`: mật khẩu xác nhận không trùng.
  - 409 `AUTH_EMAIL_ALREADY_EXISTS`: email đã có tài khoản chưa xóa.

### 1.5 Cập nhật thông tin cá nhân (UC005)

- **Tác nhân**: Học viên, Giảng viên, Quản trị viên đã đăng nhập.
- **Xem** — `GET /users/me` (200): trả thông tin hiện tại để hiển thị form.
- **Cập nhật** — `PUT /users/me` (200): gửi đủ form theo Bảng 2-8: `full_name` (≤ 255 ký tự), `email` (bắt buộc),
  `date_of_birth`, `phone` (chỉ chữ số), `gender` (`MALE`, `FEMALE`, `OTHER`). Giảng viên, Quản trị viên được gửi thêm
  `password`, `confirm_password` (UC002).
- **Ảnh đại diện** — `PUT /users/me/avatar` (200): upload file png, gif, jpg hoặc jpeg.
- **Kịch bản ngoại lệ**:
  - 400 `VALIDATION_ERROR`: sai định dạng (luồng 5a); học viên gửi kèm `password`.
  - 400 `AUTH_PASSWORD_CONFIRM_MISMATCH`: giảng viên, QTV gửi mật khẩu xác nhận không trùng.
  - 400 `FILE_TYPE_NOT_SUPPORTED`: ảnh sai định dạng.
  - 409 `AUTH_EMAIL_ALREADY_EXISTS`: đổi sang email của tài khoản khác.
  - 401 `UNAUTHENTICATED`, `TOKEN_EXPIRED`: thiếu token hoặc token hết hạn.

## 2. Kịch bản Kiểm thử (Test Cases)

| Mã TC | Loại | UC | Kịch bản | Input | Kết quả kỳ vọng |
|---|---|---|---|---|---|
| TC_AUTH_01 | Luồng chuẩn | UC001 | Đăng nhập đúng | Email, mật khẩu đúng | 200, có `access_token` và `role` |
| TC_AUTH_02 | Ngoại lệ | UC001 | Sai mật khẩu | Mật khẩu sai | 401 `AUTH_CREDENTIALS_INVALID` |
| TC_AUTH_03 | Ngoại lệ | UC001 | Email chưa đăng ký | Email lạ | 401 `AUTH_CREDENTIALS_INVALID` |
| TC_AUTH_04 | Ngoại lệ | UC001 | Tài khoản bị khóa | Tài khoản `LOCKED` | 403 `AUTH_ACCOUNT_BLOCKED` |
| TC_AUTH_05 | Ngoại lệ | UC001 | Thiếu mật khẩu | `password` rỗng | 400 `VALIDATION_ERROR`, `fields` có `password` |
| TC_AUTH_06 | Luồng chuẩn | UC004 | Đăng ký | Email mới, mật khẩu 6 ký tự, xác nhận khớp | 201, `role` = `STUDENT` |
| TC_AUTH_07 | Ngoại lệ | UC004 | Email trùng | Email đã có tài khoản | 409 `AUTH_EMAIL_ALREADY_EXISTS` |
| TC_AUTH_08 | Ngoại lệ | UC004 | Xác nhận không khớp | `confirm_password` khác | 400 `AUTH_PASSWORD_CONFIRM_MISMATCH` |
| TC_AUTH_09 | Ngoại lệ | UC004 | Mật khẩu ngắn | Mật khẩu 5 ký tự | 400 `VALIDATION_ERROR` |
| TC_AUTH_10 | Luồng chuẩn | UC003 | Yêu cầu link | Email đã đăng ký | 200, gửi một email chứa link |
| TC_AUTH_11 | Ngoại lệ | UC003 | Email không tồn tại | Email lạ | 404 `NOT_FOUND`, không gửi email |
| TC_AUTH_12 | Luồng chuẩn | UC003 | Đặt lại bằng token còn hạn | Token vừa nhận, mật khẩu mới khớp | 200, đăng nhập được bằng mật khẩu mới |
| TC_AUTH_13 | Ngoại lệ | UC003 | Token quá hạn | Token tạo cách đây 61 phút | 400 `AUTH_CODE_INVALID` |
| TC_AUTH_14 | Ngoại lệ | UC003 | Dùng lại token | Token đã dùng một lần | 400 `AUTH_CODE_INVALID` |
| TC_AUTH_15 | Luồng chuẩn | UC002 | Học viên đổi mật khẩu | Mật khẩu cũ đúng, mới khớp | 200, đăng nhập được bằng mật khẩu mới |
| TC_AUTH_16 | Ngoại lệ | UC002 | Học viên sai mật khẩu cũ | Mật khẩu cũ sai | 400 `AUTH_OLD_PASSWORD_INCORRECT` |
| TC_AUTH_17 | Luồng chuẩn | UC002 | Giảng viên đổi mật khẩu trong form thông tin | `password`, `confirm_password` khớp | 200, đăng nhập được bằng mật khẩu mới |
| TC_AUTH_18 | Ngoại lệ | UC002 | Học viên gửi mật khẩu trong `PUT /users/me` | Có `password` | 400 `VALIDATION_ERROR` |
| TC_AUTH_19 | Luồng chuẩn | UC005 | Cập nhật thông tin | Dữ liệu hợp lệ | 200, trả thông tin mới |
| TC_AUTH_20 | Ngoại lệ | UC005 | Số điện thoại có chữ | `phone` = `09abc` | 400 `VALIDATION_ERROR` |
| TC_AUTH_21 | Ngoại lệ | UC005 | Đổi sang email người khác | Email đã có tài khoản | 409 `AUTH_EMAIL_ALREADY_EXISTS` |
| TC_AUTH_22 | Ngoại lệ | UC005 | Ảnh sai định dạng | File `.bmp` | 400 `FILE_TYPE_NOT_SUPPORTED` |
| TC_AUTH_23 | Ngoại lệ | UC005 | Không có token | Thiếu header `Authorization` | 401 `UNAUTHENTICATED` |
