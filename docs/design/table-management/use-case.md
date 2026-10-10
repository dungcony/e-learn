# Kịch bản Use Case & Test Cases: Quản lý Bàn

Tác nhân: Quản lý (UC011, ghi dữ liệu). Nhân viên phục vụ và Thu ngân chỉ xem sơ đồ bàn và tìm bàn (UC007). Quy ước chung và cách
hiểu SRS: xem [`../README.md`](../README.md).

## 1. Kịch bản Use Case

### 1.1 Tìm kiếm bàn (UC007)

- **Kịch bản chính (200)** — `GET /tables`: nhập một hoặc nhiều tiêu chí của Bảng 2-15: `name` (chứa chuỗi, không phân biệt hoa thường),
  `zone`, `min_capacity` (sức chứa tối thiểu), `status`. Hệ thống trả danh sách phân trang thỏa mọi tiêu chí. Quản lý, Nhân viên phục
  vụ và Thu ngân dùng được; vai trò khác → 403 `FORBIDDEN`.
- **Sơ đồ bàn**: không có endpoint riêng. Client gọi `GET /tables?page_size=100&sort_by=name&sort_order=asc` rồi gom nhóm theo `zone`,
  tô màu theo `status`, làm mới ~10 giây/lần (README mục 2.11). Muốn biết bàn `OCCUPIED` đang gọi món gì thì gọi tiếp `GET /orders?status=OPEN`
  của module `ordering`.
- **Kịch bản ngoại lệ**:
  - Không bàn nào thỏa mãn → 200 với `items: []`.
  - `zone`, `status` sai giá trị hoặc `min_capacity` không dương → 400 `VALIDATION_ERROR`.

### 1.2 Quản lý bàn (UC011)

- **Xem (R)**: danh sách `GET /tables` và chi tiết `GET /tables/{id}`.
  - Ngoại lệ: id không tồn tại → 404 `NOT_FOUND`.
- **Thêm (C)** — `POST /tables` (201): nhập `name`, `zone`, `capacity`, `note` (Bảng 2-24). Bàn mới luôn có `status = AVAILABLE` (hậu
  điều kiện UC011), nên request không có `status`.
  - Ngoại lệ: 400 `VALIDATION_ERROR` (luồng 4a); 409 `TABLE_NAME_EXISTS` (luồng 4b).
- **Sửa (U)** — `PUT /tables/{id}` (200): gửi `name`, `zone`, `capacity`, `note`, và `status` tùy chọn. Quản lý chỉ đặt tay
  `AVAILABLE` hoặc `OUT_OF_SERVICE` (ghi chú Bảng 2-24); `RESERVED`, `OCCUPIED` do hệ thống cập nhật.
  - Ngoại lệ: 400 `VALIDATION_ERROR` (gồm `status` là `RESERVED`/`OCCUPIED`); 404 `NOT_FOUND`; 409 `TABLE_NAME_EXISTS`;
    409 `TABLE_CAPACITY_BELOW_RESERVATION` nếu giảm sức chứa xuống thấp hơn số khách của đặt bàn đang gán cho bàn (luồng 4b);
    409 `TABLE_STATUS_NOT_EDITABLE` nếu đổi `status` khi bàn đang `RESERVED` hoặc `OCCUPIED`;
    409 `TABLE_IN_USE` nếu đặt `OUT_OF_SERVICE` khi bàn còn đặt bàn `CONFIRMED` chưa hết khung giờ hoặc còn đơn `OPEN`.
- **Xóa (D)** — `DELETE /tables/{id}` (200): client hỏi xác nhận trước khi gọi. Hệ thống kiểm tra bàn không `OCCUPIED`, chạy các
  `TableDeletionGuard`, rồi xóa cứng.
  - Ngoại lệ: 409 `TABLE_IN_USE` nếu bàn đang phục vụ hoặc còn đặt bàn chưa hoàn tất (luồng 4a); 404 `NOT_FOUND`.
  - Bàn từng có đặt bàn hoặc đơn đã hoàn tất vẫn xóa được; dữ liệu cũ giữ `table_id` và ảnh chụp tên bàn (trong hóa đơn).

### 1.3 Hệ thống cập nhật trạng thái bàn

Các chuyển trạng thái do module khác gọi qua `TableService`, bằng cập nhật có điều kiện `WHERE status IN (...)` (không đọc rồi ghi):

| Sự kiện | Chuyển | Gọi bởi |
|---|---|---|
| Còn ≤ 60 phút tới giờ hẹn của đặt bàn `CONFIRMED` (job) | `AVAILABLE` → `RESERVED` | `reservation` |
| Hủy / không đến một đặt bàn mà bàn đang `RESERVED` vì đặt bàn đó | `RESERVED` → `AVAILABLE` | `reservation` |
| Nhận bàn (check-in) | `AVAILABLE` hoặc `RESERVED` → `OCCUPIED` | `reservation` qua `order` |
| Mở đơn cho khách vãng lai | `AVAILABLE` → `OCCUPIED` | `order` |
| Thanh toán, hoặc hủy đơn chưa có món | `OCCUPIED` → `AVAILABLE` | `billing`, `order` |

Cập nhật không thành công (0 dòng bị ảnh hưởng) nghĩa là bàn đã đổi trạng thái giữa chừng; module gọi ném lỗi nghiệp vụ của mình
(`ORDER_TABLE_NOT_OPENABLE`, `RESERVATION_TABLE_NOT_READY`).

