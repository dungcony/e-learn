// cấu trúc phản hồi chuẩn từ ApiResponse của Spring Boot backend
export interface ApiResponse<T> {
  success: boolean;
  data: T;
  msg: string;
}

// chi tiết lỗi từ ErrorResponse của Spring Boot
export interface ErrorDetail {
  code: string;
  message: string;
  fields?: Record<string, string>;
  detail?: string;
}

// cấu trúc phản hồi lỗi khi backend ném exception
export interface ErrorResponse {
  success: false;
  error: ErrorDetail;
}

// thông tin phân trang từ PageMeta
export interface PageMeta {
  page: number;
  pageSize: number;
  totalItems: number;
  totalPages: number;
}

// dữ liệu phân trang từ PageResponse
export interface PageResponse<T> {
  items: T[];
  meta: PageMeta;
}

// thông tin người dùng cơ bản
export interface UserProfile {
  id: string;
  email: string;
  fullName: string;
  role: 'ADMIN' | 'INSTRUCTOR' | 'STUDENT';
  avatarUrl?: string;
}
