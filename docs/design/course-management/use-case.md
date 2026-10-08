# Kịch bản Use Case & Test Cases: Quản lý Đào tạo

Quy ước chung, mã lỗi dùng chung và cách hiểu SRS: xem [`../README.md`](../README.md). Mọi kiểm tra quyền sở hữu không
đạt đều trả 404 `NOT_FOUND` (rule 2.9).

## 1. Kịch bản Use Case

### 1.1 Quản lý thể loại khóa học (UC015)

- **Tác nhân**: Giảng viên (`TEACHER`).
- **Xem, Tìm kiếm (R, S)** — `GET /categories?name=` (200): trả mọi thể loại chưa xóa, lọc theo tên chứa chuỗi (A9). Không có
  kết quả → `items: []` (luồng 2a).
- **Thêm (C)** — `POST /categories` (201): nhập `name` (bắt buộc, ≤ 255 ký tự, Bảng 2-32). Tên không được trùng (không phân
  biệt hoa thường) với thể loại chưa xóa. Người tạo là giảng viên hiện tại.
- **Sửa (U)** — `PUT /categories/{id}` (200): chỉ giảng viên tạo ra thể loại được sửa (ghi chú UC015).
- **Xóa (D)** — `DELETE /categories/{id}` (200): chỉ người tạo được xóa, và chỉ khi không còn khóa học chưa xóa nào thuộc thể
  loại này (ghi chú UC015). Xóa mềm.
- **Kịch bản ngoại lệ**:
  - 400 `VALIDATION_ERROR`: tên trống hoặc quá 255 ký tự (luồng 4a).
  - 409 `COURSE_CATEGORY_NAME_EXISTS`: trùng tên.
  - 409 `COURSE_CATEGORY_IN_USE`: xóa khi còn khóa học.
  - 404 `NOT_FOUND`: thể loại không tồn tại, đã xóa hoặc không phải của giảng viên hiện tại (khi sửa, xóa).

### 1.2 Tìm kiếm và xem khóa học công khai (UC007, mục 3.1 của SRS)

- **Tác nhân**: Khách, Học viên (A9).
- **Tìm kiếm** — `GET /courses` (200): lọc theo Bảng 2-13, trừ trạng thái: `code`, `name` (chứa chuỗi), `price` (bằng giá),
  `start_date` (bắt đầu từ ngày này trở đi), `end_date` (kết thúc trước hoặc đúng ngày này). Chỉ trả khóa học `PUBLIC` chưa xóa
  (QĐ1).
- **Xem chi tiết** — `GET /courses/{id}` (200): tên, mã, mô tả, giá, thời gian, ảnh, tài liệu tham khảo, thể loại, giảng viên
  và danh sách tên bài giảng. Nội dung bài giảng chỉ mở cho học viên đã ghi danh (module `learning`).
- **Kịch bản ngoại lệ**: 404 `NOT_FOUND` khi khóa học không tồn tại, đã xóa, hoặc `PRIVATE` mà người xem không phải giảng viên
  chủ khóa học.

### 1.3 Quản lý khóa học (UC009)

- **Tác nhân**: Giảng viên (`TEACHER`).
- **Xem, Tìm kiếm (R, S)** — `GET /teachers/me/courses` (200): chỉ khóa học của giảng viên hiện tại, gồm cả `PRIVATE`. Lọc như
  mục 1.2 và thêm `status`. Xem chi tiết qua `GET /courses/{id}`.
- **Thêm (C)** — `POST /courses` (201): nhập các trường Bảng 2-19 và A1. Bắt buộc: `title` (≤ 255 ký tự), `description`,
  `start_date`, `end_date` (phải sau `start_date`), `status`, `category_id`. Tùy chọn: `price` (≥ 0, mặc định 0),
  `reference_materials`. Hệ thống sinh `code` dạng `CO` + 6 chữ số, không trùng. Giảng viên hiện tại là chủ khóa học.
