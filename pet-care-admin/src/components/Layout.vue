<template>
  <div style="display:flex;min-height:100vh;">
    <!-- Fixed sidebar, full viewport height -->
    <div style="width:200px;background:#304156;display:flex;flex-direction:column;position:fixed;top:0;left:0;bottom:0;overflow:hidden;">
      <div style="padding:20px 16px;border-bottom:1px solid rgba(255,255,255,0.1);">
        <div style="color:#fff;font-size:16px;font-weight:700;">爪爪护理</div>
        <div style="color:#bfcbd9;font-size:12px;margin-top:4px;">{{nickname}} · {{roleText}}</div>
      </div>
      <el-menu :default-active="$route.path" background-color="#304156" text-color="#bfcbd9" active-text-color="#4CAF50" router style="border-right:none;flex:1;">
        <el-menu-item index="/dashboard">数据看板</el-menu-item>
        <el-menu-item index="/staff">店员管理</el-menu-item>
        <el-menu-item index="/service">服务配置</el-menu-item>
        <el-menu-item index="/order">订单管理</el-menu-item>
        <el-menu-item index="/billing">营收报表</el-menu-item>
      </el-menu>
      <div style="padding:16px 20px;border-top:1px solid rgba(255,255,255,0.1);">
        <el-button style="width:100%;color:#ef5350;" link @click="logout">退出登录</el-button>
      </div>
    </div>
    <!-- Main content area, offset by sidebar width -->
    <div style="flex:1;margin-left:200px;background:#f0f2f5;min-height:100vh;">
      <router-view />
    </div>
  </div>
</template>
<script setup>
import { ref } from 'vue';
import { useRouter } from 'vue-router';
const router = useRouter();
const user = JSON.parse(localStorage.getItem('user') || '{}');
const nickname = ref(user.nickname || '管理员');
const roleMap = { 0:'宠物主', 1:'店员', 2:'店长' };
const roleText = ref(roleMap[user.role] || '');
function logout() {
  localStorage.removeItem('token');
  localStorage.removeItem('user');
  router.push('/login');
}
</script>
