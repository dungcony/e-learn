# Kịch bản Use Case & Test Cases: Module Học tập (Học viên)

## 1. Kịch bản Use Case

### 1.1 Đăng ký khóa học & Xem lịch sử (UC016, UC014)
- **Kịch bản chính**: Học viên chọn khóa học và nhấn đăng ký (Enroll). Hệ thống lưu lại quá trình học tập. Học viên xem danh sách các khóa học đang tham gia.
- **Kịch bản ngoại lệ**: (409) Đã đăng ký khóa học này rồi; (404) Khóa học không tồn tại.

### 1.2 Học bài & Nộp bài tập (UC016)
- **Kịch bản chính**: Học viên xem bài giảng, trả lời câu hỏi trắc nghiệm, hệ thống chấm điểm và lưu trạng thái hoàn thành.
- **Kịch bản ngoại lệ**: (403) Chưa đăng ký khóa học nhưng cố tình truy cập vào bài tập.

### 1.3 Bình luận & Thảo luận (UC016)
- **Kịch bản chính**: Học viên đăng bình luận, thắc mắc bên dưới mỗi bài giảng. Xóa hoặc sửa bình luận của chính mình.
- **Kịch bản ngoại lệ**: (403) Sửa/Xóa bình luận của người khác.

## 2. Kịch bản Kiểm thử (Test Cases)

| Mã TC | Loại | Kịch bản | Input | Kết quả kỳ vọng |
|---|---|---|---|---|
| TC_LEARN_01 | Luồng chuẩn | Đăng ký khóa học mới | ID Khóa học hợp lệ | 201 Created (Ghi danh thành công) |
| TC_LEARN_02 | Ngoại lệ | Đăng ký trùng | Cùng ID khóa học đã ghi danh | 409 Conflict |
| TC_LEARN_03 | Luồng chuẩn | Nộp bài tập trắc nghiệm | List câu trả lời | 200 OK, trả về điểm số |
| TC_LEARN_04 | Ngoại lệ | Nộp bài chưa ghi danh | ID bài tập | 403 Forbidden |
