const app = getApp();
const request = (url, method = 'GET', data = {}) => {
  return new Promise((resolve, reject) => {
    wx.request({
      url: app.globalData.baseUrl + url,
      method,
      data,
      header: {
        'Authorization': 'Bearer ' + (app.globalData.token || ''),
        'Content-Type': 'application/json'
      },
      success(res) {
        if (res.statusCode === 200) { resolve(res.data); }
        else if (res.statusCode === 401) {
          wx.removeStorageSync('token');
          wx.navigateTo({ url: '/pages/login/login' });
          reject(res.data);
        } else { reject(res.data); }
      },
      fail(err) { reject(err); }
    });
  });
};
module.exports = { request };
