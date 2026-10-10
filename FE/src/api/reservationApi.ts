import axiosClient from '@/helpers/axiosClient';
import type { ApiResponse } from '@/utils/apiTypes';

export interface AvailabilityRequest {
  reserved_at: string; // ISO 8601, e.g. 2026-10-15T19:00:00Z
  guest_count: number;
}

export interface AvailabilityResponse {
  available: boolean;
  suggestions: string[]; // ISO 8601 strings
}

export interface ReservationCreateRequest {
  guest_name: string;
  phone: string;
  email?: string;
  reserved_at: string;
  guest_count: number;
  preferred_zone?: string;
  note?: string;
}

export interface ReservationResponse {
  id: string;
  code: string;
  status: string;
  reserved_at: string;
  guest_count: number;
  // ... other fields if needed
}

export const reservationApi = {
  checkAvailability: (params: AvailabilityRequest): Promise<ApiResponse<AvailabilityResponse>> => 
    axiosClient.get('/reservations/availability', { params }),
    
  createReservation: (data: ReservationCreateRequest, idempotencyKey?: string): Promise<ApiResponse<ReservationResponse>> => 
    axiosClient.post('/reservations', data, {
      headers: idempotencyKey ? { 'Idempotency-Key': idempotencyKey } : undefined
    }),
};
