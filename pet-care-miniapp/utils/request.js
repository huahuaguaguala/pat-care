const getBaseUrl = () => {
  const app = getApp();
  const baseUrl = app && app.globalData && app.globalData.baseUrl
    ? app.globalData.baseUrl
    : 'http://localhost:8080';
  return baseUrl.replace(/\/$/, '');
};

const request = (url, method = 'GET', data = {}) => {
  const token = wx.getStorageSync('token') || '';
  const requestUrl = getBaseUrl() + url;
  return new Promise((resolve, reject) => {
    wx.request({
      url: requestUrl,
      method, data,
      header: {
        'Authorization': token ? 'Bearer ' + token : '',
        'Content-Type': 'application/json'
      },
      success(res) {
        const body = res.data || {};
        if (res.statusCode === 200 && body.code === 200) resolve(body.data);
        else if (res.statusCode === 401 || body.code === 401) {
          const app = getApp();
          wx.removeStorageSync('token');
          wx.removeStorageSync('role');
          if (app && app.globalData) {
            app.globalData.token = null;
            app.globalData.role = null;
          }
          reject({ code: 401, msg: '请先登录' });
        } else reject({ code: body.code || res.statusCode, msg: body.message || body.msg || '请求失败' });
      },
      fail(err) {
        console.error('[request fail]', method, requestUrl, err);
        reject({
          code: -1,
          msg: err.errMsg || err.message || 'Network error',
          errMsg: err.errMsg
        });
      }
    });
  });
};

module.exports = {
  get: (url) => request(url, 'GET'),
  post: (url, data) => request(url, 'POST', data),
  put: (url, data) => request(url, 'PUT', data),
  del: (url) => request(url, 'DELETE')
};
