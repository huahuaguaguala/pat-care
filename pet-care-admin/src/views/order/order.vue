<template>
  <div style="padding:24px;">
    <h2>订单管理</h2>
    <el-card style="margin:12px 0;">
      <el-row :gutter="12" align="middle">
        <el-col :span="4"><el-date-picker v-model="dateRange" type="daterange" range-separator="~" start-placeholder="从" end-placeholder="到" value-format="YYYY-MM-DD" style="width:100%;"/></el-col>
        <el-col :span="3"><el-select v-model="filterStatus" placeholder="状态" clearable style="width:100%;" @change="loadOrders">
          <el-option v-for="(name,i) in statusMap" :key="i" :label="name" :value="i"/>
        </el-select></el-col>
        <el-col :span="3">
          <el-select v-model="quickRange" placeholder="快捷" clearable @change="setQuickRange" style="width:100%;">
            <el-option label="今天" value="today"/><el-option label="本周" value="week"/><el-option label="本月" value="month"/>
          </el-select>
        </el-col>
        <el-col :span="2"><el-button type="primary" @click="loadOrders">查询</el-button></el-col>
        <el-col :span="6" style="text-align:right;"><el-tag>共 {{orders.length}} 单</el-tag></el-col>
      </el-row>
    </el-card>
    <el-table :data="orders" border size="small" max-height="500">
      <el-table-column prop="order.orderNo" label="订单号" width="180"/>
      <el-table-column label="服务明细" min-width="200">
        <template #default="s"><span v-for="d in s.row.details" :key="d.id" style="display:block;font-size:12px;">{{d.serviceName}} x{{d.quantity}} ${{d.subtotal}}</span></template>
      </el-table-column>
      <el-table-column prop="order.totalAmount" label="金额" width="100"/>
      <el-table-column label="状态" width="110">
        <template #default="s">
          <el-tag :type="['warning','info','','success','danger'][s.row.order.status]" size="small">{{statusMap[s.row.order.status]}}</el-tag>
        </template>
      </el-table-column>
      <el-table-column prop="order.createTime" label="创建时间" width="160"/>
      <el-table-column prop="order.appointmentTime" label="预约时间" width="160"/>
      <el-table-column label="操作" width="260" fixed="right">
        <template #default="s">
          <el-button v-if="s.row.order.status===1" size="small" type="success" @click="act('accept',s.row.order.id)">接单</el-button>
          <el-button v-if="s.row.order.status===2" size="small" type="warning" @click="act('complete',s.row.order.id)">完成</el-button>
          <el-button v-if="s.row.order.status===1" size="small" type="danger" @click="act('reject',s.row.order.id)">拒单</el-button>
          <el-button v-if="s.row.order.status===3" size="small" @click="act('refund',s.row.order.id)">退款</el-button>
        </template>
      </el-table-column>
    </el-table>
  </div>
</template>
<script setup>
import { ref, onMounted } from 'vue';
import api from '../../api/index.js';
import { ElMessage, ElMessageBox } from 'element-plus';
const orders = ref([]), dateRange = ref([]), filterStatus = ref(null), quickRange = ref(null);
const statusMap = ['待支付','已支付','服务中','已完成','已取消'];
onMounted(() => { const t = new Date().toISOString().split('T')[0]; dateRange.value = [t, t]; loadOrders(); });
function setQuickRange(v) {
  const t = new Date(); const d = new Date(t);
  if (v === 'today') dateRange.value = [fmt(t), fmt(t)];
  else if (v === 'week') { d.setDate(t.getDate() - 7); dateRange.value = [fmt(d), fmt(t)]; }
  else { d.setMonth(t.getMonth() - 1); dateRange.value = [fmt(d), fmt(t)]; }
  loadOrders();
}
function fmt(d) { return d.toISOString().split('T')[0]; }
async function loadOrders() {
  if (!dateRange.value || dateRange.value.length < 2) return;
  let url = '/admin/orders?startDate=' + dateRange.value[0] + '&endDate=' + dateRange.value[1];
  if (filterStatus.value !== null && filterStatus.value !== '') url += '&status=' + filterStatus.value;
  try { orders.value = await api.get(url); } catch { orders.value = []; }
}
function act(action, id) {
  if (['reject','cancel','refund'].includes(action)) {
    ElMessageBox.prompt('请输入' + action + '的原因', '确认').then(({value}) => {
      api.put('/order/' + id + '/' + action + '?reason=' + (value||'')).then(() => { loadOrders(); ElMessage.success('完成'); });
    });
  } else {
    api.put('/order/' + id + '/' + action).then(() => { loadOrders(); ElMessage.success('完成'); });
  }
}
</script>
