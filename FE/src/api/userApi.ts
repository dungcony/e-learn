import axiosClient from '@/helpers/axiosClient';
import type {
  ApiResponse,
  ProfileResponse,
  ProfileUpdateRequest,
  PasswordChangeRequest,
} from '@/utils/apiTypes';

export const userApi = {
  // lấy thông tin hồ sơ của người dùng hiện tại
  getProfile: (): Promise<ApiResponse<ProfileResponse>> => {
    return axiosClient.get('/users/me');
  },

  // cập nhật thông tin hồ sơ cá nhân
  updateProfile: (data: ProfileUpdateRequest): Promise<ApiResponse<ProfileResponse>> => {
    return axiosClient.put('/users/me', data);
  },

  // cập nhật ảnh đại diện người dùng
  updateAvatar: (file: File): Promise<ApiResponse<ProfileResponse>> => {
    const formData = new FormData();
    formData.append('file', file);
    return axiosClient.put('/users/me/avatar', formData, {
      headers: {
        'Content-Type': 'multipart/form-data',
      },
    });
  },

  // đổi mật khẩu tài khoản
  changePassword: (data: PasswordChangeRequest): Promise<ApiResponse<null>> => {
    return axiosClient.put('/users/me/password', data);
  },
};

export default userApi;

