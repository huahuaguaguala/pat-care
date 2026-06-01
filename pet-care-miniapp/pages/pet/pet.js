
const { get, post, put, del } = require('../../utils/request');
Page({
  data: { pets: [], breedOptions: [], showForm: false, editPet: null },
  onShow() { this.loadPets(); this.loadBreeds(); },
  loadPets() {
    get('/api/pet/my').then(pets => this.setData({ pets: pets || [] })).catch(() => {});
  },
  loadBreeds() {
    get('/api/breed').then(list => this.setData({ breedOptions: list || [] })).catch(() => {});
  },
  showAdd() { this.setData({ showForm: true, editPet: null }); },
  hideForm() { this.setData({ showForm: false }); },
  editPet(e) {
    const pet = this.data.pets.find(p => p.id === e.currentTarget.dataset.id);
    this.setData({ showForm: true, editPet: pet });
  },
  savePet(e) {
    const d = e.detail.value;
    const data = { name: d.name, breedId: parseInt(d.breedId), breedName: d.breedName,
      birthday: d.birthday, gender: parseInt(d.gender), weight: parseFloat(d.weight),
      personality: d.personality, notes: d.notes, isNeutered: parseInt(d.isNeutered || 0) };
    const action = this.data.editPet
      ? put('/api/pet/' + this.data.editPet.id, data)
      : post('/api/pet', data);
    action.then(() => { this.hideForm(); this.loadPets(); }).catch(() => wx.showToast({ title: 'Error', icon: 'none' }));
  },
  deletePet(e) {
    wx.showModal({ title: 'Confirm', content: 'Delete this pet?', success: r => {
      if (r.confirm) del('/api/pet/' + e.currentTarget.dataset.id).then(() => this.loadPets());
    }});
  }
});
