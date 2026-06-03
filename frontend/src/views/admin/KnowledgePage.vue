<template>
  <section class="knowledge-page">
    <el-card shadow="never">
      <div class="toolbar">
        <div>
          <h2>知识库文档</h2>
          <p>上传资料后自动切分 chunk，并进入 BM25、关键词、向量混合检索。</p>
        </div>
        <el-button type="primary" @click="uploadDialogVisible = true">上传文档</el-button>
      </div>

      <el-table v-loading="loading" :data="documents" empty-text="暂无知识文档">
        <el-table-column prop="title" label="标题" min-width="180" show-overflow-tooltip />
        <el-table-column prop="fileName" label="文件名" min-width="180" show-overflow-tooltip />
        <el-table-column prop="docType" label="类型" width="90" />
        <el-table-column label="状态" width="100">
          <template #default="{ row }">
            <el-tag :type="row.status === 'active' ? 'success' : 'info'">
              {{ row.status === 'active' ? '启用' : '停用' }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column label="Chunk" width="120">
          <template #default="{ row }">{{ row.enabledChunkCount }}/{{ row.chunkCount }}</template>
        </el-table-column>
        <el-table-column label="更新时间" width="180">
          <template #default="{ row }">{{ formatTime(row.updatedAt) }}</template>
        </el-table-column>
        <el-table-column label="操作" width="260" fixed="right">
          <template #default="{ row }">
            <el-button size="small" @click="openChunks(row)">查看 chunk</el-button>
            <el-button size="small" :type="row.status === 'active' ? 'warning' : 'success'" @click="toggleDocument(row)">
              {{ row.status === 'active' ? '停用' : '启用' }}
            </el-button>
            <el-button size="small" type="danger" plain @click="removeDocument(row)">删除</el-button>
          </template>
        </el-table-column>
      </el-table>
    </el-card>

    <el-dialog v-model="uploadDialogVisible" title="上传知识文档" width="460px">
      <el-form label-position="top">
        <el-form-item label="文档标题">
          <el-input v-model="uploadForm.title" placeholder="如：红叶谷讲解资料" />
        </el-form-item>
        <el-form-item label="文件">
          <input type="file" accept=".txt,.md,.pdf" @change="handleFileChange" />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="uploadDialogVisible = false">取消</el-button>
        <el-button type="primary" :loading="uploading" @click="submitUpload">上传并解析</el-button>
      </template>
    </el-dialog>

    <el-drawer v-model="chunkDrawerVisible" :title="currentDocument?.title ?? 'Chunk 查看'" size="58%">
      <el-table v-loading="chunkLoading" :data="chunks" size="small" empty-text="暂无 chunk">
        <el-table-column prop="title" label="标题" min-width="180" show-overflow-tooltip />
        <el-table-column prop="summary" label="摘要" min-width="240" show-overflow-tooltip />
        <el-table-column prop="keywords" label="关键词" min-width="160" show-overflow-tooltip />
        <el-table-column prop="scenicSpotName" label="景点" width="120" />
        <el-table-column label="启用" width="92">
          <template #default="{ row }">
            <el-switch v-model="row.enabled" :loading="chunkTogglingId === row.id" @change="toggleChunk(row)" />
          </template>
        </el-table-column>
      </el-table>

      <el-divider />
      <div class="chunk-preview" v-for="chunk in chunks" :key="chunk.id">
        <div>
          <strong>{{ chunk.title }}</strong>
          <el-tag size="small" :type="chunk.enabled ? 'success' : 'info'">{{ chunk.enabled ? '启用' : '停用' }}</el-tag>
        </div>
        <p>{{ chunk.content }}</p>
      </div>
    </el-drawer>
  </section>
</template>

<script setup lang="ts">
import { onMounted, reactive, ref } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import {
  deleteKnowledgeDocument,
  fetchKnowledgeChunks,
  fetchKnowledgeDocuments,
  updateKnowledgeChunkEnabled,
  updateKnowledgeDocumentStatus,
  uploadKnowledgeDocument,
  type KnowledgeChunk,
  type KnowledgeDocument,
} from '@/api/admin'

const documents = ref<KnowledgeDocument[]>([])
const chunks = ref<KnowledgeChunk[]>([])
const loading = ref(false)
const chunkLoading = ref(false)
const uploading = ref(false)
const uploadDialogVisible = ref(false)
const chunkDrawerVisible = ref(false)
const currentDocument = ref<KnowledgeDocument | null>(null)
const selectedFile = ref<File | null>(null)
const chunkTogglingId = ref<number | null>(null)
const uploadForm = reactive({ title: '' })

onMounted(loadDocuments)

async function loadDocuments() {
  loading.value = true
  try {
    documents.value = await fetchKnowledgeDocuments(1)
  } catch {
    ElMessage.error('知识库列表加载失败')
  } finally {
    loading.value = false
  }
}

function handleFileChange(event: Event) {
  const input = event.target as HTMLInputElement
  selectedFile.value = input.files?.[0] ?? null
  if (selectedFile.value && !uploadForm.title) {
    uploadForm.title = selectedFile.value.name.replace(/\.[^.]+$/, '')
  }
}

async function submitUpload() {
  if (!uploadForm.title.trim() || !selectedFile.value) {
    ElMessage.warning('请填写标题并选择文件')
    return
  }
  uploading.value = true
  try {
    const result = await uploadKnowledgeDocument(1, uploadForm.title.trim(), selectedFile.value)
    ElMessage.success(`上传成功，生成 ${result.chunkCount} 个 chunk`)
    uploadDialogVisible.value = false
    uploadForm.title = ''
    selectedFile.value = null
    await loadDocuments()
  } catch {
    ElMessage.error('上传失败，请检查文件内容')
  } finally {
    uploading.value = false
  }
}

async function toggleDocument(row: KnowledgeDocument) {
  const nextStatus = row.status === 'active' ? 'disabled' : 'active'
  try {
    const updated = await updateKnowledgeDocumentStatus(row.id, nextStatus)
    Object.assign(row, updated)
    ElMessage.success(nextStatus === 'active' ? '文档已启用' : '文档已停用')
  } catch {
    ElMessage.error('状态更新失败')
  }
}

async function removeDocument(row: KnowledgeDocument) {
  try {
    await ElMessageBox.confirm(`确认删除「${row.title}」及其全部 chunk？`, '删除确认', { type: 'warning' })
    await deleteKnowledgeDocument(row.id)
    ElMessage.success('文档已删除')
    await loadDocuments()
  } catch (err) {
    if (err !== 'cancel') {
      ElMessage.error('删除失败')
    }
  }
}

async function openChunks(row: KnowledgeDocument) {
  currentDocument.value = row
  chunkDrawerVisible.value = true
  chunkLoading.value = true
  try {
    chunks.value = await fetchKnowledgeChunks(row.id)
  } catch {
    ElMessage.error('Chunk 加载失败')
  } finally {
    chunkLoading.value = false
  }
}

async function toggleChunk(row: KnowledgeChunk) {
  chunkTogglingId.value = row.id
  try {
    const updated = await updateKnowledgeChunkEnabled(row.id, row.enabled)
    Object.assign(row, updated)
    await loadDocuments()
  } catch {
    row.enabled = !row.enabled
    ElMessage.error('Chunk 状态更新失败')
  } finally {
    chunkTogglingId.value = null
  }
}

function formatTime(value: string) {
  return new Date(value).toLocaleString()
}
</script>

<style scoped>
.knowledge-page {
  display: grid;
  gap: 16px;
}

.toolbar {
  display: flex;
  justify-content: space-between;
  gap: 18px;
  align-items: center;
  margin-bottom: 18px;
}

.toolbar h2 {
  margin: 0 0 6px;
  color: #111827;
}

.toolbar p {
  margin: 0;
  color: #6b7280;
}

.chunk-preview {
  padding: 14px 0;
  border-bottom: 1px solid #eef2f7;
}

.chunk-preview > div {
  display: flex;
  justify-content: space-between;
  gap: 12px;
  align-items: center;
}

.chunk-preview p {
  margin: 10px 0 0;
  color: #4b5563;
  line-height: 1.7;
  white-space: pre-wrap;
}
</style>
