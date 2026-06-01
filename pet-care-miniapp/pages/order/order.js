
const { get, put, post } = require('../../utils/request');
const statusMap = ['Unpaid', 'Paid', 'In-progress', 'Done', 'Cancelled'];
Page({
  data: { orders: [], filterStatus: null, statusMap },
  onShow() { this.loadOrders(); },
  loadOrders() { get('/api/order/my').then(list => this.setData({ orders: list || [] })).catch(() => {}); },
  payOrder(e) {
    const id = e.currentTarget.dataset.id;
    put('/api/order/' + id + '/pay', {}).then(() => this.loadOrders()).catch(() => wx.showToast({ title: 'Failed', icon: 'none' }));
  },
  cancelOrder(e) {
    const id = e.currentTarget.dataset.id;
    wx.showModal({ title: 'Cancel', content: 'Reason?', editable: true, success: r => {
      if (r.confirm) put('/api/order/' + id + '/cancel?reason=' + (r.content||'')).then(() => this.loadOrders());
    }});
  },
  reviewOrder(e) {
    const id = e.currentTarget.dataset.id;
    wx.showModal({ title: 'Rate', content: '1-5 stars + comment', editable: true, placeholderText: '5 Great service!',
      success: r => {
        if (r.confirm) {
          const parts = r.content.split(' ');
          post('/api/order/' + id + '/review?rating=' + (parts[0]||'5') + '&review=' + (parts.slice(1).join(' ')||'')).then(() => this.loadOrders());
        }
      }
    }});
  }
});
