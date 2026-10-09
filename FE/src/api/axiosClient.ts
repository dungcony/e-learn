import axios, { AxiosError, InternalAxiosRequestConfig } from 'axios';
import type { ErrorResponse } from '@/types/api';

const axiosClient = axios.create({
  baseURL: import.meta.env.VITE_API_BASE_URL || 'http://localhost:8080',
  headers: {
    'Content-Type': 'application/json',
  },
});

// đính kèm token xác thực jwt vào header nếu đã đăng nhập
axiosClient.interceptors.request.use(
  (config: InternalAxiosRequestConfig) => {
    const token = localStorage.getItem('access_token');
    if (token && config.headers) {
      config.headers.Authorization = `Bearer ${token}`;
    }
    return config;
  },
  (error) => Promise.reject(error)
);

// chuẩn hóa dữ liệu trả về và bắt lỗi từ backend
axiosClient.interceptors.response.use(
  (response) => {
    return response.data;
  },
  (error: AxiosError<ErrorResponse>) => {
    // bóc tách dữ liệu lỗi trả về từ GlobalExceptionHandler
    const errorData = error.response?.data;
    if (error.response?.status === 401) {
      // xóa token khi phiên đăng nhập hết hạn hoặc không hợp lệ
      localStorage.removeItem('access_token');
    }
    return Promise.reject(errorData || error);
  }
);

export default axiosClient;
