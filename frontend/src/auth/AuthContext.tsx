import { createContext, useCallback, useContext, useMemo, type ReactNode } from 'react';
import { useQuery, useQueryClient } from '@tanstack/react-query';
import { authApi } from '../api/auth';
import type { User } from '../types';

interface AuthContextValue {
  user: User | null;
  isLoading: boolean;
  login: (username: string, password: string) => Promise<void>;
  register: (username: string, password: string) => Promise<void>;
  logout: () => Promise<void>;
  markSessionExpired: () => void;
}

const AuthContext = createContext<AuthContextValue | null>(null);

export function AuthProvider({ children }: { children: ReactNode }) {
  const queryClient = useQueryClient();

  const {
    data: user,
    isLoading,
    isError,
  } = useQuery({
    queryKey: ['auth', 'me'],
    queryFn: () => authApi.me(),
    retry: false,
    staleTime: 5 * 60 * 1000,
  });

  const login = useCallback(
    async (username: string, password: string) => {
      const loggedIn = await authApi.login(username, password);
      queryClient.setQueryData(['auth', 'me'], loggedIn);
    },
    [queryClient],
  );

  const register = useCallback(
    async (username: string, password: string) => {
      const registered = await authApi.register(username, password);
      queryClient.setQueryData(['auth', 'me'], registered);
    },
    [queryClient],
  );

  const logout = useCallback(async () => {
    await authApi.logout();
    queryClient.setQueryData(['auth', 'me'], null);
    queryClient.clear();
  }, [queryClient]);

  const markSessionExpired = useCallback(() => {
    queryClient.setQueryData(['auth', 'me'], null);
    queryClient.clear();
  }, [queryClient]);

  const value = useMemo<AuthContextValue>(
    () => ({
      user: isError ? null : (user ?? null),
      isLoading,
      login,
      register,
      logout,
      markSessionExpired,
    }),
    [user, isError, isLoading, login, register, logout, markSessionExpired],
  );

  return <AuthContext.Provider value={value}>{children}</AuthContext.Provider>;
}

export function useAuth(): AuthContextValue {
  const context = useContext(AuthContext);
  if (!context) {
    throw new Error('useAuth must be used within an AuthProvider');
  }
  return context;
}
