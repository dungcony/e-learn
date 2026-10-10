# Thiết kế API & Biểu đồ tuần tự: Quản lý Người dùng

Mọi endpoint chỉ dành cho `MANAGER`. Quy ước URL, response, phân trang và mã lỗi: xem [`../README.md`](../README.md).

## 1. Mô tả API Endpoints

| Method | URL | UC | Request | Response (`data`) |
|---|---|---|---|---|
| GET | `/admin/staff` | UC006, UC008 | Query `UserSearchRequest` + phân trang | 200 `{items: UserSummaryResponse[], meta}` |
| GET | `/admin/staff/{id}` | UC008 | — | 200 `UserDetailResponse` |
| POST | `/admin/staff` | UC008 | `StaffCreateRequest` | 201 `UserDetailResponse` |
| PUT | `/admin/staff/{id}` | UC008 | `StaffUpdateRequest` | 200 `UserDetailResponse` |
| PUT | `/admin/staff/{id}/avatar` | UC008 | multipart, field `file` | 200 `UserDetailResponse` |
| PUT | `/admin/staff/{id}/status` | UC008 | `UserStatusUpdateRequest` | 200 `UserDetailResponse` |
| DELETE | `/admin/staff/{id}` | UC008 | — | 200 `null` |
| GET | `/admin/customers` | UC006, UC009 | Query `UserSearchRequest` + phân trang | 200 `{items: UserSummaryResponse[], meta}` |
| GET | `/admin/customers/{id}` | UC009 | — | 200 `UserDetailResponse` |
| PUT | `/admin/customers/{id}/status` | UC009 | `UserStatusUpdateRequest` | 200 `UserDetailResponse` |
| DELETE | `/admin/customers/{id}` | UC009 | — | 200 `null` |

Ví dụ tìm kiếm (UC006): `GET /admin/staff?name=tran&role=CASHIER&status=ACTIVE&page=1&page_size=20`.

Sắp xếp: `sort_by` nhận `created_at` (mặc định, giảm dần), `full_name`, `email`; giá trị khác rơi về mặc định
(`PageRequestParams.toPageable`).

`UserSearchRequest` ở `/admin/customers` bỏ qua `role`; ở `/admin/staff` bỏ qua `email_verified`.

## 2. Biểu đồ Tuần tự (Sequence Diagram) - Thêm nhân viên (UC008)

```mermaid
sequenceDiagram
    actor Manager as Quản lý
    participant Ctrl as AdminStaffController
    participant Svc as UserServiceImpl
    participant Val as UserValidator
    participant Repo as UserRepository
    participant Handler as GlobalExceptionHandler

    Manager->>Ctrl: POST /admin/staff (StaffCreateRequest)
    Ctrl->>Ctrl: @PreAuthorize MANAGER, @Valid kiểm tra request

    alt Request không hợp lệ
        Ctrl->>Handler: MethodArgumentNotValidException
        Handler-->>Manager: 400 VALIDATION_ERROR
    else Request hợp lệ
        Ctrl->>Svc: createStaff(request)
        Svc->>Val: validateEmailNotTaken(email, null)

        alt Email đã có tài khoản
            Val->>Handler: BusinessException(AUTH_EMAIL_ALREADY_EXISTS)
            Handler-->>Manager: 409 AUTH_EMAIL_ALREADY_EXISTS
        else Email chưa dùng
            Svc->>Svc: Map sang User, băm mật khẩu, email_verified = true, must_change_password = true
            Svc->>Repo: save(user)
            Repo-->>Svc: User đã lưu
            Svc-->>Ctrl: UserDetailResponse
            Ctrl-->>Manager: 201 ApiResponse(UserDetailResponse)
        end
    end
```

## 3. Biểu đồ Tuần tự (Sequence Diagram) - Khóa nhân viên (UC008)

```mermaid
sequenceDiagram
    actor Manager as Quản lý
    participant Ctrl as AdminStaffController
    participant Svc as UserServiceImpl
    participant Repo as UserRepository
    participant Black as BlacklistedUserRepository
    participant Handler as GlobalExceptionHandler

    Manager->>Ctrl: PUT /admin/staff/{id}/status (UserStatusUpdateRequest)
    Ctrl->>Svc: changeStaffStatus(currentUserId, id, status)

    alt id là chính người đang gọi
        Svc->>Handler: BusinessException(USER_CANNOT_MODIFY_SELF)
        Handler-->>Manager: 403 USER_CANNOT_MODIFY_SELF
    else Khác
        Svc->>Repo: findByIdAndRoleIn(id, staffRoles)

        alt Không tìm thấy
            Svc->>Handler: BusinessException(NOT_FOUND)
            Handler-->>Manager: 404 NOT_FOUND
        else Tìm thấy
            Svc->>Repo: Cập nhật status
            alt status = LOCKED
                Svc->>Black: add(userId, "LOCKED", TTL = thời hạn access token)
            else status = ACTIVE
                Svc->>Black: deleteById(userId)
            end
            Svc-->>Ctrl: UserDetailResponse
            Ctrl-->>Manager: 200 ApiResponse(UserDetailResponse)
        end
    end
```

## 4. Biểu đồ Tuần tự (Sequence Diagram) - Xóa nhân viên với guard (UC008)

```mermaid
sequenceDiagram
    actor Manager as Quản lý
    participant Ctrl as AdminStaffController
    participant Svc as UserServiceImpl
    participant Repo as UserRepository
    participant G1 as OrderUserDeletionGuard
    participant G2 as BillingUserDeletionGuard
    participant G3 as ReservationUserDeletionGuard
    participant Black as BlacklistedUserRepository
    participant Handler as GlobalExceptionHandler

    Manager->>Ctrl: DELETE /admin/staff/{id}
    Ctrl->>Svc: deleteStaff(currentUserId, id)
    Svc->>Repo: findByIdAndRoleIn(id, staffRoles)
    Svc->>G1: hasBlockingData(userId)
    Svc->>G2: hasBlockingData(userId)
    Svc->>G3: hasBlockingData(userId)

    alt Có guard trả true
        Svc->>Handler: BusinessException(USER_HAS_ACTIVITY)
        Handler-->>Manager: 409 USER_HAS_ACTIVITY
    else Không guard nào chặn
        Svc->>Repo: Gán deleted_at = now
        Svc->>Black: add(userId, "DELETED", TTL = thời hạn access token)
        Ctrl-->>Manager: 200 ApiResponse(null)
    end
```
