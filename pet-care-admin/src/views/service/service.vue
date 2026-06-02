<template>
  <div style="padding:24px;">
    <h2>服务配置</h2>
    <el-card style="margin:12px 0;"><h3>品类</h3>
      <el-tag v-for="c in categories" :key="c.id" style="margin:4px;" type="success">{{ c.name }}</el-tag>
    </el-card>
    <el-table :data="items" border>
      <el-table-column prop="id" label="ID" width="50" />
      <el-table-column prop="name" label="名称" />
      <el-table-column prop="price" label="价格" />
      <el-table-column prop="duration" label="时长(分钟)" />
      <el-table-column prop="maxPerSlot" label="容量/时段" />
      <el-table-column label="状态">
        <template #default="scope"><el-switch :model-value="scope.row.status===1" @change="toggleItem(scope.row)" /></template>
      </el-table-column>
      <el-table-column label="操作" width="200">
        <template #default="scope">
          <el-button size="small" @click="editItem(scope.row)">编辑</el-button>
          <el-button size="small" type="danger" @click="deleteItem(scope.row.id)">删除</el-button>
        </template>
      </el-table-column>
    </el-table>
    <el-dialog v-model="showForm" :title="editId?'编辑':'新增'">
      <el-form :model="form" label-width="100px">
        <el-form-item label="名称"><el-input v-model="form.name" /></el-form-item>
        <el-form-item label="品类ID"><el-input-number v-model="form.categoryId" /></el-form-item>
        <el-form-item label="价格"><el-input-number v-model="form.price" :precision="2" /></el-form-item>
        <el-form-item label="时长(分钟)"><el-input-number v-model="form.duration" /></el-form-item>
        <el-form-item label="容量/时段"><el-input-number v-model="form.maxPerSlot" /></el-form-item>
        <el-form-item label="开始日期"><el-date-picker v-model="form.startDate" type="date" /></el-form-item>
        <el-form-item label="结束日期"><el-date-picker v-model="form.endDate" type="date" /></el-form-item>
      </el-form>
      <template #footer><el-button @click="showForm=false">取消</el-button><el-button type="primary" @click="saveItem">保存</el-button></template>
    </el-dialog>
  </div>
</template>
<script setup>
import { ref, onMounted } from 'vue';
import api from '../../api/index.js';
import { ElMessage, ElMessageBox } from 'element-plus';
const categories = ref([]), items = ref([]), showForm = ref(false), editId = ref(null);
const form = ref({ name:'', categoryId:1, price:0, duration:30, maxPerSlot:3, startDate:null, endDate:null });
onMounted(() => { api.get('/service/category/all').then(r => categories.value = r||[]); loadItems(); });
function loadItems() { api.get('/service/item/all').then(r => items.value = r||[]); }
function toggleItem(item) { api.put('/service/item/'+item.id+'/toggle').then(() => loadItems()); }
function editItem(item) { editId.value = item.id; Object.assign(form.value, item); showForm.value = true; }
function saveItem() {
  const p = editId.value ? api.put('/service/item/'+editId.value, form.value) : api.post('/service/item', form.value);
  p.then(() => { showForm.value=false; editId.value=null; loadItems(); ElMessage.success('保存成功'); });
}
function deleteItem(id) {
  ElMessageBox.confirm('确认删除此服务？').then(() => api.del('/service/item/'+id).then(() => loadItems()));
}
</script>
