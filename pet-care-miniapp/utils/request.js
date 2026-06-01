const app = getApp();
const BASE = app ? (app.globalData.baseUrl || 'http://localhost:8080') : 'http://localhost:8080';

const request = (url, method = 'GET', data = {}) => {
  const token = wx.getStorageSync('token') || '';
  return new Promise((resolve, reject) => {
    wx.request({
      url: BASE + url,
      method, data,
      header: {
        'Authorization': token ? 'Bearer ' + token : '',
        'Content-Type': 'application/json'
      },
      success(res) {
        if (res.statusCode === 200 && res.data.code === 200) resolve(res.data.data);
        else if (res.statusCode === 401) {
          wx.removeStorageSync('token');
          app.globalData.token = null;
          reject({ code: 401, msg: 'Please login' });
        } else reject(res.data || { msg: 'Request failed' });
      },
      fail(err) { reject(err); }
    });
  });
};

module.exports = {
  get: (url) => request(url, 'GET'),
  post: (url, data) => request(url, 'POST', data),
  put: (url, data) => request(url, 'PUT', data),
  del: (url) => request(url, 'DELETE')
};
