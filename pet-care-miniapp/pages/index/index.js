const { request } = require('../../utils/request');
Page({
  data: { pets: [], services: [], weeklyRank: [] },
  onShow() {
    this.loadRank();
    this.loadServices();
  },
  loadRank() {
    request('/api/rank/pet/weekly').then(res => {
      if (res.code === 200) this.setData({ weeklyRank: res.data || [] });
    });
  },
  loadServices() {
    request('/api/service/item').then(res => {
      if (res.code === 200) this.setData({ services: (res.data || []).slice(0, 5) });
    });
  }
});
