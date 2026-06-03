<template>
  <section class="dashboard-page">
    <div class="metric-grid">
      <el-card v-for="metric in metrics" :key="metric.label" shadow="never" class="metric-card">
        <span>{{ metric.label }}</span>
        <strong>{{ metric.value }}</strong>
        <small>{{ metric.hint }}</small>
      </el-card>
    </div>

    <div class="chart-grid">
      <el-card shadow="never">
        <template #header>近 7 日服务趋势</template>
        <div ref="trendChartRef" class="chart" />
      </el-card>
      <el-card shadow="never">
        <template #header>情感分布</template>
        <div ref="emotionChartRef" class="chart" />
      </el-card>
    </div>

    <div class="bottom-grid">
      <el-card shadow="never">
        <template #header>热门问题 Top 10</template>
        <el-table :data="hotQuestions" size="small" empty-text="暂无热门问题">
          <el-table-column type="index" width="54" />
          <el-table-column prop="query" label="问题" min-width="220" show-overflow-tooltip />
          <el-table-column prop="count" label="次数" width="92" align="right" />
        </el-table>
      </el-card>

      <el-card shadow="never" class="advice-card">
        <template #header>服务建议</template>
        <el-skeleton v-if="loading" :rows="4" animated />
        <ul v-else>
          <li v-for="item in overview?.serviceAdvice ?? []" :key="item">{{ item }}</li>
        </ul>
      </el-card>
    </div>
  </section>
</template>

<script setup lang="ts">
import { computed, nextTick, onBeforeUnmount, onMounted, ref } from 'vue'
import * as echarts from 'echarts'
import { ElMessage } from 'element-plus'
import {
  fetchDashboardOverview,
  fetchDashboardTrends,
  fetchHotQuestions,
  type DashboardOverview,
  type DashboardTrends,
  type HotQuestion,
} from '@/api/admin'

const overview = ref<DashboardOverview | null>(null)
const trends = ref<DashboardTrends | null>(null)
const hotQuestions = ref<HotQuestion[]>([])
const loading = ref(false)
const trendChartRef = ref<HTMLDivElement | null>(null)
const emotionChartRef = ref<HTMLDivElement | null>(null)
let trendChart: echarts.ECharts | null = null
let emotionChart: echarts.ECharts | null = null

const metrics = computed(() => [
  { label: '累计问答', value: overview.value?.totalInteractions ?? 0, hint: '全部游客交互' },
  { label: '今日服务', value: overview.value?.todayInteractions ?? 0, hint: '今日新增交互' },
  { label: '平均满意度', value: `${overview.value?.averageSatisfaction ?? 0}/5`, hint: '来自游客反馈' },
  { label: '拒答率', value: `${overview.value?.rejectRate ?? 0}%`, hint: '知识依据不足' },
  { label: '平均耗时', value: `${overview.value?.averageLatencyMs ?? 0}ms`, hint: '问答链路耗时' },
  { label: '启用文档', value: overview.value?.activeDocuments ?? 0, hint: '当前可检索资料' },
])

onMounted(async () => {
  await loadDashboard()
  window.addEventListener('resize', resizeCharts)
})

onBeforeUnmount(() => {
  window.removeEventListener('resize', resizeCharts)
  trendChart?.dispose()
  emotionChart?.dispose()
})

async function loadDashboard() {
  loading.value = true
  try {
    const [overviewData, trendData, hotData] = await Promise.all([
      fetchDashboardOverview(),
      fetchDashboardTrends(7),
      fetchHotQuestions(10),
    ])
    overview.value = overviewData
    trends.value = trendData
    hotQuestions.value = hotData
    await nextTick()
    renderCharts()
  } catch {
    ElMessage.error('数据大屏加载失败')
  } finally {
    loading.value = false
  }
}

function renderCharts() {
  if (trendChartRef.value) {
    trendChart?.dispose()
    trendChart = echarts.init(trendChartRef.value)
    trendChart.setOption({
      tooltip: { trigger: 'axis' },
      grid: { left: 38, right: 20, top: 28, bottom: 34 },
      xAxis: { type: 'category', data: trends.value?.daily.map((item) => item.date.slice(5)) ?? [] },
      yAxis: { type: 'value', minInterval: 1 },
      series: [
        {
          name: '交互次数',
          type: 'line',
          smooth: true,
          areaStyle: { opacity: 0.12 },
          data: trends.value?.daily.map((item) => item.interactions) ?? [],
          color: '#00a884',
        },
        {
          name: '满意度',
          type: 'line',
          smooth: true,
          data: trends.value?.daily.map((item) => item.averageSatisfaction ?? 0) ?? [],
          color: '#c7923e',
        },
      ],
    })
  }
  if (emotionChartRef.value) {
    emotionChart?.dispose()
    emotionChart = echarts.init(emotionChartRef.value)
    emotionChart.setOption({
      tooltip: { trigger: 'item' },
      series: [
        {
          type: 'pie',
          radius: ['46%', '72%'],
          data: Object.entries(trends.value?.emotions ?? {}).map(([name, value]) => ({ name, value })),
          color: ['#00a884', '#4f7cff', '#c7923e', '#8b5cf6', '#94a3b8'],
        },
      ],
    })
  }
}

function resizeCharts() {
  trendChart?.resize()
  emotionChart?.resize()
}
</script>

<style scoped>
.dashboard-page {
  display: grid;
  gap: 18px;
}

.metric-grid {
  display: grid;
  grid-template-columns: repeat(6, minmax(0, 1fr));
  gap: 14px;
}

.metric-card :deep(.el-card__body) {
  display: grid;
  gap: 7px;
}

.metric-card span,
.metric-card small {
  color: #6b7280;
}

.metric-card strong {
  color: #111827;
  font-size: 28px;
  line-height: 1;
}

.chart-grid,
.bottom-grid {
  display: grid;
  grid-template-columns: minmax(0, 1.35fr) minmax(320px, 0.65fr);
  gap: 18px;
}

.chart {
  height: 320px;
}

.advice-card ul {
  display: grid;
  gap: 12px;
  margin: 0;
  padding-left: 18px;
  color: #374151;
  line-height: 1.7;
}

@media (max-width: 1180px) {
  .metric-grid {
    grid-template-columns: repeat(3, minmax(0, 1fr));
  }

  .chart-grid,
  .bottom-grid {
    grid-template-columns: 1fr;
  }
}

@media (max-width: 640px) {
  .metric-grid {
    grid-template-columns: 1fr;
  }
}
</style>
