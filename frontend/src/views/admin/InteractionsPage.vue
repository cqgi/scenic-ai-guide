<template>
  <section class="interactions-page">
    <el-card shadow="never">
      <el-form :inline="true" class="filter-bar">
        <el-form-item label="关键词">
          <el-input v-model="filters.keyword" clearable placeholder="问题或回答" />
        </el-form-item>
        <el-form-item label="意图">
          <el-select v-model="filters.intent" clearable placeholder="全部" style="width: 170px">
            <el-option label="景区问答" value="scenic_qa" />
            <el-option label="路线推荐" value="route_recommendation" />
            <el-option label="闲聊" value="smalltalk" />
            <el-option label="未知" value="unknown" />
          </el-select>
        </el-form-item>
        <el-form-item label="情绪">
          <el-select v-model="filters.emotion" clearable placeholder="全部" style="width: 140px">
            <el-option label="开心" value="happy" />
            <el-option label="抱歉" value="sorry" />
            <el-option label="思考" value="thinking" />
            <el-option label="中性" value="neutral" />
          </el-select>
        </el-form-item>
        <el-form-item label="满意度">
          <el-select v-model="filters.satisfaction" clearable placeholder="全部" style="width: 120px">
            <el-option v-for="score in [5, 4, 3, 2, 1]" :key="score" :label="`${score} 分`" :value="score" />
          </el-select>
        </el-form-item>
        <el-form-item>
          <el-button type="primary" @click="loadInteractions">查询</el-button>
        </el-form-item>
      </el-form>

      <el-table v-loading="loading" :data="page.items" empty-text="暂无交互记录">
        <el-table-column prop="userQuery" label="用户问题" min-width="220" show-overflow-tooltip />
        <el-table-column prop="intent" label="意图" width="150" />
        <el-table-column prop="emotion" label="情绪" width="100" />
        <el-table-column label="满意度" width="110">
          <template #default="{ row }">{{ row.satisfaction ? `${row.satisfaction} 分` : '-' }}</template>
        </el-table-column>
        <el-table-column label="耗时" width="110">
          <template #default="{ row }">{{ row.latencyMs ?? 0 }}ms</template>
        </el-table-column>
        <el-table-column label="时间" width="180">
          <template #default="{ row }">{{ formatTime(row.createdAt) }}</template>
        </el-table-column>
        <el-table-column label="操作" width="100" fixed="right">
          <template #default="{ row }">
            <el-button size="small" @click="openDetail(row.id)">详情</el-button>
          </template>
        </el-table-column>
      </el-table>

      <el-pagination
        class="pager"
        layout="prev, pager, next, total"
        :current-page="page.page + 1"
        :page-size="page.size"
        :total="page.total"
        @current-change="changePage"
      />
    </el-card>

    <el-drawer v-model="detailVisible" title="交互详情" size="62%">
      <el-skeleton v-if="detailLoading" :rows="8" animated />
      <div v-else-if="detail" class="detail-content">
        <section class="detail-block">
          <h3>问答内容</h3>
          <p><strong>问题：</strong>{{ detail.interaction.userQuery }}</p>
          <p v-if="detail.interaction.asrText"><strong>ASR：</strong>{{ detail.interaction.asrText }}</p>
          <p><strong>回答：</strong>{{ detail.interaction.answer }}</p>
          <div class="meta-line">
            <el-tag>{{ detail.interaction.intent }}</el-tag>
            <el-tag type="success">{{ detail.interaction.emotion }}</el-tag>
            <el-tag type="warning">{{ detail.interaction.latencyMs ?? 0 }}ms</el-tag>
            <el-tag v-if="detail.interaction.satisfaction">{{ detail.interaction.satisfaction }} 分</el-tag>
          </div>
        </section>

        <section class="detail-block">
          <h3>来源引用</h3>
          <el-empty v-if="!detail.sourceChunks.length" description="暂无来源 chunk" />
          <div v-for="chunk in detail.sourceChunks" :key="chunk.id" class="source-item">
            <strong>{{ chunk.title }}</strong>
            <small>{{ chunk.documentTitle }} · {{ chunk.keywords || '无关键词' }}</small>
            <p>{{ chunk.summary || chunk.content }}</p>
          </div>
        </section>

        <section class="detail-block">
          <h3>RAG Trace</h3>
          <el-empty v-if="!detail.trace" description="暂无检索 trace" />
          <template v-else>
            <p v-if="detail.trace.rejected" class="reject-reason">拒答原因：{{ detail.trace.rejectReason }}</p>
            <el-tabs>
              <el-tab-pane label="BM25">
                <CandidateList :items="detail.trace.bm25Candidates" score-key="bm25Score" />
              </el-tab-pane>
              <el-tab-pane label="关键词">
                <CandidateList :items="detail.trace.keywordCandidates" score-key="keywordScore" />
              </el-tab-pane>
              <el-tab-pane label="向量">
                <CandidateList :items="detail.trace.vectorCandidates" score-key="vectorScore" />
              </el-tab-pane>
              <el-tab-pane label="融合">
                <CandidateList :items="detail.trace.finalCandidates" score-key="finalScore" />
              </el-tab-pane>
            </el-tabs>
          </template>
        </section>
      </div>
    </el-drawer>
  </section>
