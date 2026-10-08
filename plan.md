# Kế hoạch thiết kế các module hệ thống E-Learning

Tài liệu SRS đã được đánh giá là **đạt chuẩn**. Để tiến hành thiết kế các module theo kiến trúc Spring Boot, tôi đề xuất chia hệ thống thành 5 module chính. 

Theo yêu cầu của bạn, **mỗi module sẽ được lưu trong một thư mục riêng** và bao gồm 3 file thiết kế:
1. `use-case.md`: Chứa Kịch bản chi tiết Use case (luồng chuẩn, ngoại lệ) và Test Cases.
2. `api.md`: Chứa thiết kế RESTful API Endpoint chi tiết và Biểu đồ tuần tự (Sequence Diagram).
3. `lopthucthe.md`: Chứa thiết kế Lớp thực thể (Entity), Lớp DTO và Biểu đồ lớp (Class Diagram).

## Danh sách các thư mục module sẽ tạo (nằm trong `docs/design/`):

### 1. `docs/design/auth-profile/` (Module Xác thực & Tài khoản)
- Gồm: Đăng nhập (UC001), Thay đổi mật khẩu (UC002), Lấy lại mật khẩu (UC003), Đăng ký (UC004), Cập nhật thông tin cá nhân (UC005).
- Các file: `use-case.md`, `api.md`, `lopthucthe.md`

### 2. `docs/design/user-management/` (Module Quản lý Người dùng)
- Gồm: Quản lý giảng viên (UC008), Quản lý học viên (UC010).
- Các file: `use-case.md`, `api.md`, `lopthucthe.md`

### 3. `docs/design/course-management/` (Module Quản lý Đào tạo)
- Gồm: Quản lý thể loại khóa học (UC015), Quản lý khóa học (UC009), Quản lý bài giảng & bài tập (UC011).
- Các file: `use-case.md`, `api.md`, `lopthucthe.md`

### 4. `docs/design/learning/` (Module Học tập của Học viên)
- Gồm: Đăng ký khóa học, học bài giảng, làm bài tập, thảo luận (UC016), Xem lịch sử (UC014).
- Các file: `use-case.md`, `api.md`, `lopthucthe.md`

### 5. `docs/design/info-management/` (Module Quản lý Thông tin)
- Gồm: Quản lý tin tức (UC012), Quản lý FAQ (UC013).
- Các file: `use-case.md`, `api.md`, `lopthucthe.md`

## Kế hoạch hành động:
Nếu bạn đồng ý với cấu trúc thư mục và file như trên, vui lòng gõ "Process" hoặc "Đồng ý" để tôi bắt đầu tạo các file `.md` thiết kế vào dự án.
