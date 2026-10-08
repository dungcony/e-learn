# Thiết kế API & Biểu đồ tuần tự: Quản lý Người dùng

Mọi endpoint chỉ dành cho `ADMIN`. Quy ước URL, response, phân trang và mã lỗi: xem [`../README.md`](../README.md).

## 1. Mô tả API Endpoints

| Method | URL | UC | Request | Response (`data`) |
|---|---|---|---|---|
| GET | `/admin/teachers` | UC006, UC008 | Query `UserSearchRequest` + phân trang | 200 `{items: UserSummaryResponse[], meta}` |
| GET | `/admin/teachers/{id}` | UC008 | — | 200 `UserDetailResponse` |
| POST | `/admin/teachers` | UC008 | `TeacherCreateRequest` | 201 `UserDetailResponse` |
| PUT | `/admin/teachers/{id}` | UC008 | `TeacherUpdateRequest` | 200 `UserDetailResponse` |
| PUT | `/admin/teachers/{id}/avatar` | UC008 | multipart, field `file` | 200 `UserDetailResponse` |
| DELETE | `/admin/teachers/{id}` | UC008 | — | 200 `null` |
| GET | `/admin/students` | UC006, UC010 | Query `UserSearchRequest` + phân trang | 200 `{items: UserSummaryResponse[], meta}` |
| GET | `/admin/students/{id}` | UC010 | — | 200 `UserDetailResponse` |
| PUT | `/admin/students/{id}/status` | UC010 | `UserStatusUpdateRequest` | 200 `UserDetailResponse` |
| DELETE | `/admin/students/{id}` | UC010 | — | 200 `null` |

Ví dụ tìm kiếm (UC006): `GET /admin/teachers?name=nguyen&gender=FEMALE&page=1&page_size=20`.

## 2. Biểu đồ Tuần tự (Sequence Diagram) - Thêm Giảng viên mới (UC008)

```mermaid
sequenceDiagram
    actor Admin
    participant Ctrl as AdminTeacherController
    participant Svc as UserServiceImpl
    participant Val as UserValidator
    participant Repo as UserRepository
    participant Handler as GlobalExceptionHandler

    Admin->>Ctrl: POST /admin/teachers (TeacherCreateRequest)
    Ctrl->>Ctrl: @PreAuthorize ADMIN, @Valid kiểm tra request

    alt Request không hợp lệ
        Ctrl->>Handler: MethodArgumentNotValidException
        Handler-->>Admin: 400 VALIDATION_ERROR
    else Request hợp lệ
        Ctrl->>Svc: createTeacher(request)
        Svc->>Val: validateEmailNotTaken(email, null)

        alt Email đã có tài khoản
            Val->>Handler: BusinessException(AUTH_EMAIL_ALREADY_EXISTS)
            Handler-->>Admin: 409 AUTH_EMAIL_ALREADY_EXISTS
        else Email chưa dùng
            Svc->>Svc: Map sang User, role = TEACHER, băm mật khẩu
            Svc->>Repo: save(user)
            Repo-->>Svc: User đã lưu
            Svc-->>Ctrl: UserDetailResponse
            Ctrl-->>Admin: 201 ApiResponse(UserDetailResponse)
        end
    end
```

## 3. Biểu đồ Tuần tự (Sequence Diagram) - Khóa học viên (UC010)

```mermaid
sequenceDiagram
    actor Admin
    participant Ctrl as AdminStudentController
    participant Svc as UserServiceImpl
    participant Repo as UserRepository
    participant Black as BlacklistedUserRepository
    participant Handler as GlobalExceptionHandler

    Admin->>Ctrl: PUT /admin/students/{id}/status (UserStatusUpdateRequest)
    Ctrl->>Svc: changeStudentStatus(id, status)
    Svc->>Repo: findByIdAndRole(id, STUDENT)

    alt Không tìm thấy
        Svc->>Handler: BusinessException(NOT_FOUND)
        Handler-->>Admin: 404 NOT_FOUND
    else Tìm thấy
        Svc->>Repo: Cập nhật status
        alt status = LOCKED
            Svc->>Black: add(userId, lý do, TTL = thời hạn access token)
        else status = ACTIVE
            Svc->>Black: deleteById(userId)
        end
        Svc-->>Ctrl: UserDetailResponse
        Ctrl-->>Admin: 200 ApiResponse(UserDetailResponse)
    end
```
