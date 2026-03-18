<template>
  <div class="cart-page">
    <div class="page-container">
      <n-card title="我的购物车">
        <template #header-extra>
          <span class="cart-count">共 {{ cartList.length }} 件商品</span>
        </template>
        <template v-if="cartList.length">
          <n-table :bordered="false" :single-line="false">
            <thead>
              <tr>
                <th style="width: 50px"><n-checkbox v-model:checked="isAllSelected" @update:checked="handleSelectAll" /></th>
                <th>商品信息</th>
                <th style="width: 120px">单价</th>
                <th style="width: 150px">数量</th>
                <th style="width: 120px">小计</th>
                <th style="width: 100px">操作</th>
              </tr>
            </thead>
            <tbody>
              <tr v-for="item in cartList" :key="item.id" :class="{ 'invalid-item': !item.product || item.product.status !== 1 }">
                <td>
                  <n-checkbox 
                    :checked="item.selected === 1" 
                    :disabled="!item.product || item.product.status !== 1 || item.product.stock <= 0"
                    @update:checked="(v) => handleSelect(item, v)" 
                  />
                </td>
                <td>
                  <div class="product-cell">
                    <div class="product-img-wrap" @click="goDetail(item.productId)">
                      <img :src="item.product?.mainImage" class="product-img" />
                      <span v-if="!item.product || item.product.status !== 1" class="invalid-tag">已下架</span>
                      <span v-else-if="item.product.stock <= 0" class="invalid-tag">已售罄</span>
                    </div>
                    <div class="product-info">
                      <div class="product-name" @click="goDetail(item.productId)">{{ item.product?.name || '商品已删除' }}</div>
                      <div v-if="item.product" class="product-stock">
                        <span v-if="item.product.stock > 10" class="stock-normal">库存充足</span>
                        <span v-else-if="item.product.stock > 0" class="stock-low">仅剩{{ item.product.stock }}件</span>
                        <span v-else class="stock-out">已售罄</span>
                      </div>
                    </div>
                  </div>
                </td>
                <td class="price">¥{{ item.product?.price || 0 }}</td>
                <td>
                  <n-input-number 
                    v-model:value="item.quantity" 
                    :min="1" 
                    :max="item.product?.stock || 1" 
                    size="small"
                    :disabled="!item.product || item.product.stock <= 0"
                    @update:value="(v) => handleQuantityChange(item, v)" 
                  />
                </td>
                <td class="price">¥{{ ((item.product?.price || 0) * item.quantity).toFixed(2) }}</td>
                <td><n-button text type="error" @click="handleDelete(item.id)">删除</n-button></td>
              </tr>
            </tbody>
          </n-table>
          <div class="cart-footer">
            <div class="footer-left">
              <n-button text type="error" @click="handleClear">清空购物车</n-button>
              <n-button v-if="invalidCount > 0" text type="warning" @click="handleClearInvalid">清除失效商品({{ invalidCount }})</n-button>
            </div>
            <div class="cart-total">
              <span>已选 <b>{{ selectedCount }}</b> 件商品</span>
              <span>合计：<b class="total-price">¥{{ totalPrice }}</b></span>
              <n-button type="primary" size="large" :disabled="!selectedCount" @click="showOrderModal = true">去结算</n-button>
            </div>
          </div>
        </template>
        <n-empty v-else description="购物车是空的" style="padding: 60px 0;">
          <template #extra>
            <n-button type="primary" @click="router.push('/product')">去逛逛</n-button>
          </template>
        </n-empty>
      </n-card>
    </div>
    <!-- 下单弹窗 -->
    <n-modal v-model:show="showOrderModal" preset="card" title="确认订单" style="width: 550px" :mask-closable="false">
      <div class="order-summary">
        <div class="summary-item">
          <span>商品数量</span>
          <span>{{ selectedCount }} 件</span>
        </div>
        <div class="summary-item">
          <span>商品金额</span>
          <span class="price">¥{{ totalPrice }}</span>
        </div>
      </div>
      <n-divider />
      <n-form ref="orderFormRef" :model="orderForm" :rules="orderRules" label-placement="left" label-width="80">
        <n-form-item label="收货人" path="receiverName">
          <n-input v-model:value="orderForm.receiverName" placeholder="请输入收货人姓名" />
        </n-form-item>
        <n-form-item label="联系电话" path="receiverPhone">
          <n-input v-model:value="orderForm.receiverPhone" placeholder="请输入联系电话" maxlength="11" />
        </n-form-item>
        <n-form-item label="收货地址" path="receiverAddress">
          <n-input v-model:value="orderForm.receiverAddress" type="textarea" placeholder="请输入详细地址" :rows="2" />
        </n-form-item>
        <n-form-item label="订单备注">
          <n-input v-model:value="orderForm.remark" placeholder="选填，可填写特殊要求" />
        </n-form-item>
      </n-form>
      <template #footer>
        <n-space justify="end">
          <n-button @click="showOrderModal = false">取消</n-button>
          <n-button type="primary" :loading="orderLoading" @click="handleCreateOrder">提交订单 (¥{{ totalPrice }})</n-button>
        </n-space>
      </template>
    </n-modal>
  </div>
</template>