- **Sửa (U)** — `PUT /courses/{id}` (200): sửa các trường như khi thêm; `code` không đổi. Đổi `status` là thao tác Mở khóa/Khóa
  khóa học (A7). Ảnh minh họa: `PUT /courses/{id}/image`.
- **Xóa (D)** — `DELETE /courses/{id}` (200): client hỏi xác nhận trước khi gọi. Xóa mềm; ghi danh và bài làm giữ nguyên nhưng
  học viên không còn thấy khóa học.
- **Kịch bản ngoại lệ**:
  - 400 `VALIDATION_ERROR`: thiếu trường bắt buộc, sai định dạng (luồng 4a).
  - 400 `COURSE_DATE_RANGE_INVALID`: `end_date` không sau `start_date`.
  - 400 `FILE_TYPE_NOT_SUPPORTED`: ảnh sai định dạng.
  - 404 `NOT_FOUND`: thể loại không tồn tại; khóa học không tồn tại, đã xóa hoặc của giảng viên khác.

### 1.4 Quản lý bài giảng và bài tập (UC011)

- **Tác nhân**: Giảng viên chủ khóa học.
- **Bài giảng**:
  - Xem, Tìm kiếm (R, S): `GET /courses/{courseId}/lectures?name=` (Bảng 2-15); chi tiết
    `GET /courses/{courseId}/lectures/{id}` kèm danh sách bài tập của bài giảng.
  - Thêm (C): `POST /courses/{courseId}/lectures` (201), nhập theo Bảng 2-22: `title` (bắt buộc, ≤ 255 ký tự), `description`
    (tùy chọn), `content_url` (bắt buộc, là URL tới video hoặc tài liệu). Khóa học lấy từ URL, người tạo là giảng viên hiện
    tại (A2).
  - Sửa (U): `PUT /courses/{courseId}/lectures/{id}` (200).
  - Xóa (D): `DELETE /courses/{courseId}/lectures/{id}` (200), xóa mềm. Bài tập của bài giảng không còn truy cập được vì mọi
    truy cập đều đi qua bài giảng.
- **Bài tập** (Bảng 2-23 đến 2-25):
  - Thêm (CE): `POST /lectures/{lectureId}/exercises` (201), nhập `title`, `description` (đều bắt buộc) và danh sách câu hỏi.
    Mỗi câu hỏi có `content` và đúng 4 đáp án; mỗi đáp án có `content` và `is_correct`; mỗi câu đúng 1 đáp án đúng.
  - Xem chi tiết: `GET /lectures/{lectureId}/exercises/{id}` (200), kèm câu hỏi, đáp án và cờ đáp án đúng.
  - Sửa (U): `PUT /lectures/{lectureId}/exercises/{id}` (200), gửi lại toàn bộ bài tập. Câu hỏi và đáp án cũ bị xóa mềm rồi
    tạo mới; bài làm cũ của học viên vẫn trỏ tới câu cũ.
  - Xóa (D): `DELETE /lectures/{lectureId}/exercises/{id}` (200), xóa mềm.
- **Kịch bản ngoại lệ**:
  - 400 `VALIDATION_ERROR`: thiếu trường bắt buộc, `content_url` không phải URL, câu hỏi không đủ 4 đáp án.
  - 400 `EXERCISE_ANSWER_INVALID`: một câu hỏi có số đáp án đúng khác 1.
  - 404 `NOT_FOUND`: khóa học, bài giảng hoặc bài tập không tồn tại, đã xóa, không thuộc cha trong URL, hoặc không phải của
    giảng viên hiện tại.

## 2. Kịch bản Kiểm thử (Test Cases)

