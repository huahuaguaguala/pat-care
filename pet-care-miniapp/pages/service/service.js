var http = require('../../utils/request');
Page({
  data: { categories: [], items: [], selectedCategory: null, selectedItems: [], myPets: [], showBooking: false },
  onShow: function() {
    var self = this;
    http.get('/api/service/category').then(function(list) { var cats = list || []; self.setData({ categories: cats }); if (cats.length>0) self.loadItems(cats[0].id); });
    http.get('/api/pet/my').then(function(pets) { self.setData({ myPets: pets || [] }); }).catch(function(){});
  },
  loadItems: function(catId) {
    var self = this; this.setData({ selectedCategory: catId });
    http.get('/api/service/item?categoryId=' + catId).then(function(list) { self.setData({ items: list || [] }); });
  },
  toggleItem: function(e) {
    var id = e.currentTarget.dataset.id, sel = this.data.selectedItems.slice(), idx = sel.indexOf(id);
    if (idx > -1) sel.splice(idx, 1); else sel.push(id);
    this.setData({ selectedItems: sel });
  },
  hideBooking: function() { this.setData({ showBooking: false }); },
  book: function() {
    if (this.data.selectedItems.length === 0) { wx.showToast({ title: 'Select services', icon: 'none' }); return; }
    this.setData({ showBooking: true });
  },
  submitOrder: function(e) {
    var d = e.detail.value, self = this;
    var items = this.data.selectedItems.map(function(sid) { return { serviceId: sid, quantity: 1 }; });
    var body = { petId: parseInt(d.petId), items: items, appointmentTime: d.appointmentTime || null, remark: d.remark };
    http.post('/api/order', body).then(function() {
      wx.showToast({ title: 'Order created', icon: 'success' }); self.setData({ showBooking: false, selectedItems: [] });
    }).catch(function() { wx.showToast({ title: 'Failed', icon: 'none' }); });
  }
});
