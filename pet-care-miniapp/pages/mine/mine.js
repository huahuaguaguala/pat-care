const { request } = require('../../utils/request');
Page({
  data: { user: null },
  onShow() {
    request('/api/user/me').then(res => {
      if (res.code === 200) this.setData({ user: res.data });
    }).catch(() => {});
  },
  goLogin() { wx.navigateTo({ url: '/pages/login/login' }); },
  goPet() { wx.navigateTo({ url: '/pages/pet/pet' }); },
  goSign() { wx.navigateTo({ url: '/pages/sign/sign' }); },
  logout() {
    wx.removeStorageSync('token');
    wx.reLaunch({ url: '/pages/index/index' });
  }
});
