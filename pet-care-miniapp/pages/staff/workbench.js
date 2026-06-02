var http = require('../../utils/request');
Page({
  data: { pendingOrders: [], myOrders: [], myActiveCount: 0, schedule: [], unreadCount: 0, activeTab: 'pending' },
  switchTab: function(e) { this.setData({ activeTab: e.currentTarget.dataset.tab }); },
  onShow: function() {
    var role = wx.getStorageSync('role'), self = this;
    if (!role || role != 1) {
      wx.showModal({ title: 'Staff Login', content: 'username password', editable: true, placeholderText: 'staff01 123456', success: function(res) {
        if (res.confirm) {
          var parts = res.content.split(' ');
          http.post('/api/auth/login', { username: parts[0], password: parts[1] }).then(function(d) {
            wx.setStorageSync('token', d.token); wx.setStorageSync('role', 1); self.loadAll();
          }).catch(function() { wx.showToast({ title: 'Login failed', icon: 'none' }); });
        }
      }});
    } else { this.loadAll(); }
  },
  loadAll: function() { this.loadPending(); this.loadMyOrders(); this.loadSchedule(); this.loadUnread(); },
  loadPending: function() { var s=this; http.get('/api/order/pending').then(function(o){ s.setData({pendingOrders:o||[]}); }).catch(function(){}); },
  loadMyOrders: function() {
    var s=this; http.get('/api/order/my').then(function(l){
      var a=(l||[]).filter(function(i){ var o=i.order||i; return o.status===2; });
      s.setData({myOrders:a, myActiveCount:a.length});
    }).catch(function(){});
  },
  loadSchedule: function() { var s=this; http.get('/api/schedule/staff').then(function(l){ s.setData({schedule:l||[]}); }).catch(function(){}); },
  loadUnread: function() { var s=this; http.get('/api/notification/unread').then(function(c){ s.setData({unreadCount:c||0}); }).catch(function(){}); },
  acceptOrder: function(e) { var id=e.currentTarget.dataset.id, s=this; http.put('/api/order/'+id+'/accept',{}).then(function(){ wx.showToast({title:'Accepted',icon:'success'}); s.loadAll(); }).catch(function(){ wx.showToast({title:'Failed',icon:'none'}); }); },
  rejectOrder: function(e) {
    var id=e.currentTarget.dataset.id, s=this;
    wx.showModal({title:'Reject Reason',editable:true,placeholderText:'Reason',success:function(res){
      if(res.confirm) http.put('/api/order/'+id+'/reject?reason='+(res.content||'')).then(function(){ wx.showToast({title:'Rejected',icon:'success'}); s.loadAll(); });
    }});
  },
  completeOrder: function(e) { var id=e.currentTarget.dataset.id, s=this; http.put('/api/order/'+id+'/complete',{}).then(function(){ wx.showToast({title:'Done',icon:'success'}); s.loadAll(); }).catch(function(){ wx.showToast({title:'Failed',icon:'none'}); }); },
  quickCheckin: function() {
    wx.showModal({title:'Check In',content:'petId cageNo',editable:true,placeholderText:'1 A01',success:function(res){
      if(res.confirm){ var p=res.content.split(' '); http.post('/api/boarding/checkin',{petId:parseInt(p[0]),cageNo:p[1]}).then(function(){ wx.showToast({title:'Checked In',icon:'success'}); }).catch(function(){ wx.showToast({title:'Failed',icon:'none'}); }); }
    }});
  },
  quickWeight: function() {
    wx.showModal({title:'Record Weight',content:'petId weight(kg)',editable:true,placeholderText:'1 12.5',success:function(res){
      if(res.confirm){ var p=res.content.split(' '); http.post('/api/weight',{petId:parseInt(p[0]),weight:parseFloat(p[1])}).then(function(){ wx.showToast({title:'Recorded',icon:'success'}); }).catch(function(){ wx.showToast({title:'Failed',icon:'none'}); }); }
    }});
  },
  quickVaccine: function() {
    wx.showModal({title:'Add Vaccine',content:'petId name nextDate',editable:true,placeholderText:'1 Rabies 2027-06-01',success:function(res){
      if(res.confirm){ var p=res.content.split(' '); http.post('/api/vaccine',{petId:parseInt(p[0]),vaccineName:p[1],doseNumber:1,nextDueDate:p[2]}).then(function(){ wx.showToast({title:'Recorded',icon:'success'}); }).catch(function(){ wx.showToast({title:'Failed',icon:'none'}); }); }
    }});
  },
  logout: function() { wx.removeStorageSync('token'); wx.removeStorageSync('role'); wx.switchTab({ url: '/pages/mine/mine' }); }
});
