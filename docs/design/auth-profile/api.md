# Thiết kế API & Biểu đồ tuần tự: Xác thực & Tài khoản

## 1. Mô tả API Endpoints

- `POST /api/v1/auth/login`: Xác thực người dùng, trả về Access Token.
- `POST /api/v1/auth/register`: Đăng ký tài khoản học viên mới.
- `POST /api/v1/auth/forgot-password`: Gửi email reset mật khẩu.
- `PUT /api/v1/users/profile`: Cập nhật thông tin cá nhân (Cần Header Authorization).
- `PUT /api/v1/users/password`: Thay đổi mật khẩu (Cần Header Authorization).

## 2. Biểu đồ Tuần tự (Sequence Diagram) - Đăng nhập

```mermaid
sequenceDiagram
    actor Client
    participant Ctrl as AuthController
    participant Svc as AuthService
    participant Repo as UserRepository
    participant DB as Database
    participant Handler as GlobalExceptionHandler

    Client->>Ctrl: POST /api/v1/auth/login (LoginRequestDTO)
    Ctrl->>Ctrl: @Valid kiểm tra DTO
    
    alt DTO không hợp lệ
        Ctrl->>Handler: MethodArgumentNotValidException
        Handler-->>Client: 400 Bad Request
    else DTO hợp lệ
        Ctrl->>Svc: authenticate(loginDTO)
        Svc->>Repo: findByEmail(loginDTO.email)
        Repo->>DB: SELECT * FROM users WHERE email
        DB-->>Repo: UserEntity
        Repo-->>Svc: Optional<UserEntity>
        
        alt Không tìm thấy hoặc Sai Pass
            Svc->>Handler: BadCredentialsException
            Handler-->>Client: 401 Unauthorized
        else Đăng nhập đúng
            Svc->>Svc: Generate JWT Token
            Svc-->>Ctrl: AuthResponseDTO
            Ctrl-->>Client: 200 OK (AuthResponseDTO)
        end
    end
```
