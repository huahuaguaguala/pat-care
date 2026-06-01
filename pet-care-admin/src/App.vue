<template>
  <el-container style="min-height:100vh;">
    <el-aside width="200px" style="background:#304156;">
      <div style="color:#fff;font-size:18px;font-weight:700;padding:20px;text-align:center;">ZippyCare</div>
      <el-menu :default-active="$route.path" background-color="#304156" text-color="#bfcbd9" active-text-color="#4CAF50" router>
        <el-menu-item index="/dashboard">Dashboard</el-menu-item>
        <el-menu-item index="/staff">Staff</el-menu-item>
        <el-menu-item index="/service">Service</el-menu-item>
        <el-menu-item index="/order">Order</el-menu-item>
      </el-menu>
    </el-aside>
    <el-main style="background:#f0f2f5;"><router-view /></el-main>
  </el-container>
</template>
<script setup>
import { onMounted } from 'vue';
import api from './api/index.js';
onMounted(async () => {
  if (!localStorage.getItem('token')) {
    const user = prompt('Staff/Admin Login - Username:', 'staff01');
    const pass = prompt('Password:', '123456');
    if (user && pass) {
      try {
        const r = await api.post('/auth/login', { username: user, password: pass });
        if (r && r.token) localStorage.setItem('token', r.token);
      } catch(e) { alert('Login failed'); }
    }
  }
});
</script>
<style>
@import 'element-plus/dist/index.css';
body { margin:0; font-family: sans-serif; }
</style>
