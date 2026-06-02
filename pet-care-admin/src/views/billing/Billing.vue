<template>
  <div style="padding:24px;">
    <h2>营收报表</h2>
    <el-card style="margin:12px 0;">
      <el-row :gutter="12" align="middle">
        <el-col :span="4"><el-date-picker v-model="dateRange" type="daterange" range-separator="~" value-format="YYYY-MM-DD" style="width:100%;"/></el-col>
        <el-col :span="6">
          <el-radio-group v-model="quickRange" @change="setQuickRange">
            <el-radio-button label="today">今天</el-radio-button>
            <el-radio-button label="week">本周</el-radio-button>
            <el-radio-button label="month">本月</el-radio-button>
          </el-radio-group>
        </el-col>
        <el-col :span="2"><el-button type="primary" @click="loadReport">查询</el-button></el-col>
      </el-row>
    </el-card>
    <el-row :gutter="16" style="margin-top:16px;">
      <el-col :span="6"><el-card><h3>总营收</h3><p style="font-size:28px;color:#4CAF50;">${{(report.totalRevenue||0).toFixed(2)}}</p></el-card></el-col>
      <el-col :span="6"><el-card><h3>总退款</h3><p style="font-size:28px;color:#f44336;">${{(report.totalRefund||0).toFixed(2)}}</p></el-card></el-col>
      <el-col :span="6"><el-card><h3>净收入</h3><p style="font-size:28px;color:#1565C0;">${{(report.netRevenue||0).toFixed(2)}}</p></el-card></el-col>
      <el-col :span="6"><el-card><h3>订单量</h3><p style="font-size:20px;color:#FF9800;">{{report.totalOrders||0}} 已完成</p><p style="font-size:12px;color:#999;">{{report.totalRefundOrders||0}} 已退款</p></el-card></el-col>
    </el-row>
    <el-card style="margin-top:16px;"><h3>每日趋势</h3><div ref="chartDom" style="height:350px;"></div></el-card>
    <el-card style="margin-top:16px;"><h3>每日明细</h3>
      <el-table :data="report.dailyBreakdown" border size="small">
        <el-table-column prop="date" label="日期" width="120"/>
        <el-table-column label="营收"><template #default="s">${{(s.row.revenue||0).toFixed(2)}}</template></el-table-column>
        <el-table-column label="退款"><template #default="s">${{(s.row.refund||0).toFixed(2)}}</template></el-table-column>
        <el-table-column prop="orderCount" label="订单数" width="80"/>
      </el-table>
    </el-card>
  </div>
</template>
<script setup>
import { ref, onMounted, nextTick } from 'vue';
import api from '../../api/index.js';
import * as echarts from 'echarts';
const report = ref({}), dateRange = ref([]), quickRange = ref('today'), chartDom = ref(null);
onMounted(() => { const t = new Date().toISOString().split('T')[0]; dateRange.value = [t, t]; loadReport(); });
function setQuickRange(v) {
  const t = new Date(); const d = new Date(t);
  if (v === 'today') dateRange.value = [fmt(t), fmt(t)];
  else if (v === 'week') { d.setDate(t.getDate() - 7); dateRange.value = [fmt(d), fmt(t)]; }
  else { d.setMonth(t.getMonth() - 1); dateRange.value = [fmt(d), fmt(t)]; }
  loadReport();
}
function fmt(d) { return d.toISOString().split('T')[0]; }
async function loadReport() {
  if (!dateRange.value || dateRange.value.length < 2) return;
  try {
    const data = await api.get('/admin/revenue?startDate=' + dateRange.value[0] + '&endDate=' + dateRange.value[1]);
    report.value = data || {};
    await nextTick();
    if (chartDom.value && data && data.dailyBreakdown) {
      const c = echarts.init(chartDom.value);
      c.setOption({
        tooltip: { trigger: 'axis' }, legend: { data: ['营收', '退款'] },
        xAxis: { type: 'category', data: data.dailyBreakdown.map(d => d.date) }, yAxis: { type: 'value' },
        series: [
          { name: '营收', type: 'bar', data: data.dailyBreakdown.map(d => (d.revenue || 0).toFixed(2)), itemStyle: { color: '#4CAF50' } },
          { name: '退款', type: 'bar', data: data.dailyBreakdown.map(d => (d.refund || 0).toFixed(2)), itemStyle: { color: '#f44336' } }
        ]
      });
    }
  } catch {}
}
</script>
