# Kịch bản Use Case & Test Cases: Module Học tập

Quy ước chung, mã lỗi dùng chung và cách hiểu SRS: xem [`../README.md`](../README.md).

## 1. Kịch bản Use Case

### Điều kiện học tập

Áp dụng cho mọi thao tác ở mục 1.3, 1.4 và 1.5. Hệ thống kiểm tra theo thứ tự:

| Điều kiện | Không đạt |
|---|---|
| Khóa học tồn tại, chưa xóa, đang `PUBLIC`; bài giảng hoặc bài tập thuộc đúng khóa học | 404 `NOT_FOUND` (QĐ1: khóa `PRIVATE` bị ẩn cả với học viên đã ghi danh) |
| Học viên đã ghi danh khóa học | 403 `ENROLLMENT_REQUIRED` |
| Ngày hiện tại không trước `start_date` của khóa học | 403 `COURSE_NOT_STARTED` (UC016 luồng 4a) |

### 1.1 Ghi danh khóa học (UC016)

- **Tác nhân**: Học viên (`STUDENT`).
- **Kịch bản chính (201)**: sau khi tìm và xem khóa học (`GET /courses`, `GET /courses/{id}` của `course-management`), học
  viên gọi `POST /courses/{courseId}/enrollments`. Hệ thống kiểm tra khóa học tồn tại, `PUBLIC` và học viên chưa ghi danh,
  rồi thêm học viên vào khóa học. Client chuyển sang danh sách khóa học đã ghi danh (luồng 3).
- Được ghi danh trước ngày bắt đầu; bài giảng chỉ mở từ `start_date`.
- **Kịch bản ngoại lệ**:
  - 404 `NOT_FOUND`: khóa học không tồn tại, đã xóa hoặc `PRIVATE`.
  - 409 `ENROLLMENT_ALREADY_EXISTS`: đã ghi danh khóa này (A10).

### 1.2 Danh sách khóa học đã ghi danh (UC016, UC007 Bảng 2-14)

- **Tác nhân**: Học viên.
- **Kịch bản chính (200)**: `GET /my-courses?name=&code=` trả các khóa học đã ghi danh, chưa xóa và đang `PUBLIC`, lọc theo tên,
  mã khóa học. Mỗi khóa có `progress` = số bài giảng đã xác nhận hoàn thành / tổng số bài giảng hiện có × 100, làm tròn xuống;
  khóa chưa có bài giảng thì `progress` = 0.
- **Kịch bản ngoại lệ**: không có khóa nào → 200 với `items: []`.

### 1.3 Học bài giảng (UC016 bước 3–5, 10–11)

- **Tác nhân**: Học viên thỏa điều kiện học tập.
- **Danh sách bài giảng** — `GET /my-courses/{courseId}/lectures` (200): tên bài giảng và đã hoàn thành hay chưa.
- **Nội dung bài giảng** — `GET /my-courses/{courseId}/lectures/{id}` (200): mô tả, `content_url` (video hoặc tài liệu), các bài
  tập kèm câu hỏi và đáp án (không có cờ đáp án đúng), trạng thái bài làm của từng bài tập.
- **Xác nhận hoàn thành** — `PUT /lectures/{lectureId}/completion` (200): lưu trạng thái đã hoàn thành bài giảng. Gọi lại vẫn
  trả 200 và không tạo bản ghi trùng.
- **Kịch bản ngoại lệ**: theo bảng điều kiện học tập.

### 1.4 Làm và nộp bài tập (UC016 bước 6–9)

- **Tác nhân**: Học viên thỏa điều kiện học tập.
- **Lưu tạm** — `PUT /exercises/{exerciseId}/submission` (200): gửi `answers` gồm các cặp `question_id`, `answer_id`, chưa cần
  đủ câu (bước 7). Hệ thống tạo hoặc cập nhật bài làm `DRAFT`; câu đã có đáp án thì ghi đè.
- **Nộp bài** — `PUT /exercises/{exerciseId}/submission/status` với `status` = `SUBMITTED` (200): hệ thống kiểm tra đã trả lời
  hết các câu hiện có của bài tập (bước 8), chấm từng câu, lưu `score` (số câu đúng), `total_questions`, `submitted_at`, rồi
  trả kết quả kèm đáp án đúng (bước 9). Đáp án lưu tạm cho câu hỏi đã bị giảng viên thay thì bị bỏ qua khi nộp.
