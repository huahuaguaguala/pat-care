var http = require('../../utils/request');
Page({
  data: { user: null, roleText: {0:'Pet Owner',1:'Staff',2:'Admin'} },
  onShow: function() {
    var role = wx.getStorageSync('role'), token = wx.getStorageSync('token'), self = this;
    if (role == 1 && token) { wx.navigateTo({ url: '/pages/staff/workbench' }); return; }
    if (token) {
      http.get('/api/user/me').then(function(u) { self.setData({ user: u }); }).catch(function() {
        self.setData({ user: null }); wx.removeStorageSync('token'); wx.removeStorageSync('role');
      });
    }
  },
  goToLogin: function() { wx.navigateTo({ url: '/pages/login/login' }); },
  goToPet: function() { wx.navigateTo({ url: '/pages/pet/pet' }); },
  goToOrders: function() { wx.switchTab({ url: '/pages/order/order' }); },
  goToSign: function() { wx.navigateTo({ url: '/pages/sign/sign' }); },
  goToWorkbench: function() { wx.navigateTo({ url: '/pages/staff/workbench' }); },
  logout: function() { wx.removeStorageSync('token'); wx.removeStorageSync('role'); this.setData({ user: null }); }
});
