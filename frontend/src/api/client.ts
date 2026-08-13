import axios from 'axios';

export const http = axios.create({
  baseURL: '/api/v1',
  withCredentials: true,
});

export function apiErrorMessage(error: unknown): string {
  if (axios.isAxiosError(error)) {
    const data = error.response?.data as { message?: string } | undefined;
    if (data?.message) {
      return data.message;
    }
    if (error.response?.status === 401) {
      return 'Invalid username or password';
    }
    if (error.response?.status === 403) {
      return 'You do not have permission to perform this action';
    }
    return error.message;
  }
  return 'An unexpected error occurred';
}