- **Xem kết quả** — `GET /exercises/{exerciseId}/submission` (200): bài `DRAFT` trả các đáp án đã chọn; bài `SUBMITTED` trả
  thêm điểm và đáp án đúng.
- Mỗi bài tập chỉ nộp một lần.
- **Kịch bản ngoại lệ**:
  - 400 `VALIDATION_ERROR`: `answers` rỗng; `question_id` không thuộc bài tập; `answer_id` không thuộc câu hỏi; `status` khác
    `SUBMITTED`.
  - 400 `SUBMISSION_INCOMPLETE`: nộp khi còn câu chưa trả lời.
  - 409 `SUBMISSION_ALREADY_SUBMITTED`: lưu tạm hoặc nộp lại sau khi đã nộp.
  - 404 `NOT_FOUND`: xem kết quả khi chưa có bài làm; các trường hợp của bảng điều kiện học tập.
  - 403 `ENROLLMENT_REQUIRED`, `COURSE_NOT_STARTED`: theo bảng điều kiện học tập.

### 1.5 Bình luận, thảo luận (UC016 bước 12–13)

- **Tác nhân**: Học viên thỏa điều kiện học tập.
- **Xem** — `GET /lectures/{lectureId}/comments` (200): bình luận gốc mới nhất trước, mỗi bình luận kèm các trả lời (cũ nhất
  trước). Phân trang theo bình luận gốc.
- **Thêm** — `POST /lectures/{lectureId}/comments` (201): gửi `content`; gửi thêm `parent_id` để trả lời bình luận của học
  viên khác. Chỉ trả lời một cấp: `parent_id` phải là bình luận gốc của cùng bài giảng.
- **Sửa** — `PUT /comments/{id}` (200): sửa nội dung bình luận của chính mình.
- **Xóa** — `DELETE /comments/{id}` (200): xóa mềm bình luận của chính mình. Xóa bình luận gốc thì các trả lời của nó cũng
  không còn hiển thị.
- **Kịch bản ngoại lệ**:
  - 400 `VALIDATION_ERROR`: nội dung trống; `parent_id` là một trả lời hoặc thuộc bài giảng khác.
  - 404 `NOT_FOUND`: bình luận không tồn tại, đã xóa hoặc không phải của mình (khi sửa, xóa).
  - 403 `ENROLLMENT_REQUIRED`, `COURSE_NOT_STARTED`: theo bảng điều kiện học tập.

### 1.6 Xem lịch sử khóa học và thông tin học viên (UC014, UC007 Bảng 2-14)

- **Tác nhân**: Giảng viên, Quản trị viên.
- **Lịch sử khóa học** — `GET /course-histories?name=&code=` (200): giảng viên thấy các khóa học chưa xóa của mình (mọi trạng
  thái); QTV thấy mọi khóa học chưa xóa. Mỗi khóa có `student_count`.
- **Danh sách học viên** — `GET /course-histories/{courseId}/students` (200): học viên đã ghi danh khóa học, gồm tên, email,
  ngày ghi danh, tiến độ.
- **Kịch bản ngoại lệ**:
  - Không có khóa học hoặc học viên nào → 200 với `items: []` (luồng 2a, 4a).
  - 404 `NOT_FOUND`: khóa học không tồn tại, đã xóa, hoặc giảng viên không phải chủ khóa học.

## 2. Kịch bản Kiểm thử (Test Cases)

