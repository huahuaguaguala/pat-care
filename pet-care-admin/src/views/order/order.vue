
<template>
  <div style="padding:24px;">
    <h2>Order Management</h2>
    <el-tabs v-model="tab" @tab-change="loadOrders">
      <el-tab-pane label="Pending" name="pending" />
      <el-tab-pane label="All" name="all" />
    </el-tabs>
    <el-table :data="orders" border>
      <el-table-column prop="orderNo" label="Order No" width="180" />
      <el-table-column prop="totalAmount" label="Amount" width="100" />
      <el-table-column label="Status" width="120">
        <template #default="scope">
          <el-tag :type="['warning','info','','success','danger'][scope.row.status]">{{ ['Unpaid','Paid','In-progress','Done','Cancelled'][scope.row.status] }}</el-tag>
        </template>
      </el-table-column>
      <el-table-column prop="appointmentTime" label="Appointment" width="160" />
      <el-table-column prop="remark" label="Remark" />
      <el-table-column label="Action" width="280">
        <template #default="scope">
          <el-button v-if="scope.row.status===0" size="small" type="primary" @click="act('pay',scope.row.id)">Pay</el-button>
          <el-button v-if="scope.row.status===1" size="small" type="success" @click="act('accept',scope.row.id)">Accept</el-button>
          <el-button v-if="scope.row.status===2" size="small" type="warning" @click="act('complete',scope.row.id)">Complete</el-button>
          <el-button v-if="scope.row.status===1" size="small" type="danger" @click="act('reject',scope.row.id)">Reject</el-button>
          <el-button v-if="scope.row.status<=1" size="small" @click="act('cancel',scope.row.id)">Cancel</el-button>
        </template>
      </el-table-column>
    </el-table>
  </div>
</template>
<script setup>
import { ref, onMounted } from 'vue';
import api from '../../api/index.js';
import { ElMessage } from 'element-plus';
const orders = ref([]), tab = ref('pending');
onMounted(() => loadOrders());
function loadOrders() {
  const url = tab.value === 'pending' ? '/order/pending' : '/order/my';
  api.get(url).then(r => orders.value = Array.isArray(r) ? (r[0]?.order ? r.map(x=>x.order) : r) : []);
}
function act(action, id) {
  const prompts = { reject: 'Reason for rejection?' };
  const fn = () => api.put('/order/'+id+'/'+action).then(() => { loadOrders(); ElMessage.success('Done'); });
  if (prompts[action]) {
    ElMessageBox.prompt(prompts[action], 'Confirm').then(({value}) => {
      api.put('/order/'+id+'/'+action+'?reason='+(value||'')).then(() => { loadOrders(); ElMessage.success('Done'); });
    });
  } else fn();
}
</script>
