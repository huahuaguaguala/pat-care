
const { get, post } = require('../../utils/request');
Page({
  data: { categories: [], items: [], selectedCategory: null, selectedItems: [],
    petId: null, myPets: [], appointmentTime: '', remark: '', showBooking: false },
  onShow() {
    get('/api/service/category').then(list => {
      this.setData({ categories: list || [] });
      if (list && list.length > 0) this.loadItems(list[0].id);
    });
    get('/api/pet/my').then(pets => this.setData({ myPets: pets || [] })).catch(() => {});
  },
  loadItems(catId) {
    this.setData({ selectedCategory: catId });
    get('/api/service/item?categoryId=' + catId).then(list => this.setData({ items: list || [] }));
  },
  toggleItem(e) {
    const id = e.currentTarget.dataset.id;
    let sel = this.data.selectedItems;
    const idx = sel.indexOf(id);
    if (idx > -1) sel.splice(idx, 1); else sel.push(id);
    this.setData({ selectedItems: sel });
  },
  book() {
    if (this.data.selectedItems.length === 0) { wx.showToast({ title: 'Select services', icon: 'none' }); return; }
    this.setData({ showBooking: true });
  },
  submitOrder(e) {
    const d = e.detail.value;
    const items = this.data.selectedItems.map(sid => ({ serviceId: sid, quantity: 1 }));
    const body = { petId: parseInt(d.petId), items, appointmentTime: d.appointmentTime || null, remark: d.remark };
    post('/api/order', body).then(order => {
      wx.showToast({ title: 'Order created!', icon: 'success' });
      this.setData({ showBooking: false, selectedItems: [] });
    }).catch(err => wx.showToast({ title: (err && err.message) || 'Failed', icon: 'none' }));
  }
});
