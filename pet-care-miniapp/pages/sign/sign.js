const { request } = require('../../utils/request');
Page({
  data: { signStatus: null, today: 0, todaySigned: false, signCount: 0, consecutiveDays: 0 },
  onShow() { this.loadStatus(); },
  loadStatus() {
    request('/api/sign/status').then(res => {
      if (res.code === 200) this.setData(res.data);
    }).catch(() => {});
  },
  doSign() {
    request('/api/sign/do', 'POST').then(res => {
      wx.showToast({ title: res.data.msg || '签到成功', icon: 'none' });
      this.loadStatus();
    }).catch(e => wx.showToast({ title: e.message || '签到失败', icon: 'none' }));
  }
});
