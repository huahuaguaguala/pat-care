
<template>
  <div style="padding:24px;">
    <h2>Service Configuration</h2>
    <el-card style="margin:12px 0;">
      <h3>Categories</h3>
      <el-tag v-for="c in categories" :key="c.id" style="margin:4px;" type="success">{{ c.name }}</el-tag>
      <el-button size="small" @click="showCat=true" style="margin-left:8px;">+ Add</el-button>
    </el-card>
    <el-table :data="items" border>
      <el-table-column prop="id" label="ID" width="50" />
      <el-table-column prop="name" label="Name" />
      <el-table-column prop="price" label="Price" />
      <el-table-column prop="duration" label="Duration" />
      <el-table-column prop="maxPerSlot" label="Max/Slot" />
      <el-table-column label="Status">
        <template #default="scope">
          <el-switch :model-value="scope.row.status===1" @change="toggleItem(scope.row)" />
        </template>
      </el-table-column>
      <el-table-column label="Action" width="200">
        <template #default="scope">
          <el-button size="small" @click="editItem(scope.row)">Edit</el-button>
          <el-button size="small" type="danger" @click="deleteItem(scope.row.id)">Delete</el-button>
        </template>
      </el-table-column>
    </el-table>

    <el-dialog v-model="showForm" :title="editId?'Edit':'Add'">
      <el-form :model="form" label-width="100px">
        <el-form-item label="Name"><el-input v-model="form.name" /></el-form-item>
        <el-form-item label="Category ID"><el-input-number v-model="form.categoryId" /></el-form-item>
        <el-form-item label="Price"><el-input-number v-model="form.price" :precision="2" /></el-form-item>
        <el-form-item label="Duration (min)"><el-input-number v-model="form.duration" /></el-form-item>
        <el-form-item label="Max/Slot"><el-input-number v-model="form.maxPerSlot" /></el-form-item>
        <el-form-item label="Start Date"><el-date-picker v-model="form.startDate" type="date" /></el-form-item>
        <el-form-item label="End Date"><el-date-picker v-model="form.endDate" type="date" /></el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="showForm=false">Cancel</el-button>
        <el-button type="primary" @click="saveItem">Save</el-button>
      </template>
    </el-dialog>
  </div>
</template>
<script setup>
import { ref, onMounted } from 'vue';
import api from '../../api/index.js';
import { ElMessage, ElMessageBox } from 'element-plus';
const categories = ref([]), items = ref([]), showForm = ref(false), showCat = ref(false), editId = ref(null);
const form = ref({ name:'', categoryId:1, price:0, duration:30, maxPerSlot:3, startDate:null, endDate:null });
onMounted(() => { api.get('/service/category/all').then(r => categories.value = r||[]); loadItems(); });
function loadItems() { api.get('/service/item/all').then(r => items.value = r||[]); }
function toggleItem(item) { api.put('/service/item/'+item.id+'/toggle').then(() => loadItems()); }
function editItem(item) { editId.value = item.id; Object.assign(form.value, item); showForm.value = true; }
function saveItem() {
  const p = editId.value ? api.put('/service/item/'+editId.value, form.value) : api.post('/service/item', form.value);
  p.then(() => { showForm.value=false; editId.value=null; loadItems(); ElMessage.success('Saved'); });
}
function deleteItem(id) {
  ElMessageBox.confirm('Delete this service?').then(() => api.del('/service/item/'+id).then(() => loadItems()));
}
</script>
