import { http } from './client';
import type { AuthConfig, User } from '../types';

export const authApi = {
  register: (username: string, password: string) =>
    http.post<User>('/auth/register', { username, password }).then((response) => response.data),

  login: (username: string, password: string) =>
    http.post<User>('/auth/login', { username, password }).then((response) => response.data),

  logout: () => http.post<void>('/auth/logout'),

  me: () => http.get<User>('/auth/me').then((response) => response.data),

  config: () => http.get<AuthConfig>('/auth/config').then((response) => response.data),
};
