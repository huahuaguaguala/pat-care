const { get, post } = require('../../utils/request');
Page({
  data: {
    weeklyRank: [], hotServices: [], breeds: [],
    userInfo: null, loading: true
  },
  onShow() {
    this.loadData();
    this.checkLogin();
  },
  loadData() {
    Promise.all([
      get('/api/rank/pet/weekly').catch(() => []),
      get('/api/rank/service/hot').catch(() => []),
      get('/api/breed').catch(() => [])
    ]).then(([rank, services, breeds]) => {
      this.setData({
        weeklyRank: rank || [],
        hotServices: (services || []).slice(0, 5),
        breeds: (breeds || []).slice(0, 6),
        loading: false
      });
    });
  },
  checkLogin() {
    const token = wx.getStorageSync('token');
    if (token) {
      get('/api/user/me').then(u => this.setData({ userInfo: u })).catch(() => {});
    }
  },
  goService() { wx.switchTab({ url: '/pages/service/service' }); },
  goLogin() {
    wx.showModal({ title: 'Login', content: 'Enter openid for test login', editable: true,
      placeholderText: 'mock_openid_001',
      success: res => {
        if (res.confirm) {
          post('/api/auth/wechat-login', { openid: res.content }).then(data => {
            wx.setStorageSync('token', data.token);
            getApp().globalData.token = data.token;
            this.setData({ userInfo: data.user });
            this.loadData();
          });
        }
      }
    });
  }
});