<script setup>
import { ref, computed, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import { useMessage, useDialog } from 'naive-ui'
import { getCartList, updateCartQuantity, updateCartSelected, selectAllCart, deleteCartItem, clearCart } from '@/api/cart'
import { createOrder } from '@/api/order'
import { useUserStore } from '@/store/user'

const router = useRouter()
const message = useMessage()
const dialog = useDialog()
const userStore = useUserStore()

const cartList = ref([])
const showOrderModal = ref(false)
const orderLoading = ref(false)
const orderFormRef = ref(null)
const orderForm = ref({ receiverName: '', receiverPhone: '', receiverAddress: '', remark: '' })
const orderRules = {
  receiverName: { required: true, message: '请输入收货人', trigger: 'blur' },
  receiverPhone: [
    { required: true, message: '请输入联系电话', trigger: 'blur' },
    { pattern: /^1[3-9]\d{9}$/, message: '请输入正确的手机号', trigger: 'blur' }
  ],
  receiverAddress: { required: true, message: '请输入收货地址', trigger: 'blur' }
}

// 有效商品（上架且有库存）
const validItems = computed(() => cartList.value.filter(item => item.product && item.product.status === 1 && item.product.stock > 0))
// 失效商品数量
const invalidCount = computed(() => cartList.value.length - validItems.value.length)
// 是否全选
const isAllSelected = computed(() => validItems.value.length > 0 && validItems.value.every(item => item.selected === 1))
// 已选数量
const selectedCount = computed(() => cartList.value.filter(item => item.selected === 1 && item.product && item.product.status === 1 && item.product.stock > 0).length)
// 总价
const totalPrice = computed(() => {
  return cartList.value
    .filter(item => item.selected === 1 && item.product && item.product.status === 1 && item.product.stock > 0)
    .reduce((sum, item) => sum + (item.product?.price || 0) * item.quantity, 0)
    .toFixed(2)
})

function goDetail(id) { router.push(`/product/${id}`) }

async function fetchData() {
  try {
    cartList.value = await getCartList() || []
  } catch (e) {}
}

async function handleSelect(item, checked) {
  await updateCartSelected(item.id, checked ? 1 : 0)
  item.selected = checked ? 1 : 0
}

async function handleSelectAll(checked) {
  await selectAllCart(checked ? 1 : 0)
  cartList.value.forEach(item => {
    if (item.product && item.product.status === 1 && item.product.stock > 0) {
      item.selected = checked ? 1 : 0
    }
  })
}

async function handleQuantityChange(item, value) {
  if (value && value > 0) {
    await updateCartQuantity(item.id, value)
  }
}

async function handleDelete(id) {
  dialog.warning({
    title: '提示',
    content: '确定删除该商品吗？',
    positiveText: '确定',
    negativeText: '取消',
    onPositiveClick: async () => {
      await deleteCartItem(id)
      fetchData()
    }
  })
}

async function handleClear() {
  dialog.warning({
    title: '提示',
    content: '确定清空购物车吗？',
    positiveText: '确定',
    negativeText: '取消',
    onPositiveClick: async () => {
      await clearCart()
      fetchData()
    }
  })
}

async function handleClearInvalid() {
  dialog.warning({
    title: '提示',
    content: `确定清除 ${invalidCount.value} 件失效商品吗？`,
    positiveText: '确定',
    negativeText: '取消',
    onPositiveClick: async () => {
      const invalidIds = cartList.value
        .filter(item => !item.product || item.product.status !== 1 || item.product.stock <= 0)
        .map(item => item.id)
      for (const id of invalidIds) {
        await deleteCartItem(id)
      }
      message.success('已清除失效商品')
      fetchData()
    }
  })
}

async function handleCreateOrder() {
  try {
    await orderFormRef.value?.validate()
    orderLoading.value = true
    const order = await createOrder(orderForm.value)
    message.success('下单成功')
    showOrderModal.value = false
    // 更新购物车数量
    window.dispatchEvent(new CustomEvent('cart-updated'))
    router.push(`/order/${order.id}`)
  } catch (e) {
    // 验证失败
  } finally {
    orderLoading.value = false
  }
}

onMounted(() => {
  if (!userStore.isLogin) {
    message.warning('请先登录')
    router.push('/login?redirect=/cart')
    return
  }
  fetchData()
})
</script>

<style scoped>
.cart-page { padding: 20px; }
.page-container { max-width: 1200px; margin: 0 auto; }

.cart-count { font-size: 14px; color: #666; }

.invalid-item { background: #fafafa; }

.product-cell { display: flex; align-items: center; gap: 12px; }
.product-img-wrap { position: relative; flex-shrink: 0; }
.product-img { width: 80px; height: 80px; object-fit: cover; border-radius: 4px; cursor: pointer; }
.invalid-tag { position: absolute; top: 0; left: 0; right: 0; bottom: 0; background: rgba(0,0,0,0.6); color: #fff; display: flex; align-items: center; justify-content: center; font-size: 12px; border-radius: 4px; }

.product-info { flex: 1; min-width: 0; }
.product-name { cursor: pointer; overflow: hidden; text-overflow: ellipsis; white-space: nowrap; }
.product-name:hover { color: #18a058; }
.product-stock { font-size: 12px; margin-top: 4px; }
.stock-normal { color: #18a058; }
.stock-low { color: #f0a020; }
.stock-out { color: #d03050; }

.price { color: #e53935; font-weight: bold; }

.cart-footer { display: flex; justify-content: space-between; align-items: center; margin-top: 20px; padding-top: 20px; border-top: 1px solid #eee; }
.footer-left { display: flex; gap: 16px; }
.cart-total { display: flex; align-items: center; gap: 24px; }
.total-price { font-size: 24px; color: #e53935; }

.order-summary { background: #f9f9f9; padding: 16px; border-radius: 8px; }
.summary-item { display: flex; justify-content: space-between; padding: 8px 0; }
.summary-item .price { color: #e53935; font-weight: bold; }
</style>
