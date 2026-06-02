<template>
  <div style="padding:24px;">
    <h2>数据看板</h2>
    <el-row :gutter="16" style="margin-top:16px;">
      <el-col :span="6"><el-card><h3>今日营收</h3><p style="font-size:28px;color:#4CAF50;">${{((dashboard.revenue||0)/100).toFixed(2)}}</p></el-card></el-col>
      <el-col :span="6"><el-card><h3>待接订单</h3><p style="font-size:28px;color:#1565C0;">{{activeOrders.length||0}}</p></el-card></el-col>
      <el-col :span="6"><el-card><h3>店员人数</h3><p style="font-size:28px;color:#7B1FA2;">{{staff.length||0}}</p></el-card></el-col>
      <el-col :span="6"><el-card><h3>在住寄养</h3><p style="font-size:28px;color:#FF9800;">{{boardings.length||0}}</p></el-card></el-col>
    </el-row>
    <el-row :gutter="16" style="margin-top:16px;">
      <el-col :span="12">
        <el-card><h3>今日热门服务</h3><div ref="hotChart" style="height:300px;margin-top:12px;"></div></el-card>
      </el-col>
      <el-col :span="12">
        <el-card><h3>今日排班</h3>
          <el-table :data="todaySchedule" size="small" style="margin-top:8px;">
            <el-table-column prop="staffId" label="店员" width="80"/>
            <el-table-column prop="startTime" label="开始" width="80"/>
            <el-table-column prop="endTime" label="结束" width="80"/>
            <el-table-column label="容量">
              <template #default="s">{{s.row.maxSlots}}</template>
            </el-table-column>
          </el-table>
        </el-card>
      </el-col>
    </el-row>
    <el-card style="margin-top:16px;">
      <h3>待接订单</h3>
      <el-table :data="activeOrders" style="width:100%;margin-top:8px;" size="small">
        <el-table-column prop="orderNo" label="订单号" width="180"/>
        <el-table-column prop="totalAmount" label="金额" width="100"/>
        <el-table-column label="状态" width="100">
          <template #default="s"><el-tag :type="['warning','info','','success','danger'][s.row.status]" size="small">{{statusMap[s.row.status]}}</el-tag></template>
        </el-table-column>
        <el-table-column label="操作" width="260">
          <template #default="s">
            <el-button v-if="s.row.status===1" size="small" type="success" @click="act('accept',s.row.id)">接单</el-button>
            <el-button v-if="s.row.status===2" size="small" type="warning" @click="act('complete',s.row.id)">完成</el-button>
            <el-button v-if="s.row.status===1" size="small" type="danger" @click="act('reject',s.row.id)">拒单</el-button>
            <el-button v-if="s.row.status<=1" size="small" @click="act('cancel',s.row.id)">取消</el-button>
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
const statusMap = ['待支付','已支付','服务中','已完成','已取消'];
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
          data: (hot||[]).map(h => ({ name: '服务#'+h.serviceId, value: h.score })) }]
      });
    }
  } catch {}
});
function act(action, id) {
  if (action === 'reject' || action === 'cancel') {
    ElMessageBox.prompt('请输入原因', '确认').then(({value}) => {
      api.put('/order/'+id+'/'+action+'?reason='+(value||'')).then(() => location.reload());
    });
  } else {
    api.put('/order/'+id+'/'+action).then(() => { location.reload(); ElMessage.success('完成'); });
  }
}
</script>
