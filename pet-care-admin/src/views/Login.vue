<template>
  <div style="display:flex;justify-content:center;align-items:center;height:100vh;background:linear-gradient(135deg,#4CAF50,#81C784);">
    <el-card style="width:380px;padding:20px;">
      <h2 style="text-align:center;margin-bottom:24px;color:#333;">爪爪护理 · 店长后台</h2>
      <el-form :model="form" label-width="0">
        <el-form-item><el-input v-model="form.username" placeholder="账号" prefix-icon="User" size="large"/></el-form-item>
        <el-form-item><el-input v-model="form.password" type="password" placeholder="密码" prefix-icon="Lock" size="large" @keyup.enter="login"/></el-form-item>
        <el-form-item><el-button type="primary" size="large" style="width:100%;" @click="login" :loading="loading">登 录</el-button></el-form-item>
      </el-form>
      <div style="text-align:center;color:#999;font-size:12px;margin-top:8px;">测试账号：staff01 / admin01  密码：123456</div>
    </el-card>
  </div>
</template>
<script setup>
import { ref } from 'vue';
import { useRouter } from 'vue-router';
import api from '../api/index.js';
import { ElMessage } from 'element-plus';
const router = useRouter();
const form = ref({ username: 'admin01', password: '123456' });
const loading = ref(false);
async function login() {
  loading.value = true;
  try {
    const r = await api.post('/auth/login', { username: form.value.username, password: form.value.password });
    if (r && r.token) {
      localStorage.setItem('token', r.token);
      localStorage.setItem('user', JSON.stringify(r.user || {}));
      router.push('/dashboard');
    }
  } catch(e) {
    ElMessage.error('登录失败，请检查账号密码');
  } finally { loading.value = false; }
}
</script>
