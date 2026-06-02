<template>
  <div style="padding:24px;">
    <h2>店员管理</h2>
    <el-button type="primary" @click="showAdd=true" style="margin:12px 0;">+ 添加店员</el-button>
    <el-table :data="staff" border>
      <el-table-column prop="id" label="ID" width="60" />
      <el-table-column prop="nickname" label="昵称" />
      <el-table-column prop="realName" label="真实姓名" />
      <el-table-column prop="phone" label="手机号" />
      <el-table-column label="状态">
        <template #default="scope">
          <el-switch :model-value="scope.row.status===1" @change="toggleStatus(scope.row)" />
        </template>
      </el-table-column>
    </el-table>

    <el-dialog v-model="showAdd" title="添加店员">
      <el-form :model="form" label-width="80px">
        <el-form-item label="账号"><el-input v-model="form.username" /></el-form-item>
        <el-form-item label="密码"><el-input v-model="form.password" type="password" /></el-form-item>
        <el-form-item label="昵称"><el-input v-model="form.nickname" /></el-form-item>
        <el-form-item label="手机号"><el-input v-model="form.phone" /></el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="showAdd=false">取消</el-button>
        <el-button type="primary" @click="addStaff">提交</el-button>
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
function loadStaff() {
  api.get('/user/staff').then(r => staff.value = r || [])
    .catch(() => ElMessage.warning('无店员管理权限，请用店长账号登录'));
}
function toggleStatus(s) { api.put('/user/staff/'+s.id+'/status?status='+(s.status===1?0:1)).then(() => loadStaff()); }
function addStaff() {
  api.post('/user/staff', form.value)
    .then(() => { showAdd.value=false; loadStaff(); ElMessage.success('添加成功'); })
    .catch(e => ElMessage.error(e.message || '添加失败'));
}
</script>