</template>

<script setup lang="ts">
import { defineComponent, h, onMounted, reactive, ref } from 'vue'
import { ElMessage } from 'element-plus'
import {
  fetchInteractionDetail,
  fetchInteractions,
  type InteractionDetail,
  type InteractionSummary,
  type PageResponse,
  type RetrievalCandidate,
} from '@/api/admin'

const CandidateList = defineComponent({
  props: {
    items: { type: Array<RetrievalCandidate>, required: true },
    scoreKey: { type: String, required: true },
  },
  setup(props) {
    return () =>
      props.items.length
        ? h(
            'div',
            { class: 'candidate-list' },
            props.items.map((item) =>
              h('article', { class: 'candidate-item', key: item.chunkId }, [
                h('div', [
                  h('strong', item.title),
                  h('span', Number(item[props.scoreKey as keyof RetrievalCandidate] ?? 0).toFixed(3)),
                ]),
                h('p', item.content),
              ]),
            ),
          )
        : h('div', { class: 'empty-inline' }, '暂无候选')
  },
})

const filters = reactive<{ keyword: string; intent: string; emotion: string; satisfaction: number | null }>({
  keyword: '',
  intent: '',
  emotion: '',
  satisfaction: null,
})
const page = reactive<PageResponse<InteractionSummary>>({ items: [], page: 0, size: 10, total: 0, totalPages: 0 })
const loading = ref(false)
const detailVisible = ref(false)
const detailLoading = ref(false)
const detail = ref<InteractionDetail | null>(null)

onMounted(loadInteractions)

async function loadInteractions() {
  loading.value = true
  try {
    const result = await fetchInteractions({
      page: page.page,
      size: page.size,
      keyword: filters.keyword || undefined,
      intent: filters.intent || undefined,
      emotion: filters.emotion || undefined,
      satisfaction: filters.satisfaction,
    })
    Object.assign(page, result)
  } catch {
    ElMessage.error('交互记录加载失败')
  } finally {
    loading.value = false
  }
}

function changePage(next: number) {
  page.page = next - 1
  void loadInteractions()
}

async function openDetail(id: number) {
  detailVisible.value = true
  detailLoading.value = true
  detail.value = null
  try {
    detail.value = await fetchInteractionDetail(id)
  } catch {
    ElMessage.error('交互详情加载失败')
  } finally {
    detailLoading.value = false
  }
}

function formatTime(value: string) {
  return new Date(value).toLocaleString()
}
</script>

<style scoped>
.interactions-page {
  display: grid;
  gap: 16px;
}

.filter-bar {
  padding-bottom: 8px;
  border-bottom: 1px solid #eef2f7;
}

.pager {
  justify-content: flex-end;
  margin-top: 16px;
}

.detail-content {
  display: grid;
  gap: 18px;
}

.detail-block {
  padding-bottom: 16px;
  border-bottom: 1px solid #eef2f7;
}

.detail-block h3 {
  margin: 0 0 12px;
  color: #111827;
}

.detail-block p {
  color: #374151;
  line-height: 1.7;
  white-space: pre-wrap;
}

.meta-line {
  display: flex;
  flex-wrap: wrap;
  gap: 8px;
}

.source-item,
:deep(.candidate-item) {
  padding: 12px;
  border: 1px solid #e5e7eb;
  border-radius: 8px;
  margin-bottom: 10px;
  background: #fbfdff;
}

.source-item strong,
.source-item small {
  display: block;
}

.source-item small {
  margin-top: 4px;
  color: #6b7280;
}

:deep(.candidate-item > div) {
  display: flex;
  justify-content: space-between;
  gap: 12px;
}

:deep(.candidate-item span) {
  color: #00a884;
  font-variant-numeric: tabular-nums;
}

:deep(.candidate-item p) {
  max-height: 92px;
  overflow: hidden;
  margin: 8px 0 0;
  color: #4b5563;
  line-height: 1.6;
}

.reject-reason {
  color: #b45309;
}

.empty-inline {
  padding: 18px;
  color: #6b7280;
  text-align: center;
}
</style>
