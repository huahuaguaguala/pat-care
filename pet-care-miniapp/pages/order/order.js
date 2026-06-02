var http = require('../../utils/request');
var statusMap = ['Unpaid','Paid','In-progress','Done','Cancelled'];
Page({
  data: { orders: [], filterStatus: null, statusMap: statusMap },
  onShow: function() { this.loadOrders(); },
  loadOrders: function() {
    var self = this;
    http.get('/api/order/my').then(function(list) { self.setData({ orders: list || [] }); }).catch(function(){});
  },
  clearFilter: function() { this.setData({ filterStatus: null }); },
  setFilter: function(e) { this.setData({ filterStatus: e.currentTarget.dataset.index }); },
  payOrder: function(e) {
    var id = e.currentTarget.dataset.id, self = this;
    http.put('/api/order/' + id + '/pay', {}).then(function() { self.loadOrders(); }).catch(function(){ wx.showToast({title:'Failed',icon:'none'}); });
  },
  cancelOrder: function(e) {
    var id = e.currentTarget.dataset.id, self = this;
    wx.showModal({ title: 'Cancel', content: 'Reason?', editable: true, success: function(r) {
      if (r.confirm) http.put('/api/order/' + id + '/cancel?reason=' + (r.content || '')).then(function() { self.loadOrders(); });
    }});
  },
  reviewOrder: function(e) {
    var id = e.currentTarget.dataset.id, self = this;
    wx.showModal({ title: 'Rate', content: '1-5 stars', editable: true, placeholderText: '5 Great', success: function(r) {
      if (r.confirm) {
        var parts = r.content.split(' '), rating = parts[0] || '5', review = parts.slice(1).join(' ') || '';
        http.post('/api/order/' + id + '/review?rating=' + rating + '&review=' + review).then(function() { self.loadOrders(); });
      }
    }});
  }
});
