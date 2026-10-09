# Kế hoạch khởi tạo Frontend (FE) - E-Learning

## 1. Các file sẽ tạo mới / thay đổi

- **Cấu hình dự án Frontend (`FE/`):**
  - `FE/package.json`: Khai báo dependencies (React 19, TypeScript, Tailwind CSS, Axios, TanStack Query, React Router DOM, Lucide React, React Hook Form, Zod).
  - `FE/vite.config.ts`: Cấu hình build Vite, alias `@/` trỏ về `src/`, proxy API sang backend Spring Boot (`http://localhost:8080`).
  - `FE/tsconfig.json`, `FE/tsconfig.app.json`, `FE/tsconfig.node.json`: Cấu hình TypeScript nghiêm ngặt và path alias.
  - `FE/index.html`: File HTML gốc cho SPA.
  - `FE/tailwind.config.js`, `FE/postcss.config.js`: Cấu hình Tailwind CSS.
  - `FE/.env`: Cấu hình biến môi trường kết nối backend (`VITE_API_BASE_URL=http://localhost:8080`).
  - `FE/.gitignore`: Bỏ qua `node_modules`, `dist`, file môi trường cục bộ.

- **Mã nguồn khởi tạo (`FE/src/`):**
  - `FE/src/main.tsx`: Entry point ứng dụng React, bao bọc QueryClientProvider và Router.
  - `FE/src/App.tsx`: Điều hướng cơ bản (trang Home, Login).
  - `FE/src/index.css`: Cấu hình Tailwind CSS và biến CSS design tokens.
  - `FE/src/types/api.ts`: Định nghĩa Type TypeScript khớp với định dạng Spring Boot (`ApiResponse<T>`, `ErrorResponse`, `PageResponse<T>`).
  - `FE/src/api/axiosClient.ts`: Cấu hình Axios instance tự động đính kèm Bearer JWT Token và xử lý mã lỗi thống nhất.
  - `FE/src/lib/utils.ts`: Hàm tiện ích gộp class `cn` cho Tailwind.

---

## 2. Lý do thay đổi

- Thư mục `FE` hiện tại đang trống, chưa có mã nguồn client.
- Hệ thống cần một ứng dụng SPA (Single Page Application) độc lập bằng React + TypeScript để kết nối với REST API của Spring Boot.
- Lựa chọn Vite + TypeScript + Tailwind CSS giúp tối ưu tốc độ khởi động, dung lượng nhẹ, dễ phát triển và dễ bảo trì.

---

## 3. Hướng giải quyết & Code xem trước

### Hướng giải quyết:
- Chạy khởi tạo khung dự án React TypeScript trong thư mục `FE/`.
- Cài đặt bộ dependencies cần thiết:
  - `react-router-dom`: Quản lý định tuyến trang.
  - `axios`, `@tanstack/react-query`: Gọi API và quản lý cache/state server.
  - `tailwindcss`, `postcss`, `autoprefixer`, `clsx`, `tailwind-merge`: Hệ thống style giao diện.
  - `lucide-react`: Bộ icon giao diện hiện đại.
  - `react-hook-form`, `zod`, `@hookform/resolvers`: Quản lý và kiểm thực form.
- Thiết lập cấu trúc thư mục chuẩn trong `FE/src/`:
  - `api/`: Các hàm gọi API theo module (`auth`, `users`, `courses`, `learning`, `info`).
  - `components/`: UI components dùng chung (Buttons, Inputs, Dialogs, Tables...).
  - `hooks/`: Custom hooks.
  - `pages/`: Các màn hình theo từng vai trò (Admin, Giảng viên, Học viên).
  - `routes/`: Quản lý định tuyến và phân quyền (Public, Private theo Role).
  - `types/`: Type definitions DTO khớp với backend.

### Code xem trước:

#### Cấu hình Client gọi API (`FE/src/api/axiosClient.ts`):
```typescript
import axios from 'axios';
import type { ApiResponse } from '@/types/api';

const axiosClient = axios.create({
  baseURL: import.meta.env.VITE_API_BASE_URL || 'http://localhost:8080',
  headers: {
    'Content-Type': 'application/json',
  },
});

// gắn token xác thực vào header nếu có
axiosClient.interceptors.request.use((config) => {
  const token = localStorage.getItem('access_token');
  if (token) {
    config.headers.Authorization = `Bearer ${token}`;
  }
  return config;
});

// chuẩn hóa dữ liệu trả về theo format Spring Boot
axiosClient.interceptors.response.use(
  (response) => response.data,
  (error) => {
    // xử lý lỗi trả về từ GlobalExceptionHandler của backend
    const errorResponse = error.response?.data;
    return Promise.reject(errorResponse || error);
  }
);

export default axiosClient;
```

#### Định nghĩa Type đồng bộ Backend (`FE/src/types/api.ts`):
```typescript
// cấu trúc response chuẩn từ ApiResponse<T>
export interface ApiResponse<T> {
  success: boolean;
  data: T;
  msg: string;
}

// cấu trúc lỗi chuẩn từ ErrorResponse
export interface ErrorDetail {
  code: string;
  message: string;
  fields?: Record<string, string>;
  detail?: string;
}

export interface ErrorResponse {
  success: false;
  error: ErrorDetail;
}

// cấu trúc phân trang chuẩn PageResponse<T>
export interface PageMeta {
  page: number;
  pageSize: number;
  totalItems: number;
  totalPages: number;
}

export interface PageResponse<T> {
  items: T[];
  meta: PageMeta;
}
```
