<template>
  <div style="padding:24px;">
    <h2>Dashboard</h2>
    <el-row :gutter="16" style="margin-top:16px;">
      <el-col :span="6"><el-card><h3>Today Revenue</h3><p style="font-size:28px;color:#4CAF50;">¥{{(dashboard.revenue||0).toFixed(2)}}</p></el-card></el-col>
      <el-col :span="6"><el-card><h3>Pending Orders</h3><p style="font-size:28px;color:#1565C0;">{{activeOrders.length||0}}</p></el-card></el-col>
      <el-col :span="6"><el-card><h3>Staff</h3><p style="font-size:28px;color:#7B1FA2;">{{staff.length||0}}</p></el-card></el-col>
      <el-col :span="6"><el-card><h3>Active Boardings</h3><p style="font-size:28px;color:#FF9800;">{{boardings.length||0}}</p></el-card></el-col>
    </el-row>

    <el-row :gutter="16" style="margin-top:16px;">
      <el-col :span="12">
        <el-card><h3>Hot Services Today</h3><div ref="hotChart" style="height:300px;margin-top:12px;"></div></el-card>
      </el-col>
      <el-col :span="12">
        <el-card><h3>Today Schedule</h3>
          <el-table :data="todaySchedule" size="small" style="margin-top:8px;">
            <el-table-column prop="staffId" label="Staff" width="80"/>
            <el-table-column prop="startTime" label="Start" width="80"/>
            <el-table-column prop="endTime" label="End" width="80"/>
            <el-table-column label="Slots">
              <template #default="s">{{s.row.maxSlots}}</template>
            </el-table-column>
          </el-table>
        </el-card>
      </el-col>
    </el-row>

    <el-card style="margin-top:16px;">
      <h3>Pending Orders</h3>
      <el-table :data="activeOrders" style="width:100%;margin-top:8px;" size="small">
        <el-table-column prop="orderNo" label="Order No" width="180"/>
        <el-table-column prop="totalAmount" label="Amount" width="100"/>
        <el-table-column label="Status" width="100">
          <template #default="s"><el-tag :type="['warning','info','','success','danger'][s.row.status]" size="small">{{statusMap[s.row.status]}}</el-tag></template>
        </el-table-column>
        <el-table-column label="Action" width="260">
          <template #default="s">
            <el-button v-if="s.row.status===1" size="small" type="success" @click="act('accept',s.row.id)">Accept</el-button>
            <el-button v-if="s.row.status===2" size="small" type="warning" @click="act('complete',s.row.id)">Complete</el-button>
            <el-button v-if="s.row.status===1" size="small" type="danger" @click="act('reject',s.row.id)">Reject</el-button>
            <el-button v-if="s.row.status<=1" size="small" @click="act('cancel',s.row.id)">Cancel</el-button>
          </template>
        </el-table-column>
      </el-table>
    </el-card>
  </div>
</template>

<script setup>
import { ref, onMounted, nextTick } from 'vue';
import api from '../../api/index.js';
import { ElMessage, ElMessageBox } from 'element-plus';
import * as echarts from 'echarts';

const dashboard = ref({}), activeOrders = ref([]), staff = ref([]), boardings = ref([]);
const todaySchedule = ref([]), hotChart = ref(null);
const statusMap = ['Unpaid','Paid','In-progress','Done','Cancelled'];

onMounted(async () => {
  try { dashboard.value = await api.get('/dashboard/today'); } catch {}
  try { activeOrders.value = await api.get('/order/pending'); } catch {}
  try { staff.value = await api.get('/user/staff'); } catch {}
  try { boardings.value = await api.get('/boarding/active'); } catch {}
  try { todaySchedule.value = await api.get('/schedule/staff'); } catch {}

  try {
    const hot = await api.get('/rank/service/hot');
    await nextTick();
    if (hotChart.value && hot) {
      const chart = echarts.init(hotChart.value);
      chart.setOption({
        tooltip: { trigger: 'item' },
        series: [{ type: 'pie', radius:['40%','70%'],
          data: (hot||[]).map(h => ({ name: 'Svc#'+h.serviceId, value: h.score })) }]
      });
    }
  } catch {}
});

function act(action, id) {
  if (action === 'reject' || action === 'cancel') {
    ElMessageBox.prompt('Reason?', 'Confirm').then(({value}) => {
      api.put('/order/'+id+'/'+action+'?reason='+(value||'')).then(() => location.reload());
    });
  } else {
    api.put('/order/'+id+'/'+action).then(() => { location.reload(); ElMessage.success('Done'); });
  }
}
</script>
