# Kịch bản Use Case & Test Cases: Xác thực & Tài khoản

## 1. Kịch bản Use Case

### 1.1 Đăng nhập (UC001)
- **Kịch bản chính (200 OK)**: Người dùng (Khách/Học viên/Giảng viên/QTV) gửi thông tin `email` và `password`. Hệ thống xác thực thành công và trả về Access Token.
- **Kịch bản ngoại lệ**: 
  - (400 Bad Request): Dữ liệu gửi lên trống hoặc sai định dạng email.
  - (401 Unauthorized): Sai email hoặc mật khẩu, hoặc tài khoản đang bị khóa.

### 1.2 Đăng ký (UC004)
- **Kịch bản chính (201 Created)**: Khách gửi thông tin đăng ký hợp lệ. Hệ thống khởi tạo tài khoản Học viên mới.
- **Kịch bản ngoại lệ**:
  - (409 Conflict): Email đã tồn tại trong hệ thống.
  - (400 Bad Request): Mật khẩu và xác nhận mật khẩu không khớp, hoặc vi phạm chính sách độ phức tạp (ngắn hơn 6 ký tự).

### 1.3 Cập nhật thông tin cá nhân (UC005) & Thay đổi mật khẩu (UC002)
- **Kịch bản chính (200 OK)**: Người dùng gửi yêu cầu cập nhật Profile hoặc Mật khẩu cùng Token hợp lệ. Hệ thống lưu lại và thông báo thành công.
- **Kịch bản ngoại lệ**: 
  - (401/403): Token hết hạn hoặc không hợp lệ.
  - (400): Dữ liệu cập nhật không hợp lệ hoặc mật khẩu cũ không khớp.

## 2. Kịch bản Kiểm thử (Test Cases)

| Mã TC | Loại | Kịch bản | Input | Kết quả kỳ vọng |
|---|---|---|---|---|
| TC_AUTH_01 | Luồng chuẩn | Đăng nhập thành công | Email, Pass đúng | 200 OK, trả về JWT Token |
| TC_AUTH_02 | Ngoại lệ | Đăng nhập sai Pass | Pass sai | 401 Unauthorized |
| TC_AUTH_03 | Ngoại lệ | Đăng ký trùng Email | Email đã tồn tại | 409 Conflict |
| TC_AUTH_04 | Luồng chuẩn | Cập nhật Profile | Dữ liệu hợp lệ | 200 OK, Profile mới |
