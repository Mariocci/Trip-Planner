import axios from 'axios';

/**
 * Configured Axios instance for API calls.
 * Base URL points to the backend API.
 */
export const api = axios.create({
  baseURL: 'http://localhost:8080/api',
  headers: {
    'Content-Type': 'application/json',
  },
});

/**
 * Setup Axios interceptor to add JWT token to all requests.
 * Call this function after Auth0 is initialized.
 */
export const setupAxiosInterceptor = (getAccessToken: () => Promise<string>) => {
  api.interceptors.request.use(
    async (config) => {
      try {
        const token = await getAccessToken();
        if (token) {
          config.headers.Authorization = `Bearer ${token}`;
        }
      } catch (error) {
        console.error('Error getting access token:', error);
      }
      return config;
    },
    (error) => {
      return Promise.reject(error);
    }
  );
};
