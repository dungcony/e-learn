# Kịch bản Use Case & Test Cases: Quản lý Người dùng

## 1. Kịch bản Use Case

### 1.1 Quản lý Giảng viên (UC008)
- **Kịch bản chính**: Quản trị viên (QTV) xem danh sách, thêm, sửa thông tin, khóa/mở khóa tài khoản giảng viên.
- **Kịch bản ngoại lệ**: 
  - (409) Thêm mới với email bị trùng.
  - (404) Sửa hoặc Khóa giảng viên không tồn tại trong hệ thống.
  - (400) Thiếu thông tin bắt buộc khi thêm mới.

### 1.2 Quản lý Học viên (UC010)
- **Kịch bản chính**: QTV xem danh sách học viên, tìm kiếm học viên, khóa/mở khóa tài khoản học viên (chặn không cho học viên đăng nhập).
- **Kịch bản ngoại lệ**: Không tìm thấy học viên -> Trả về danh sách rỗng (200 OK).

## 2. Kịch bản Kiểm thử (Test Cases)

| Mã TC | Loại | Kịch bản | Input | Kết quả kỳ vọng |
|---|---|---|---|---|
| TC_USER_01 | Luồng chuẩn | QTV tạo Giảng viên mới | Dữ liệu GV hợp lệ | 201 Created |
| TC_USER_02 | Ngoại lệ | QTV tạo GV trùng email | Email đã tồn tại | 409 Conflict |
| TC_USER_03 | Luồng chuẩn | QTV khóa tài khoản HV | ID Học viên hợp lệ | 200 OK, Status -> LOCKED |
| TC_USER_04 | Ngoại lệ | QTV khóa HV không tồn tại| ID sai | 404 Not Found |
