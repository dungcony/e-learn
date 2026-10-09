# Thiết kế API & Biểu đồ tuần tự: Xác thực & Tài khoản

Quy ước URL, response, phân trang và mã lỗi: xem [`../README.md`](../README.md).

## 1. Mô tả API Endpoints

| Method | URL | Vai trò | UC | Request | Response (`data`) |
|---|---|---|---|---|---|
| POST | `/auth/register` | Khách | UC004 | `UserRegisterRequest` | 201 `ProfileResponse` |
| POST | `/auth/login` | Khách | UC001 | `UserLoginRequest` | 200 `AuthResponse` |
| POST | `/auth/forgot-password` | Khách | UC003 | `PasswordForgotRequest` | 200 `null` |
| POST | `/auth/reset-password` | Khách | UC003 | `PasswordResetRequest` | 200 `null` |
| GET | `/users/me` | Đã đăng nhập | UC005 | — | 200 `ProfileResponse` |
| PUT | `/users/me` | Đã đăng nhập | UC005, UC002 (GV, QTV) | `ProfileUpdateRequest` | 200 `ProfileResponse` |
| PUT | `/users/me/avatar` | Đã đăng nhập | UC005 | multipart, field `file` | 200 `ProfileResponse` |
| PUT | `/users/me/password` | `STUDENT` | UC002 | `PasswordChangeRequest` | 200 `null` |

Ghi chú:

- Bốn endpoint `/auth/*` là công khai và đã được mở trong `SecurityConfig`. `forgot-password`, `reset-password` giữ dạng động
  từ cho khớp cấu hình đó.
- Link trong email có dạng `<frontend-url>/reset-password?token=<token>`. Frontend lấy `token` rồi gọi
  `POST /auth/reset-password`. `frontend-url` cấu hình bằng `@ConfigurationProperties`.

Ví dụ `POST /auth/login`:

```json
{ "email": "qndev@gmail.com", "password": "123456" }
```

```json
{
  "success": true,
  "data": { "access_token": "eyJ...", "user_id": "4f1c...", "email": "qndev@gmail.com", "role": "STUDENT" },
  "msg": ""
}
```

## 2. Biểu đồ Tuần tự (Sequence Diagram) - Đăng nhập (UC001)

```mermaid
sequenceDiagram
    actor Client
    participant Ctrl as AuthController
    participant Svc as AuthServiceImpl
    participant Repo as UserRepository
    participant Jwt as JwtService
    participant Handler as GlobalExceptionHandler

    Client->>Ctrl: POST /auth/login (UserLoginRequest)
    Ctrl->>Ctrl: @Valid kiểm tra request

    alt Request không hợp lệ
        Ctrl->>Handler: MethodArgumentNotValidException
        Handler-->>Client: 400 VALIDATION_ERROR
    else Request hợp lệ
        Ctrl->>Svc: login(request)
        Svc->>Repo: findByEmail(email)
        Note over Repo: @SQLRestriction bỏ qua tài khoản đã xóa
        Repo-->>Svc: User hoặc rỗng

        alt Không có tài khoản hoặc sai mật khẩu
            Svc->>Handler: BusinessException(AUTH_CREDENTIALS_INVALID)
            Handler-->>Client: 401 AUTH_CREDENTIALS_INVALID
        else Tài khoản LOCKED
            Svc->>Handler: BusinessException(AUTH_ACCOUNT_BLOCKED)
            Handler-->>Client: 403 AUTH_ACCOUNT_BLOCKED
        else Hợp lệ
            Svc->>Jwt: generateAccessToken(userId, ROLE_xxx)
            Jwt-->>Svc: accessToken
            Svc-->>Ctrl: AuthResponse
            Ctrl-->>Client: 200 ApiResponse(AuthResponse)
        end
    end
```

## 3. Biểu đồ Tuần tự (Sequence Diagram) - Thiết lập lại mật khẩu (UC003)

```mermaid
sequenceDiagram
    actor User
    participant Ctrl as AuthController
    participant Svc as AuthServiceImpl
    participant UserRepo as UserRepository
    participant TokenRepo as PasswordResetTokenRepository
    participant Mail as EmailService
    participant Handler as GlobalExceptionHandler

    User->>Ctrl: POST /auth/forgot-password (PasswordForgotRequest)
    Ctrl->>Svc: requestPasswordReset(PasswordForgotRequest)
    Svc->>UserRepo: findByEmail(email)

    alt Không có tài khoản
        Svc->>Handler: BusinessException(NOT_FOUND)
        Handler-->>User: 404 NOT_FOUND
    else Có tài khoản
        Svc->>Svc: Sinh token ngẫu nhiên và băm token
        Svc->>TokenRepo: save(userId, tokenHash, expiresAt = now + 60 phút)
        Svc->>Mail: sendEmail(email, tiêu đề, link chứa token)
        Ctrl-->>User: 200
    end

    User->>Ctrl: POST /auth/reset-password (PasswordResetRequest)
    Ctrl->>Svc: resetPassword(request)
    Svc->>TokenRepo: findByTokenHash(hash(token))

    alt Không có, quá hạn hoặc đã dùng
        Svc->>Handler: BusinessException(AUTH_CODE_INVALID)
        Handler-->>User: 400 AUTH_CODE_INVALID
    else Hợp lệ
        Svc->>Svc: Kiểm tra new_password trùng confirm_password
        Svc->>UserRepo: Cập nhật passwordHash
        Svc->>TokenRepo: Đánh dấu usedAt = now
        Ctrl-->>User: 200
    end
```

Email gửi sau khi transaction commit, để không giữ kết nối DB trong lúc gọi SMTP.
