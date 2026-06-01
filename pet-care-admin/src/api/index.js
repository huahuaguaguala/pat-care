
import axios from 'axios';
const api = axios.create({ baseURL: '/api' });
api.interceptors.request.use(config => {
  const token = localStorage.getItem('token');
  if (token) config.headers.Authorization = 'Bearer ' + token;
  return config;
});
api.interceptors.response.use(
  res => res.data?.data !== undefined ? res.data.data : res.data,
  err => {
    if (err.response?.status === 401) {
      localStorage.removeItem('token');
      if (confirm('Login required. Staff login?')) {
        const u = prompt('Username:','staff01');
        const p = prompt('Password:','123456');
        if (u && p) {
          api.post('/auth/login', { username: u, password: p }).then(r => {
            localStorage.setItem('token', r.token);
            window.location.reload();
          });
        }
      }
    }
    return Promise.reject(err);
  }
);
export default {
  get: (url) => api.get(url),
  post: (url, data) => api.post(url, data),
  put: (url, data) => api.put(url, data),
  del: (url) => api.delete(url)
};
