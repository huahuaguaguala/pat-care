var http = require('../../utils/request');

Page({
  data: { mode: 'owner', openid: '', username: '', password: '' },
  switchMode: function(e) { this.setData({ mode: e.currentTarget.dataset.mode }); },
  onOpenidInput: function(e) { this.setData({ openid: e.detail.value }); },
  onUsernameInput: function(e) { this.setData({ username: e.detail.value }); },
  onPasswordInput: function(e) { this.setData({ password: e.detail.value }); },

  doOwnerLogin: function() {
    var self = this;
    var openid = this.data.openid || 'mock_openid_001';
    wx.showLoading({ title: 'Logging in...' });
    http.post('/api/auth/wechat-login', { openid: openid }).then(function(d) {
      wx.hideLoading();
      wx.setStorageSync('token', d.token);
      wx.setStorageSync('role', 0);
      wx.showToast({ title: 'Login success', icon: 'success' });
      setTimeout(function() { wx.switchTab({ url: '/pages/index/index' }); }, 600);
    }).catch(function(err) {
      wx.hideLoading();
      wx.showToast({ title: 'Login failed: ' + (err.msg || err.errMsg || 'Network error'), icon: 'none' });
    });
  },

  doStaffLogin: function() {
    var self = this;
    var username = this.data.username;
    var password = this.data.password;
    if (!username || !password) {
      wx.showToast({ title: 'Please enter username and password', icon: 'none' });
      return;
    }
    wx.showLoading({ title: 'Logging in...' });
    http.post('/api/auth/login', { username: username, password: password }).then(function(d) {
      wx.hideLoading();
      wx.setStorageSync('token', d.token);
      wx.setStorageSync('role', (d.user && d.user.role) || 1);
      wx.showToast({ title: 'Login success', icon: 'success' });
      if (d.user && d.user.role === 1) {
        setTimeout(function() { wx.redirectTo({ url: '/pages/staff/workbench' }); }, 600);
      } else {
        setTimeout(function() { wx.switchTab({ url: '/pages/index/index' }); }, 600);
      }
    }).catch(function(err) {
      wx.hideLoading();
      wx.showToast({ title: 'Login failed: ' + (err.msg || err.errMsg || 'Network error'), icon: 'none' });
    });
  }
});
