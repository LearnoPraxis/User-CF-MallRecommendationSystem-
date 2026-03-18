<template>
  <div class="order-list-page">
    <div class="page-container">
      <n-card title="我的订单">
        <template #header-extra>
          <n-radio-group v-model:value="status" @update:value="handleStatusChange">
            <n-radio-button :value="null">全部</n-radio-button>
            <n-radio-button :value="0">待付款</n-radio-button>
            <n-radio-button :value="1">待发货</n-radio-button>
            <n-radio-button :value="2">待收货</n-radio-button>
            <n-radio-button :value="3">已完成</n-radio-button>
            <n-radio-button :value="4">已取消</n-radio-button>
          </n-radio-group>
        </template>
        <div v-if="orderList.length" class="order-list">
          <div v-for="order in orderList" :key="order.id" class="order-item">
            <div class="order-header">
              <div class="order-header-left">
                <span class="order-no">订单号：{{ order.orderNo }}</span>
                <span class="order-time">{{ formatTime(order.createTime) }}</span>
              </div>
              <n-tag :type="orderStatusMap[order.status]?.type" size="small">{{ orderStatusMap[order.status]?.text }}</n-tag>
            </div>
            <div class="order-body">
              <!-- 商品列表 -->
              <div class="order-products">
                <div v-for="item in (order.orderItems || [])" :key="item.id" class="product-item" @click="goProductDetail(item.productId)">
                  <img :src="item.productImage" class="product-img" />
                  <div class="product-info">
                    <div class="product-name">{{ item.productName }}</div>
                    <div class="product-price-qty">
                      <span class="product-price">¥{{ item.price }}</span>
                      <span class="product-qty">× {{ item.quantity }}</span>
                    </div>
                    <div class="product-subtotal">小计：¥{{ (item.price * item.quantity).toFixed(2) }}</div>
                  </div>
                </div>
              </div>
              <!-- 订单信息 -->
              <div class="order-info">
                <div class="info-section">
                  <div class="info-title">收货信息</div>
                  <div class="receiver">
                    <n-icon><PersonOutline /></n-icon>
                    <span>{{ order.receiverName }}</span>
                  </div>
                  <div class="phone">
                    <n-icon><CallOutline /></n-icon>
                    <span>{{ order.receiverPhone }}</span>
                  </div>
                  <div class="address">
                    <n-icon><LocationOutline /></n-icon>
                    <span>{{ order.receiverAddress }}</span>
                  </div>
                </div>
                <div v-if="order.remark" class="info-section">
                  <div class="info-title">订单备注</div>
                  <div class="remark">{{ order.remark }}</div>
                </div>
              </div>
              <!-- 金额 -->
              <div class="order-amount">
                <div class="amount-row">
                  <span>商品总数</span>
                  <span>{{ getTotalQuantity(order) }}件</span>
                </div>
                <div class="amount-row">
                  <span>商品金额</span>
                  <span>¥{{ order.totalAmount }}</span>
                </div>
                <div v-if="order.discountAmount > 0" class="amount-row discount">
                  <span>优惠</span>
                  <span>-¥{{ order.discountAmount }}</span>
                </div>
                <div class="amount-row total">
                  <span>实付款</span>
                  <span class="pay-amount">¥{{ order.payAmount }}</span>
                </div>
              </div>
            </div>
            <div class="order-footer">
              <div class="footer-left">
                <span v-if="order.payTime" class="time-info">支付时间：{{ formatTime(order.payTime) }}</span>
                <span v-if="order.deliveryTime" class="time-info">发货时间：{{ formatTime(order.deliveryTime) }}</span>
                <span v-if="order.receiveTime" class="time-info">收货时间：{{ formatTime(order.receiveTime) }}</span>
              </div>
              <div class="footer-right">
                <n-button v-if="order.status === 0" type="primary" size="small" @click="handlePay(order)">立即支付</n-button>
                <n-button v-if="order.status === 0" size="small" @click="handleCancel(order)">取消订单</n-button>
                <n-button v-if="order.status === 2" type="primary" size="small" @click="handleReceive(order)">确认收货</n-button>
                <n-button v-if="order.status === 3" type="warning" size="small" @click="handleComment(order)">
                  <template #icon><n-icon><ChatbubbleOutline /></n-icon></template>
                  去评价
                </n-button>
                <n-button size="small" @click="goDetail(order.id)">订单详情</n-button>
              </div>
            </div>
          </div>
        </div>
        <n-empty v-else description="暂无订单" style="padding: 60px 0;">
          <template #extra>
            <n-button type="primary" @click="router.push('/product')">去逛逛</n-button>
          </template>
        </n-empty>
        <div v-if="total > 0" class="pagination">
          <n-pagination v-model:page="page" :page-size="size" :item-count="total" @update:page="fetchData" />
        </div>
      </n-card>
    </div>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import { useMessage, useDialog } from 'naive-ui'
import { LocationOutline, PersonOutline, CallOutline, ChatbubbleOutline } from '@vicons/ionicons5'
import { getOrderList, payOrder, cancelOrder, receiveOrder } from '@/api/order'
import { formatTime, orderStatusMap } from '@/utils/format'
import { useUserStore } from '@/store/user'

