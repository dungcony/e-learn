import axiosClient from '@/helpers/axiosClient';
import type {
  ApiResponse,
  PageResponse,
  TableResponse,
  TableCreateRequest,
  TableUpdateRequest,
  TableSearchRequest,
} from '@/utils/apiTypes';

export const tableApi = {
  // tìm kiếm và lấy danh sách phân trang bàn ăn
  searchTables: (
    params: TableSearchRequest & { page?: number; page_size?: number; sort_by?: string; sort_order?: string }
  ): Promise<ApiResponse<PageResponse<TableResponse>>> => {
    return axiosClient.get('/tables', { params });
  },

  // lấy chi tiết một bàn ăn
  getTable: (id: string): Promise<ApiResponse<TableResponse>> => {
    return axiosClient.get(`/tables/${id}`);
  },

  // tạo mới bàn ăn
  createTable: (data: TableCreateRequest): Promise<ApiResponse<TableResponse>> => {
    return axiosClient.post('/tables', data);
  },

  // cập nhật thông tin bàn ăn
  updateTable: (id: string, data: TableUpdateRequest): Promise<ApiResponse<TableResponse>> => {
    return axiosClient.put(`/tables/${id}`, data);
  },

  // xóa bàn ăn
  deleteTable: (id: string): Promise<ApiResponse<void>> => {
    return axiosClient.delete(`/tables/${id}`);
  },
};

export default tableApi;

