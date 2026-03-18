<template>
  <div class="my-products-page">
    <div class="page-container">
      <n-card title="我的商品">
        <template #header-extra>
          <n-button type="primary" @click="handleAdd">
            <template #icon><n-icon><AddOutline /></n-icon></template>
            发布商品
          </n-button>
        </template>

        <!-- 筛选栏 -->
        <div class="filter-bar">
          <n-space>
            <n-select v-model:value="filterStatus" placeholder="商品状态" clearable style="width: 120px" :options="statusOptions" @update:value="fetchData" />
            <n-input v-model:value="keyword" placeholder="搜索商品名称" clearable style="width: 200px" @keyup.enter="fetchData" />
            <n-button @click="fetchData">搜索</n-button>
          </n-space>
        </div>

        <!-- 商品列表 -->
        <n-table :bordered="false" :single-line="false" v-if="productList.length">
          <thead>
            <tr>
              <th style="width: 80px">图片</th>
              <th>商品名称</th>
              <th style="width: 100px">价格</th>
              <th style="width: 80px">库存</th>
              <th style="width: 80px">销量</th>
              <th style="width: 80px">状态</th>
              <th style="width: 150px">发布时间</th>
              <th style="width: 180px">操作</th>
            </tr>
          </thead>
          <tbody>
            <tr v-for="item in productList" :key="item.id">
              <td>
                <img :src="item.mainImage" class="product-img" @click="goDetail(item.id)" />
              </td>
              <td>
                <div class="product-name" @click="goDetail(item.id)">{{ item.name }}</div>
                <div class="product-category">{{ item.categoryName }}</div>
              </td>
              <td class="price">¥{{ item.price }}</td>
              <td>{{ item.stock }}</td>
              <td>{{ item.sales }}</td>
              <td>
                <n-tag :type="item.status === 1 ? 'success' : 'default'" size="small">
                  {{ item.status === 1 ? '上架中' : '已下架' }}
                </n-tag>
              </td>
              <td>{{ formatTime(item.createTime) }}</td>
              <td>
                <n-space>
                  <n-button text type="primary" size="small" @click="handleEdit(item)">编辑</n-button>
                  <n-button v-if="item.status === 1" text type="warning" size="small" @click="handleStatus(item, 0)">下架</n-button>
                  <n-button v-else text type="success" size="small" @click="handleStatus(item, 1)">上架</n-button>
                  <n-button text type="error" size="small" @click="handleDelete(item)">删除</n-button>
                </n-space>
              </td>
            </tr>
          </tbody>
        </n-table>
        <n-empty v-else description="暂无商品，快去发布吧~" style="padding: 60px 0;">
          <template #extra>
            <n-button type="primary" @click="handleAdd">发布商品</n-button>
          </template>
        </n-empty>

        <!-- 分页 -->
        <div v-if="total > 0" class="pagination">
          <n-pagination v-model:page="page" :page-size="size" :item-count="total" @update:page="fetchData" />
        </div>
      </n-card>
    </div>

    <!-- 发布/编辑商品弹窗 -->
    <n-modal v-model:show="showModal" preset="card" :title="editingProduct ? '编辑商品' : '发布商品'" style="width: 700px" :mask-closable="false">
      <n-form ref="formRef" :model="form" :rules="rules" label-placement="left" label-width="80">
        <n-grid :cols="2" :x-gap="16">
          <n-gi>
            <n-form-item label="商品名称" path="name">
              <n-input v-model:value="form.name" placeholder="请输入商品名称" maxlength="100" />
            </n-form-item>
          </n-gi>
          <n-gi>
            <n-form-item label="商品分类" path="categoryId">
              <n-tree-select v-model:value="form.categoryId" :options="categoryOptions" placeholder="请选择分类" />
            </n-form-item>
          </n-gi>
          <n-gi>
            <n-form-item label="价格" path="price">
              <n-input-number v-model:value="form.price" :min="0" :precision="2" placeholder="请输入价格" style="width: 100%" />
            </n-form-item>
          </n-gi>
          <n-gi>
            <n-form-item label="原价">
              <n-input-number v-model:value="form.originalPrice" :min="0" :precision="2" placeholder="选填" style="width: 100%" />
            </n-form-item>
          </n-gi>
          <n-gi>
            <n-form-item label="库存" path="stock">
              <n-input-number v-model:value="form.stock" :min="0" placeholder="请输入库存" style="width: 100%" />
            </n-form-item>
          </n-gi>
          <n-gi>
            <n-form-item label="状态">
              <n-switch v-model:value="form.status" :checked-value="1" :unchecked-value="0">
                <template #checked>上架</template>
                <template #unchecked>下架</template>
              </n-switch>
            </n-form-item>
          </n-gi>
          <n-gi :span="2">
            <n-form-item label="商品描述">
              <n-input v-model:value="form.description" type="textarea" placeholder="请输入商品描述" :rows="2" />
            </n-form-item>
          </n-gi>
          <n-gi :span="2">
            <n-form-item label="主图" path="mainImage">
              <n-upload :custom-request="handleUpload" :show-file-list="false" accept="image/*">
                <div class="upload-box">
                  <img v-if="form.mainImage" :src="form.mainImage" class="upload-preview" />
                  <div v-else class="upload-placeholder">
                    <n-icon size="24"><ImageOutline /></n-icon>
                    <span>点击上传主图</span>
                  </div>
                </div>
              </n-upload>
            </n-form-item>
          </n-gi>
          <n-gi :span="2">
            <n-form-item label="商品图片">
              <n-upload :custom-request="handleUploadImages" list-type="image-card" v-model:file-list="imageFileList" :max="5" accept="image/*">
                点击上传
              </n-upload>
            </n-form-item>
          </n-gi>
          <n-gi :span="2">
            <n-form-item label="商品详情">
              <n-input v-model:value="form.detail" type="textarea" placeholder="请输入商品详情" :rows="4" />
            </n-form-item>
          </n-gi>
        </n-grid>
      </n-form>
      <template #footer>
        <n-space justify="end">
          <n-button @click="showModal = false">取消</n-button>
          <n-button type="primary" :loading="submitLoading" @click="handleSubmit">
            {{ editingProduct ? '保存' : '发布' }}
          </n-button>
        </n-space>
      </template>
    </n-modal>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import { useMessage, useDialog } from 'naive-ui'
