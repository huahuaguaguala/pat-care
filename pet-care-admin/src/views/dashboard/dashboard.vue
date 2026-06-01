
<template>
  <div style="padding:24px;">
    <h2>Dashboard</h2>
    <el-row :gutter="16" style="margin-top:16px;">
      <el-col :span="6"><el-card><h3>Today Revenue</h3><p style="font-size:28px;color:#4CAF50;">${{(dashboard.revenue/100 || 0).toFixed(2)}}</p></el-card></el-col>
      <el-col :span="6"><el-card><h3>Active Orders</h3><p style="font-size:28px;color:#1565C0;">{{activeOrders.length || 0}}</p></el-card></el-col>
      <el-col :span="6"><el-card><h3>Staff</h3><p style="font-size:28px;color:#7B1FA2;">{{staff.length || 0}}</p></el-card></el-col>
      <el-col :span="6"><el-card><h3>Pets</h3><p style="font-size:28px;color:#FF9800;">-</p></el-card></el-col>
    </el-row>
    <el-card style="margin-top:16px;">
      <h3>Hot Services Today</h3>
      <el-table :data="hotServices" style="width:100%;margin-top:12px;">
        <el-table-column label="Service ID" prop="serviceId" />
        <el-table-column label="Orders" prop="score" />
      </el-table>
    </el-card>
    <el-card style="margin-top:16px;">
      <h3>Pending Orders</h3>
      <el-table :data="activeOrders" style="width:100%;">
        <el-table-column prop="orderNo" label="Order No" />
        <el-table-column prop="totalAmount" label="Amount" />
        <el-table-column label="Status">
          <template #default="scope">{{ statusMap[scope.row.status] }}</template>
        </el-table-column>
      </el-table>
    </el-card>
  </div>
</template>
<script setup>
import { ref, onMounted } from 'vue';
import api from '../../api/index.js';
const dashboard = ref({}), hotServices = ref([]), activeOrders = ref([]), staff = ref([]);
const statusMap = ['Unpaid','Paid','In-progress','Done','Cancelled'];
onMounted(async () => {
  try { const r = await api.get('/dashboard/today'); dashboard.value = r; } catch {}
  try { const r = await api.get('/rank/service/hot'); hotServices.value = r || []; } catch {}
  try { const r = await api.get('/order/pending'); activeOrders.value = r || []; } catch {}
  try { const r = await api.get('/user/staff'); staff.value = r || []; } catch {}
});
</script>
