
const { get } = require('../../utils/request');
Page({
  data: { user: null, roleText: {0:'Pet Owner',1:'Staff',2:'Admin'} },
  onShow() {
    get('/api/user/me').then(u => this.setData({ user: u })).catch(() => this.setData({ user: null }));
  },
  goLogin() {
    wx.showModal({ title: 'Login', content: 'OpenID for test:', editable: true, placeholderText: 'mock_openid_001',
      success: r => {
        if (r.confirm) {
          const { post } = require('../../utils/request');
          post('/api/auth/wechat-login', { openid: r.content }).then(data => {
            wx.setStorageSync('token', data.token);
            getApp().globalData.token = data.token;
            this.onShow();
          });
        }
      }
    });
  },
  staffLogin() {
    wx.showModal({ title: 'Staff Login', content: 'username password', editable: true, placeholderText: 'staff01 123456',
      success: r => {
        if (r.confirm) {
          const parts = r.content.split(' ');
          const { post } = require('../../utils/request');
          post('/api/auth/login', { username: parts[0], password: parts[1] }).then(data => {
            wx.setStorageSync('token', data.token);
            getApp().globalData.token = data.token;
            this.onShow();
          }).catch(() => wx.showToast({ title: 'Login failed', icon: 'none' }));
        }
      }
    });
  },
  logout() {
    wx.removeStorageSync('token');
    getApp().globalData.token = null;
    this.setData({ user: null });
  }
});
