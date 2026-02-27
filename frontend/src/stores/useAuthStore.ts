import { create } from 'zustand';
import { persist } from 'zustand/middleware';
import api from '../services/api';

export enum ProjectRole {
  ADMIN = 'ADMIN',
  MEMBER = 'MEMBER',
  VIEWER = 'VIEWER',
}

interface User {
  id: string;
  username: string;
  email: string;
  planType: string;
}

interface AuthState {
  user: User | null;
  accessToken: string | null;
  refreshToken: string | null;

  login: (email: string, password: string) => Promise<void>;
  register: (username: string, email: string, password: string) => Promise<void>;
  logout: () => Promise<void>;

  setTokens: (accessToken: string, refreshToken: string) => void;
  fetchMe: () => Promise<void>;
}

export const useAuthStore = create<AuthState>()(
  persist(
    (set, get) => ({
      user: null,
      accessToken: null,
      refreshToken: null,

      login: async (email, password) => {
        const response = await api.post('/auth/login', { email, password });
        const { accessToken, refreshToken } = response.data;
        set({ accessToken, refreshToken });
        await get().fetchMe();
      },

      register: async (username, email, password) => {
        const response = await api.post('/auth/register', { username, email, password });
        const { accessToken, refreshToken } = response.data;
        set({ accessToken, refreshToken });
        await get().fetchMe();
      },

      logout: async () => {
        const { refreshToken } = get();
        if (refreshToken) {
          try {
            await api.post('/auth/logout', { refreshToken });
          } catch (e) {
            // Ignore error on logout
          }
        }
        set({ user: null, accessToken: null, refreshToken: null });
      },

      setTokens: (accessToken, refreshToken) => {
          set({ accessToken, refreshToken });
      },

      fetchMe: async () => {
        try {
          const response = await api.get('/auth/me');
          set({ user: response.data });
        } catch (e) {
          set({ user: null, accessToken: null, refreshToken: null });
        }
      },
    }),
    {
      name: 'auth-storage',
      partialize: (state) => ({ accessToken: state.accessToken, refreshToken: state.refreshToken }),
    }
  )
);