import { AddOutline, ImageOutline } from '@vicons/ionicons5'
import { getMyProducts, publishProduct, updateMyProduct, deleteMyProduct, updateMyProductStatus } from '@/api/myProduct'
import { getCategoryTree } from '@/api/category'
import { uploadFile } from '@/api/upload'
import { formatTime } from '@/utils/format'
import { useUserStore } from '@/store/user'

const router = useRouter()
const message = useMessage()
const dialog = useDialog()
const userStore = useUserStore()

const productList = ref([])
const page = ref(1)
const size = ref(10)
const total = ref(0)
const keyword = ref('')
const filterStatus = ref(null)
const categoryOptions = ref([])

const statusOptions = [
  { label: '上架中', value: 1 },
  { label: '已下架', value: 0 }
]

// 弹窗相关
const showModal = ref(false)
const editingProduct = ref(null)
const formRef = ref(null)
const submitLoading = ref(false)
const imageFileList = ref([])

const form = ref({
  name: '',
  categoryId: null,
  price: null,
  originalPrice: null,
  stock: null,
  mainImage: '',
  images: '',
  description: '',
  detail: '',
  status: 1
})

const rules = {
  name: { required: true, message: '请输入商品名称', trigger: 'blur' },
  categoryId: { required: true, type: 'number', message: '请选择分类', trigger: 'change' },
  price: { required: true, type: 'number', message: '请输入价格', trigger: 'blur' },
  stock: { required: true, type: 'number', message: '请输入库存', trigger: 'blur' },
  mainImage: { required: true, message: '请上传主图', trigger: 'change' }
}

function goDetail(id) {
  router.push(`/product/${id}`)
}

async function fetchData() {
  try {
    const res = await getMyProducts({
      page: page.value,
      size: size.value,
      keyword: keyword.value || undefined,
      status: filterStatus.value
    })
    productList.value = res.records || []
    total.value = res.total || 0
  } catch (e) {}
}

async function fetchCategories() {
  try {
    const categories = await getCategoryTree()
    categoryOptions.value = categories.map(c => ({
      label: c.name,
      key: c.id,
      children: c.children?.map(cc => ({ label: cc.name, key: cc.id }))
    }))
  } catch (e) {}
}

