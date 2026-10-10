import axiosClient from '@/helpers/axiosClient';
import type { ApiResponse, PageResponse } from '@/utils/apiTypes';

export interface MenuCategoryResponse {
  category: string;
  dish_count: number;
}

export interface DishSummaryResponse {
  id: string;
  name: string;
  category: string;
  price: number;
  unit: string;
  image_url: string;
  status: 'AVAILABLE' | 'OUT_OF_STOCK';
}

export const menuApi = {
  getCategories: (): Promise<ApiResponse<MenuCategoryResponse[]>> => 
    axiosClient.get('/menu/categories'),
  
  getDishes: (params?: { category?: string; page?: number; size?: number; keyword?: string }): Promise<ApiResponse<PageResponse<DishSummaryResponse>>> => 
    axiosClient.get('/menu/dishes', { params }),
  
  getDish: (id: string): Promise<ApiResponse<DishSummaryResponse>> => 
    axiosClient.get(`/menu/dishes/${id}`),
};