## 2. Kịch bản Kiểm thử (Test Cases)

| Mã TC | Loại | UC | Kịch bản | Input | Kết quả kỳ vọng |
|---|---|---|---|---|---|
| TC_TABLE_01 | Luồng chuẩn | UC007 | Tìm bàn theo khu vực và sức chứa | `zone=VIP_ROOM&min_capacity=6` | 200, chỉ bàn phòng VIP có sức chứa ≥ 6 |
| TC_TABLE_02 | Luồng chuẩn | UC007 | Tìm bàn theo trạng thái | `status=AVAILABLE` | 200, chỉ bàn trống |
| TC_TABLE_03 | Luồng chuẩn | UC007 | Tìm bàn theo tên | `name=b0` | 200, bàn có tên chứa "b0" |
| TC_TABLE_04 | Ngoại lệ | UC007 | Khu vực sai | `zone=ROOFTOP` | 400 `VALIDATION_ERROR` |
| TC_TABLE_05 | Ngoại lệ | UC007 | Bếp xem sơ đồ bàn | Token `CHEF` | 403 `FORBIDDEN` |
| TC_TABLE_06 | Luồng chuẩn | UC011 | Thêm bàn | `name=B05, zone=INDOOR, capacity=6` | 201, `status` = `AVAILABLE` |
| TC_TABLE_07 | Ngoại lệ | UC011 | Thêm bàn thiếu sức chứa | Không có `capacity` | 400 `VALIDATION_ERROR` |
| TC_TABLE_08 | Ngoại lệ | UC011 | Sức chứa ngoài 1–50 | `capacity=0` hoặc `51` | 400 `VALIDATION_ERROR` |
| TC_TABLE_09 | Ngoại lệ | UC011 | Tên bàn quá 20 ký tự | 21 ký tự | 400 `VALIDATION_ERROR` |
| TC_TABLE_10 | Ngoại lệ | UC011 | Trùng tên bàn | Tên đã có (không phân biệt hoa thường) | 409 `TABLE_NAME_EXISTS` |
| TC_TABLE_11 | Luồng chuẩn | UC011 | Sửa thông tin bàn | Đổi khu vực, ghi chú | 200, dữ liệu mới |
| TC_TABLE_12 | Luồng chuẩn | UC011 | Tạm khóa bàn trống | `status=OUT_OF_SERVICE` | 200; bàn không còn được gán cho đặt bàn mới |
| TC_TABLE_13 | Luồng chuẩn | UC011 | Mở lại bàn | `status=AVAILABLE` từ `OUT_OF_SERVICE` | 200 |
| TC_TABLE_14 | Ngoại lệ | UC011 | Quản lý đặt tay trạng thái hệ thống | `status=OCCUPIED` | 400 `VALIDATION_ERROR` |
| TC_TABLE_15 | Ngoại lệ | UC011 | Đổi trạng thái bàn đang phục vụ | Bàn `OCCUPIED`, `status=OUT_OF_SERVICE` | 409 `TABLE_STATUS_NOT_EDITABLE` |
| TC_TABLE_16 | Ngoại lệ | UC011 | Tạm khóa bàn còn đặt bàn | Bàn `AVAILABLE` có đặt bàn `CONFIRMED` chưa hết giờ | 409 `TABLE_IN_USE` |
| TC_TABLE_17 | Ngoại lệ | UC011 | Giảm sức chứa dưới số khách đã gán | Bàn 6 chỗ có đặt bàn 5 khách, sửa `capacity=4` | 409 `TABLE_CAPACITY_BELOW_RESERVATION` |
| TC_TABLE_18 | Luồng chuẩn | UC011 | Giảm sức chứa vẫn đủ | Bàn 6 chỗ có đặt bàn 5 khách, sửa `capacity=5` | 200 |
| TC_TABLE_19 | Luồng chuẩn | UC011 | Xóa bàn không có dữ liệu liên quan | Bàn `AVAILABLE`, chưa từng có đặt bàn/đơn | 200; `GET /tables/{id}` trả 404 |
| TC_TABLE_20 | Ngoại lệ | UC011 | Xóa bàn đang phục vụ | Bàn `OCCUPIED` | 409 `TABLE_IN_USE` |
| TC_TABLE_21 | Ngoại lệ | UC011 | Xóa bàn còn đặt bàn | Có đặt bàn `CONFIRMED` chưa hết giờ | 409 `TABLE_IN_USE` |
| TC_TABLE_22 | Luồng chuẩn | UC011 | Xóa bàn chỉ có đặt bàn cũ | Mọi đặt bàn `CHECKED_IN`/`CANCELLED`/`NO_SHOW`, đơn đã đóng | 200 |
| TC_TABLE_23 | Ngoại lệ | UC011 | Thu ngân sửa bàn | Token `CASHIER` gọi `PUT /tables/{id}` | 403 `FORBIDDEN` |
| TC_TABLE_24 | Ngoại lệ | UC011 | Xem bàn không tồn tại | id sai | 404 `NOT_FOUND` |
| TC_TABLE_25 | Luồng chuẩn | Hệ thống | Chuyển trạng thái có điều kiện | Gọi `occupy` hai lần song song cho một bàn `AVAILABLE` | Một lần thành công, một lần nhận `false` |
