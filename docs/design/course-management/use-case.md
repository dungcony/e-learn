# Kịch bản Use Case & Test Cases: Quản lý Đào tạo

## 1. Kịch bản Use Case

### 1.1 Quản lý Thể loại khóa học (UC015)
- **Kịch bản chính**: Giảng viên tạo, xem, sửa, xóa thể loại khóa học (Category).
- **Kịch bản ngoại lệ**: (409) Tên thể loại bị trùng; (400) Xóa thể loại đang có khóa học tham chiếu.

### 1.2 Quản lý Khóa học (UC009)
- **Kịch bản chính**: Giảng viên tạo mới khóa học, gán vào thể loại, sửa thông tin khóa học, hoặc xóa khóa học của chính mình.
- **Kịch bản ngoại lệ**: (404) Khóa học không tồn tại; (403) Cố gắng sửa khóa học của giảng viên khác.

### 1.3 Quản lý Bài giảng & Bài tập (UC011)
- **Kịch bản chính**: Giảng viên thêm Video/Tài liệu bài giảng (Lecture) vào khóa học. Tạo các câu hỏi trắc nghiệm (Exercises) vào bài giảng.
- **Kịch bản ngoại lệ**: (404) Khóa học cha không tồn tại.

## 2. Kịch bản Kiểm thử (Test Cases)

| Mã TC | Loại | Kịch bản | Input | Kết quả kỳ vọng |
|---|---|---|---|---|
| TC_COURSE_01 | Luồng chuẩn | Giảng viên tạo khóa học | Thông tin khóa học hợp lệ | 201 Created |
| TC_COURSE_02 | Ngoại lệ | Xóa thể loại đang dùng | ID thể loại đang có khóa học | 400 Bad Request |
| TC_COURSE_03 | Ngoại lệ | Sửa khóa học của người khác | ID khóa học lạ | 403 Forbidden |
| TC_COURSE_04 | Luồng chuẩn | Thêm bài giảng vào khóa học | Dữ liệu Lecture hợp lệ | 201 Created |
