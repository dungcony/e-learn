# Kịch bản Use Case & Test Cases: Quản lý Thông tin (Tin tức & FAQ)

## 1. Kịch bản Use Case

### 1.1 Quản lý Tin tức (UC012)
- **Kịch bản chính**: Quản trị viên (QTV) đăng tin tức mới, xem, sửa, hoặc xóa tin tức. Khách và người dùng có thể xem tin tức.
- **Kịch bản ngoại lệ**: (404) Sửa/Xóa tin tức không tồn tại; (400) Thiếu tiêu đề hoặc nội dung khi thêm mới.

### 1.2 Quản lý FAQ (UC013)
- **Kịch bản chính**: QTV quản lý danh sách các câu hỏi thường gặp (Thêm, sửa, xóa). Người dùng xem danh sách FAQ.
- **Kịch bản ngoại lệ**: (404) Tương tự tin tức.

## 2. Kịch bản Kiểm thử (Test Cases)

| Mã TC | Loại | Kịch bản | Input | Kết quả kỳ vọng |
|---|---|---|---|---|
| TC_INFO_01 | Luồng chuẩn | QTV tạo Tin tức mới | Tiêu đề, nội dung hợp lệ | 201 Created |
| TC_INFO_02 | Ngoại lệ | Tạo tin tức thiếu nội dung | Content rỗng | 400 Bad Request |
| TC_INFO_03 | Luồng chuẩn | QTV sửa FAQ | ID và Dữ liệu hợp lệ | 200 OK |
| TC_INFO_04 | Ngoại lệ | Sửa FAQ không tồn tại | ID sai | 404 Not Found |
