App({
  globalData: {
    token: null,
    userInfo: null,
    role: null,
    baseUrl: 'http://localhost:8080'
  },
  onLaunch() {
    const token = wx.getStorageSync('token');
    const role = wx.getStorageSync('role');
    if (token) {
      this.globalData.token = token;
      this.globalData.role = role;
    }
  }
});