const router = useRouter()
const message = useMessage()
const dialog = useDialog()
const userStore = useUserStore()

const orderList = ref([])
const status = ref(null)
const page = ref(1)
const size = ref(10)
const total = ref(0)

// 计算订单商品总数
function getTotalQuantity(order) {
  return (order.orderItems || []).reduce((sum, item) => sum + item.quantity, 0)
}

function goDetail(id) { router.push(`/order/${id}`) }
function goProductDetail(id) { router.push(`/product/${id}`) }

function handleStatusChange() { page.value = 1; fetchData() }

async function fetchData() {
  try {
    const res = await getOrderList({ page: page.value, size: size.value, status: status.value })
    orderList.value = res.records || []
    total.value = res.total || 0
  } catch (e) {}
}

async function handlePay(order) {
  dialog.info({
    title: '支付确认',
    content: `确认支付 ¥${order.payAmount} 吗？`,
    positiveText: '确认支付',
    negativeText: '取消',
    onPositiveClick: async () => {
      await payOrder(order.id)
      message.success('支付成功')
      fetchData()
    }
  })
}

async function handleCancel(order) {
  dialog.warning({
    title: '取消订单',
    content: '确定取消该订单吗？',
    positiveText: '确定',
    negativeText: '取消',
    onPositiveClick: async () => {
      await cancelOrder(order.id)
      message.success('已取消')
      fetchData()
    }
  })
}

async function handleReceive(order) {
  dialog.info({
    title: '确认收货',
    content: '确认已收到商品吗？',
    positiveText: '确认',
    negativeText: '取消',
    onPositiveClick: async () => {
      await receiveOrder(order.id)
      message.success('已确认收货')
      fetchData()
    }
  })
}

function handleComment(order) {
  // 跳转到第一个商品详情页进行评价
  if (order.orderItems && order.orderItems.length > 0) {
    router.push(`/product/${order.orderItems[0].productId}#comment`)
  } else {
    message.warning('该订单没有可评价的商品')
  }
}

// 检查登录状态
onMounted(() => {
  if (!userStore.isLogin) {
    message.warning('请先登录')
    router.push('/login?redirect=/order')
    return
  }
  fetchData()
})
</script>

<style scoped>
.order-list-page { padding: 20px; background: #f5f7fa; min-height: calc(100vh - 60px); }
.page-container { max-width: 1200px; margin: 0 auto; }

.order-list { display: flex; flex-direction: column; gap: 16px; }

.order-item { border: 1px solid #e8e8e8; border-radius: 8px; overflow: hidden; background: #fff; }

.order-header { display: flex; align-items: center; justify-content: space-between; padding: 12px 16px; background: #fafafa; border-bottom: 1px solid #eee; }
.order-header-left { display: flex; align-items: center; gap: 16px; }
.order-no { font-size: 14px; color: #333; font-weight: 500; }
.order-time { font-size: 13px; color: #999; }

.order-body { display: flex; padding: 16px; gap: 16px; }

.order-products { width: 45%; display: flex; flex-direction: column; gap: 12px; border-right: 1px solid #f0f0f0; padding-right: 16px; }
.product-item { display: flex; gap: 12px; cursor: pointer; padding: 8px; border-radius: 6px; transition: background 0.2s; }
.product-item:hover { background: #f9f9f9; }
.product-item:hover .product-name { color: #18a058; }
.product-img { width: 60px; height: 60px; object-fit: cover; border-radius: 6px; flex-shrink: 0; }
.product-info { flex: 1; min-width: 0; display: flex; flex-direction: column; justify-content: space-between; }
.product-name { font-size: 14px; color: #333; overflow: hidden; text-overflow: ellipsis; white-space: nowrap; }
.product-price-qty { display: flex; align-items: center; gap: 8px; }
.product-price { font-size: 14px; color: #e53935; }
.product-qty { font-size: 13px; color: #999; }
.product-subtotal { font-size: 12px; color: #666; }

.order-info { width: 35%; padding: 0 16px; border-right: 1px solid #f0f0f0; }
.info-section { margin-bottom: 12px; }
.info-section:last-child { margin-bottom: 0; }
.info-title { font-size: 13px; color: #999; margin-bottom: 8px; }
.receiver, .phone, .address { display: flex; align-items: flex-start; gap: 6px; font-size: 13px; color: #333; margin-bottom: 4px; }
.address { line-height: 1.4; }
.remark { font-size: 13px; color: #666; line-height: 1.4; }

.order-amount { width: 20%; padding-left: 16px; }
.amount-row { display: flex; justify-content: space-between; font-size: 13px; color: #666; margin-bottom: 8px; }
.amount-row.discount { color: #18a058; }
.amount-row.total { margin-top: 12px; padding-top: 12px; border-top: 1px dashed #eee; font-weight: 500; }
.pay-amount { font-size: 18px; color: #e53935; font-weight: bold; }

.order-footer { display: flex; justify-content: space-between; align-items: center; padding: 12px 16px; border-top: 1px solid #eee; background: #fafafa; }
.footer-left { display: flex; gap: 16px; flex-wrap: wrap; }
.time-info { font-size: 12px; color: #999; }
.footer-right { display: flex; gap: 8px; }

.pagination { margin-top: 20px; display: flex; justify-content: center; }
</style>