| Mã TC | Loại | UC | Kịch bản | Input | Kết quả kỳ vọng |
|---|---|---|---|---|---|
| TC_COURSE_01 | Luồng chuẩn | UC015 | Tạo thể loại | `name` mới | 201 |
| TC_COURSE_02 | Ngoại lệ | UC015 | Tạo thể loại trùng tên | Tên đã có, khác hoa thường | 409 `COURSE_CATEGORY_NAME_EXISTS` |
| TC_COURSE_03 | Ngoại lệ | UC015 | Sửa thể loại của giảng viên khác | id thể loại người khác tạo | 404 `NOT_FOUND` |
| TC_COURSE_04 | Ngoại lệ | UC015 | Xóa thể loại còn khóa học | Thể loại có khóa học chưa xóa | 409 `COURSE_CATEGORY_IN_USE` |
| TC_COURSE_05 | Luồng chuẩn | UC015 | Xóa thể loại chỉ còn khóa học đã xóa | Mọi khóa học của thể loại đã xóa | 200 |
| TC_COURSE_06 | Luồng chuẩn | UC009 | Tạo khóa học | Đủ trường bắt buộc | 201, `code` dạng `CO` + 6 chữ số |
| TC_COURSE_07 | Ngoại lệ | UC009 | Ngày kết thúc trước ngày bắt đầu | `end_date` < `start_date` | 400 `COURSE_DATE_RANGE_INVALID` |
| TC_COURSE_08 | Ngoại lệ | UC009 | Thể loại không tồn tại | `category_id` sai | 404 `NOT_FOUND` |
| TC_COURSE_09 | Ngoại lệ | UC009 | Sửa khóa học của giảng viên khác | id khóa học người khác | 404 `NOT_FOUND` |
| TC_COURSE_10 | Luồng chuẩn | UC009 | Giảng viên xem khóa học của mình | Có khóa `PUBLIC` và `PRIVATE` | 200, có cả hai khóa |
| TC_COURSE_11 | Ngoại lệ | UC009 | Ảnh khóa học sai định dạng | File `.bmp` | 400 `FILE_TYPE_NOT_SUPPORTED` |
| TC_COURSE_12 | Luồng chuẩn | UC007 | Khách tìm khóa học theo tên | `name=math` | 200, chỉ khóa `PUBLIC` |
| TC_COURSE_13 | Ngoại lệ | UC007 | Học viên xem khóa `PRIVATE` | id khóa `PRIVATE` | 404 `NOT_FOUND` |
| TC_COURSE_14 | Luồng chuẩn | UC011 | Thêm bài giảng | Dữ liệu hợp lệ | 201 |
| TC_COURSE_15 | Ngoại lệ | UC011 | Đường dẫn tài liệu sai | `content_url` = `abc` | 400 `VALIDATION_ERROR` |
| TC_COURSE_16 | Ngoại lệ | UC011 | Thêm bài giảng vào khóa của giảng viên khác | `courseId` người khác | 404 `NOT_FOUND` |
| TC_COURSE_17 | Luồng chuẩn | UC011 | Tìm bài giảng theo tên | `name=chapter` | 200, chỉ bài giảng của khóa đó |
| TC_COURSE_18 | Luồng chuẩn | UC011 | Thêm bài tập | 2 câu hỏi, mỗi câu 4 đáp án, 1 đúng | 201 |
| TC_COURSE_19 | Ngoại lệ | UC011 | Câu hỏi thiếu đáp án | Một câu có 3 đáp án | 400 `VALIDATION_ERROR` |
| TC_COURSE_20 | Ngoại lệ | UC011 | Câu hỏi có 2 đáp án đúng | Hai `is_correct` = true | 400 `EXERCISE_ANSWER_INVALID` |
| TC_COURSE_21 | Luồng chuẩn | UC011 | Sửa bài tập | Danh sách câu hỏi mới | 200, chi tiết chỉ còn câu hỏi mới |
| TC_COURSE_22 | Luồng chuẩn | UC011 | Xóa bài giảng | id hợp lệ | 200, không còn trong danh sách |
| TC_COURSE_23 | Ngoại lệ | Chung | Học viên tạo khóa học | Token `STUDENT` gọi `POST /courses` | 403 `FORBIDDEN` |
