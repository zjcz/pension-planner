import axios from 'axios';

let onSessionExpired: (() => void) | null = null;

export function setSessionExpiredHandler(handler: (() => void) | null) {
  onSessionExpired = handler;
}

export const http = axios.create({
  baseURL: '/api/v1',
  withCredentials: true,
});

const NON_EXPIRY_401_PATHS = ['/auth/login', '/auth/register', '/auth/me'];

http.interceptors.response.use(
  (response) => response,
  (error) => {
    if (axios.isAxiosError(error) && error.response?.status === 401) {
      const path = error.config?.url ?? '';
      if (!NON_EXPIRY_401_PATHS.some((p) => path.includes(p)) && onSessionExpired) {
        onSessionExpired();
      }
    }
    return Promise.reject(error);
  },
);

export function apiErrorMessage(error: unknown): string {
  if (axios.isAxiosError(error)) {
    const data = error.response?.data as { message?: string } | undefined;
    if (data?.message) {
      return data.message;
    }
    if (error.response?.status === 401) {
      const path = error.config?.url ?? '';
      if (NON_EXPIRY_401_PATHS.some((p) => path.includes(p))) {
        return 'Invalid username or password';
      }
      return 'Your session has expired. Please sign in again.';
    }
    if (error.response?.status === 403) {
      return 'You do not have permission to perform this action';
    }
    return error.message;
  }
  return 'An unexpected error occurred';
}