function handleAdd() {
  editingProduct.value = null
  form.value = {
    name: '',
    categoryId: null,
    price: null,
    originalPrice: null,
    stock: null,
    mainImage: '',
    images: '',
    description: '',
    detail: '',
    status: 1
  }
  imageFileList.value = []
  showModal.value = true
}

function handleEdit(item) {
  editingProduct.value = item
  form.value = {
    id: item.id,
    name: item.name,
    categoryId: item.categoryId,
    price: item.price,
    originalPrice: item.originalPrice,
    stock: item.stock,
    mainImage: item.mainImage,
    images: item.images || '',
    description: item.description || '',
    detail: item.detail || '',
    status: item.status
  }
  // 处理图片列表
  if (item.images) {
    imageFileList.value = item.images.split(',').filter(Boolean).map((url, idx) => ({
      id: idx,
      name: `image-${idx}`,
      status: 'finished',
      url
    }))
  } else {
    imageFileList.value = []
  }
  showModal.value = true
}

async function handleUpload({ file, onFinish, onError }) {
  try {
    const url = await uploadFile(file.file)
    form.value.mainImage = url
    onFinish()
  } catch (e) {
    message.error('上传失败')
    onError()
  }
}

async function handleUploadImages({ file, onFinish, onError }) {
  try {
    const url = await uploadFile(file.file)
    file.url = url
    onFinish()
  } catch (e) {
    message.error('上传失败')
    onError()
  }
}

async function handleSubmit() {
  try {
    await formRef.value?.validate()
  } catch (e) {
    return
  }

  // 处理图片
  form.value.images = imageFileList.value.map(f => f.url).filter(Boolean).join(',')

  submitLoading.value = true
  try {
    if (editingProduct.value) {
      await updateMyProduct(form.value)
      message.success('保存成功')
    } else {
      await publishProduct(form.value)
      message.success('发布成功')
    }
    showModal.value = false
    fetchData()
  } catch (e) {
    message.error(e.message || '操作失败')
  } finally {
    submitLoading.value = false
  }
}

function handleStatus(item, status) {
  const text = status === 1 ? '上架' : '下架'
  dialog.info({
    title: '确认操作',
    content: `确定${text}该商品吗？`,
    positiveText: '确定',
    negativeText: '取消',
    onPositiveClick: async () => {
      await updateMyProductStatus(item.id, status)
      message.success(`${text}成功`)
      fetchData()
    }
  })
}

function handleDelete(item) {
  dialog.warning({
    title: '确认删除',
    content: '确定删除该商品吗？删除后无法恢复。',
    positiveText: '确定删除',
    negativeText: '取消',
    onPositiveClick: async () => {
      await deleteMyProduct(item.id)
      message.success('删除成功')
      fetchData()
    }
  })
}

onMounted(() => {
  if (!userStore.isLogin) {
    message.warning('请先登录')
    router.push('/login?redirect=/my-products')
    return
  }
  fetchData()
  fetchCategories()
})
</script>

<style scoped>
.my-products-page { padding: 20px; background: #f5f7fa; min-height: calc(100vh - 60px); }
.page-container { max-width: 1200px; margin: 0 auto; }

.filter-bar { margin-bottom: 16px; }

.product-img { width: 60px; height: 60px; object-fit: cover; border-radius: 4px; cursor: pointer; }
.product-name { cursor: pointer; color: #333; }
.product-name:hover { color: #18a058; }
.product-category { font-size: 12px; color: #999; margin-top: 4px; }
.price { color: #e53935; font-weight: bold; }

.pagination { margin-top: 20px; display: flex; justify-content: center; }

/* 上传样式 */
.upload-box { width: 120px; height: 120px; border: 1px dashed #d9d9d9; border-radius: 4px; cursor: pointer; display: flex; align-items: center; justify-content: center; overflow: hidden; }
.upload-box:hover { border-color: #18a058; }
.upload-preview { width: 100%; height: 100%; object-fit: cover; }
.upload-placeholder { display: flex; flex-direction: column; align-items: center; gap: 8px; color: #999; font-size: 12px; }
</style>
