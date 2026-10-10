# Thiết kế API & Biểu đồ tuần tự: Quản lý Bàn

Quy ước URL, response, phân trang và mã lỗi: xem [`../README.md`](../README.md).

## 1. Mô tả API Endpoints

| Method | URL | Vai trò | UC | Request | Response (`data`) |
|---|---|---|---|---|---|
| GET | `/tables` | `WAITER`, `CASHIER`, `MANAGER` | UC007, UC011 | Query `TableSearchRequest` + phân trang | 200 `{items: TableResponse[], meta}` |
| GET | `/tables/{id}` | `WAITER`, `CASHIER`, `MANAGER` | UC011 | — | 200 `TableResponse` |
| POST | `/tables` | `MANAGER` | UC011 | `TableCreateRequest` | 201 `TableResponse` |
| PUT | `/tables/{id}` | `MANAGER` | UC011 | `TableUpdateRequest` | 200 `TableResponse` |
| DELETE | `/tables/{id}` | `MANAGER` | UC011 | — | 200 `null` |

Ghi chú:

- `sort_by` nhận `name` (mặc định), `capacity`, `zone`, `status`. Chuỗi tên bàn so sánh theo thứ tự chữ; muốn `B2` đứng trước `B10`
  thì đặt tên có số 0 đệm (`B02`, `B10`).
- Hàm nội bộ cho module khác (không có endpoint), chi tiết ở `lopthucthe.md`: `listBookableTables`, `getTableBriefs`,
  `transitionStatus`.

Ví dụ `GET /tables?zone=VIP_ROOM&min_capacity=6`:

```json
{
  "success": true,
  "data": {
    "items": [
      { "id": "0b3f5c2e-9a77-4b51-b3a1-5f8d1c2a9e10", "name": "V01", "zone": "VIP_ROOM", "capacity": 8, "status": "AVAILABLE", "note": "Có máy chiếu" }
    ],
    "meta": { "page": 1, "page_size": 20, "total_items": 1, "total_pages": 1 }
  },
  "msg": ""
}
```

## 2. Biểu đồ Tuần tự (Sequence Diagram) - Sửa bàn (UC011)

```mermaid
sequenceDiagram
    actor Manager as Quản lý
    participant Ctrl as TableController
    participant Svc as TableServiceImpl
    participant Val as TableValidator
    participant Repo as TableRepository
    participant Guards as List~TableDeletionGuard~
    participant Handler as GlobalExceptionHandler

    Manager->>Ctrl: PUT /tables/{id} (TableUpdateRequest)
    Ctrl->>Ctrl: @PreAuthorize MANAGER, @Valid kiểm tra request
    Ctrl->>Svc: updateTable(id, request)
    Svc->>Repo: findById(id)

    alt Không tìm thấy
        Svc->>Handler: BusinessException(NOT_FOUND)
        Handler-->>Manager: 404 NOT_FOUND
    else Tìm thấy
        Svc->>Val: validateNameUnique(name, id)
        Svc->>Val: validateStatusChange(table, newStatus)
        Svc->>Guards: maxGuestCountAssigned(id)

        alt Tên trùng
            Val->>Handler: BusinessException(TABLE_NAME_EXISTS)
            Handler-->>Manager: 409 TABLE_NAME_EXISTS
        else Bàn đang RESERVED/OCCUPIED mà đổi trạng thái
            Val->>Handler: BusinessException(TABLE_STATUS_NOT_EDITABLE)
            Handler-->>Manager: 409 TABLE_STATUS_NOT_EDITABLE
        else capacity mới < số khách đã gán
            Val->>Handler: BusinessException(TABLE_CAPACITY_BELOW_RESERVATION)
            Handler-->>Manager: 409 TABLE_CAPACITY_BELOW_RESERVATION
        else Hợp lệ
            Svc->>Repo: save(table)
            Svc-->>Ctrl: TableResponse
            Ctrl-->>Manager: 200 ApiResponse(TableResponse)
        end
    end
```

## 3. Biểu đồ Tuần tự (Sequence Diagram) - Xóa bàn (UC011)

```mermaid
sequenceDiagram
    actor Manager as Quản lý
    participant Ctrl as TableController
    participant Svc as TableServiceImpl
    participant Repo as TableRepository
    participant G1 as ReservationTableDeletionGuard
    participant G2 as OrderTableDeletionGuard
    participant Handler as GlobalExceptionHandler

    Manager->>Ctrl: DELETE /tables/{id}
    Ctrl->>Svc: deleteTable(id)
    Svc->>Repo: findById(id)

    alt Không tìm thấy
        Svc->>Handler: BusinessException(NOT_FOUND)
        Handler-->>Manager: 404 NOT_FOUND
    else Bàn đang OCCUPIED
        Svc->>Handler: BusinessException(TABLE_IN_USE)
        Handler-->>Manager: 409 TABLE_IN_USE
    else Còn lại
        Svc->>G1: hasBlockingData(id)
        Svc->>G2: hasBlockingData(id)

        alt Có guard trả true
            Svc->>Handler: BusinessException(TABLE_IN_USE)
            Handler-->>Manager: 409 TABLE_IN_USE
        else Không guard nào chặn
            Svc->>Repo: delete(table)
            Ctrl-->>Manager: 200 ApiResponse(null)
        end
    end
```
