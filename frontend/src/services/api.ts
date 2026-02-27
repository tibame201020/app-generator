import axios from 'axios';
import { useAuthStore } from '../stores/useAuthStore';

const api = axios.create({
  baseURL: '/api',
});

api.interceptors.request.use((config) => {
  const { accessToken } = useAuthStore.getState();
  if (accessToken) {
    config.headers.Authorization = `Bearer ${accessToken}`;
  }
  return config;
}, (error) => {
  return Promise.reject(error);
});

api.interceptors.response.use((response) => {
  return response;
}, async (error) => {
  const originalRequest = error.config;

  if (error.response?.status === 401 && !originalRequest._retry) {
    originalRequest._retry = true;

    try {
      const { refreshToken, setTokens } = useAuthStore.getState();

      if (!refreshToken) {
        throw new Error('No refresh token available');
      }

      const response = await axios.post('/api/auth/refresh', { refreshToken });

      const { accessToken, refreshToken: newRefreshToken } = response.data;
      setTokens(accessToken, newRefreshToken);

      originalRequest.headers.Authorization = `Bearer ${accessToken}`;
      return api(originalRequest);
    } catch (refreshError) {
      useAuthStore.getState().logout();
      return Promise.reject(refreshError);
    }
  }

  return Promise.reject(error);
});

export default api;