| Mã TC | Loại | UC | Kịch bản | Input | Kết quả kỳ vọng |
|---|---|---|---|---|---|
| TC_LEARN_01 | Luồng chuẩn | UC016 | Ghi danh khóa học | Khóa `PUBLIC` chưa ghi danh | 201 |
| TC_LEARN_02 | Ngoại lệ | UC016 | Ghi danh trùng | Khóa đã ghi danh | 409 `ENROLLMENT_ALREADY_EXISTS` |
| TC_LEARN_03 | Ngoại lệ | UC016 | Ghi danh khóa `PRIVATE` | id khóa `PRIVATE` | 404 `NOT_FOUND` |
| TC_LEARN_04 | Luồng chuẩn | UC016 | Xem khóa học đã ghi danh | Đã hoàn thành 1/4 bài giảng | 200, `progress` = 25 |
| TC_LEARN_05 | Ngoại lệ | UC016 | Xem bài giảng trước ngày bắt đầu | `start_date` ở tương lai | 403 `COURSE_NOT_STARTED` |
| TC_LEARN_06 | Ngoại lệ | UC016 | Xem bài giảng khi chưa ghi danh | Khóa `PUBLIC` chưa ghi danh | 403 `ENROLLMENT_REQUIRED` |
| TC_LEARN_07 | Ngoại lệ | UC016 | Khóa chuyển `PRIVATE` sau khi ghi danh | Giảng viên đổi `status` | `/my-courses` không còn khóa; xem bài giảng 404 `NOT_FOUND` |
| TC_LEARN_08 | Luồng chuẩn | UC016 | Xem nội dung bài giảng | Bài giảng có bài tập | 200, đáp án không có cờ đúng |
| TC_LEARN_09 | Luồng chuẩn | UC016 | Lưu tạm đáp án | 1 trong 3 câu | 200, `status` = `DRAFT` |
| TC_LEARN_10 | Ngoại lệ | UC016 | Nộp khi còn câu chưa trả lời | Bài làm 1/3 câu | 400 `SUBMISSION_INCOMPLETE` |
| TC_LEARN_11 | Luồng chuẩn | UC016 | Nộp bài | Đủ 3 câu, đúng 2 | 200, `score` = 2, `total_questions` = 3, có đáp án đúng |
| TC_LEARN_12 | Ngoại lệ | UC016 | Nộp lại | Bài đã `SUBMITTED` | 409 `SUBMISSION_ALREADY_SUBMITTED` |
| TC_LEARN_13 | Ngoại lệ | UC016 | Lưu tạm sau khi nộp | Bài đã `SUBMITTED` | 409 `SUBMISSION_ALREADY_SUBMITTED` |
| TC_LEARN_14 | Ngoại lệ | UC016 | Đáp án không thuộc câu hỏi | `answer_id` của câu khác | 400 `VALIDATION_ERROR` |
| TC_LEARN_15 | Luồng chuẩn | UC016 | Xác nhận hoàn thành bài giảng | Bài giảng chưa hoàn thành | 200, `progress` tăng |
| TC_LEARN_16 | Luồng chuẩn | UC016 | Xác nhận hoàn thành lần hai | Bài giảng đã hoàn thành | 200, `progress` không đổi |
| TC_LEARN_17 | Luồng chuẩn | UC016 | Đăng bình luận | `content` hợp lệ | 201 |
| TC_LEARN_18 | Luồng chuẩn | UC016 | Trả lời bình luận | `parent_id` là bình luận gốc | 201, nằm trong `replies` của bình luận gốc |
| TC_LEARN_19 | Ngoại lệ | UC016 | Trả lời một trả lời | `parent_id` là một trả lời | 400 `VALIDATION_ERROR` |
| TC_LEARN_20 | Ngoại lệ | UC016 | Sửa bình luận của người khác | id bình luận người khác | 404 `NOT_FOUND` |
| TC_LEARN_21 | Luồng chuẩn | UC016 | Xóa bình luận của mình | id hợp lệ | 200, không còn trong danh sách |
| TC_LEARN_22 | Luồng chuẩn | UC014 | Giảng viên xem lịch sử khóa học | Giảng viên có 2 khóa | 200, chỉ 2 khóa của giảng viên đó |
| TC_LEARN_23 | Luồng chuẩn | UC014 | QTV xem lịch sử khóa học | — | 200, mọi khóa học chưa xóa |
| TC_LEARN_24 | Ngoại lệ | UC014 | Giảng viên xem học viên của khóa người khác | `courseId` người khác | 404 `NOT_FOUND` |
| TC_LEARN_25 | Ngoại lệ | UC014 | Học viên xem lịch sử khóa học | Token `STUDENT` | 403 `FORBIDDEN` |
