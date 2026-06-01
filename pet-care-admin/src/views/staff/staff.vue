
<template>
  <div style="padding:24px;">
    <h2>Staff Management</h2>
    <el-button type="primary" @click="showAdd=true" style="margin:12px 0;">+ Add Staff</el-button>
    <el-table :data="staff" border>
      <el-table-column prop="id" label="ID" width="60" />
      <el-table-column prop="nickname" label="Name" />
      <el-table-column prop="realName" label="Real Name" />
      <el-table-column prop="phone" label="Phone" />
      <el-table-column label="Status">
        <template #default="scope">
          <el-switch :model-value="scope.row.status===1" @change="toggleStatus(scope.row)" />
        </template>
      </el-table-column>
    </el-table>

    <el-dialog v-model="showAdd" title="Add Staff">
      <el-form :model="form" label-width="80px">
        <el-form-item label="Username"><el-input v-model="form.username" /></el-form-item>
        <el-form-item label="Password"><el-input v-model="form.password" type="password" /></el-form-item>
        <el-form-item label="Nickname"><el-input v-model="form.nickname" /></el-form-item>
        <el-form-item label="Phone"><el-input v-model="form.phone" /></el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="showAdd=false">Cancel</el-button>
        <el-button type="primary" @click="addStaff">Submit</el-button>
      </template>
    </el-dialog>
  </div>
</template>
<script setup>
import { ref, onMounted } from 'vue';
import api from '../../api/index.js';
import { ElMessage } from 'element-plus';
const staff = ref([]), showAdd = ref(false);
const form = ref({ username:'', password:'', nickname:'', phone:'' });
onMounted(() => loadStaff());
function loadStaff() { api.get('/user/staff').then(r => staff.value = r || []); }
function toggleStatus(s) { api.put('/user/staff/'+s.id+'/status?status='+(s.status===1?0:1)).then(() => loadStaff()); }
function addStaff() { api.post('/user/staff', form.value).then(() => { showAdd.value=false; loadStaff(); ElMessage.success('Added'); }); }
</script>
