# e-learn

Hệ thống học tập trực tuyến (E-learning Courses) — backend Spring Boot 3.4, Java 17, PostgreSQL, Redis.

Tài liệu thiết kế dựng từ `docs/srs.pdf` nằm ở [`docs/design/`](docs/design/README.md): use case, API, entity cho 5 module
(`auth-profile`, `user-management`, `course-management`, `learning`, `info-management`).

## Chạy ứng dụng

Cần PostgreSQL (database `elearning`) và Redis. Schema do Flyway tạo lúc khởi động (`src/main/resources/db/migration`).

| Biến môi trường | Bắt buộc | Mặc định | Ý nghĩa |
|---|---|---|---|
| `JWT_SECRET` | có | — | Khóa HS256 dạng base64, tối thiểu 32 byte sau khi giải mã, vd `openssl rand -base64 48` |
| `DB_URL`, `DB_USERNAME`, `DB_PASSWORD` | không | `jdbc:postgresql://localhost:5432/elearning`, `elearning`, `elearning` | Kết nối PostgreSQL |
| `REDIS_HOST`, `REDIS_PORT` | không | `localhost`, `6379` | Redis dùng cho danh sách chặn token của tài khoản bị khóa hoặc xóa |
| `MAIL_HOST`, `MAIL_PORT`, `MAIL_USERNAME`, `MAIL_PASSWORD`, `MAIL_FROM` | không | `localhost`, `1025` | SMTP gửi email đặt lại mật khẩu |
| `APP_FRONTEND_URL` | không | `http://localhost:3000` | Gốc URL frontend trong link đặt lại mật khẩu |
| `APP_ADMIN_EMAIL`, `APP_ADMIN_PASSWORD` | không | — | Nếu có cả hai và chưa có Quản trị viên nào, tạo tài khoản QTV đầu tiên lúc khởi động |
| `APP_STORAGE_DIR` | không | `./uploads` | Thư mục lưu ảnh tải lên, phục vụ công khai ở `/files/**` |

```bash
export JWT_SECRET=$(openssl rand -base64 48)
export APP_ADMIN_EMAIL=admin@elearning.local APP_ADMIN_PASSWORD='đổi-mật-khẩu-này'
mvn spring-boot:run
```

## Kiểm thử

```bash
# Test đơn vị và kiểm tra kiến trúc (ArchUnit), không cần database
mvn test -Dtest='!ELearningApplicationTests'
```

`ELearningApplicationTests` bật cả Spring context nên cần PostgreSQL và Redis đang chạy cùng `JWT_SECRET`.
