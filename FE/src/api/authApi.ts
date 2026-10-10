import axiosClient from '@/helpers/axiosClient';
import type {
  ApiResponse,
  AuthResponse,
  ProfileResponse,
  UserLoginRequest,
  UserRegisterRequest,
  PasswordForgotRequest,
  PasswordResetRequest,
} from '@/utils/apiTypes';

export const authApi = {
  // gửi yêu cầu đăng nhập
  login: (data: UserLoginRequest): Promise<ApiResponse<AuthResponse>> => {
    return axiosClient.post('/auth/login', data);
  },

  // gửi yêu cầu đăng ký tài khoản khách hàng
  register: (data: UserRegisterRequest): Promise<ApiResponse<ProfileResponse>> => {
    return axiosClient.post('/auth/register', data);
  },

  // gửi yêu cầu xác thực email theo mã token
  verifyEmail: (token: string): Promise<ApiResponse<null>> => {
    return axiosClient.post('/auth/verify-email', { token });
  },

  // gửi yêu cầu cấp lại mật khẩu qua email
  forgotPassword: (data: PasswordForgotRequest): Promise<ApiResponse<null>> => {
    return axiosClient.post('/auth/forgot-password', data);
  },

  // gửi yêu cầu thiết lập mật khẩu mới
  resetPassword: (data: PasswordResetRequest): Promise<ApiResponse<null>> => {
    return axiosClient.post('/auth/reset-password', data);
  },
};

export default authApi;

