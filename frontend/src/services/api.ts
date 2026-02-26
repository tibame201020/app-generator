import axios from 'axios';

const api = axios.create({
  baseURL: '/api',
});

api.interceptors.response.use(
  (response) => response,
  (error) => {
    const requestId = error.response?.headers['x-request-id'] || error.response?.data?.requestId;
    if (requestId) {
      console.error(`[API Error] Request ID: ${requestId}`, error);
    }
    return Promise.reject(error);
  }
);

export default api;
