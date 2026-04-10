<template>
  <div class="order-detail-page">
    <div class="page-container">
      <n-card v-if="order" title="订单详情">
        <template #header-extra>
          <n-tag :type="orderStatusMap[order.status]?.type" size="large">{{ orderStatusMap[order.status]?.text }}</n-tag>
        </template>
        <n-descriptions :column="2" label-placement="left" bordered>
          <n-descriptions-item label="订单号">{{ order.orderNo }}</n-descriptions-item>
          <n-descriptions-item label="下单时间">{{ formatTime(order.createTime) }}</n-descriptions-item>
          <n-descriptions-item label="收货人">{{ order.receiverName }}</n-descriptions-item>
          <n-descriptions-item label="联系电话">{{ order.receiverPhone }}</n-descriptions-item>
          <n-descriptions-item label="收货地址" :span="2">{{ order.receiverAddress }}</n-descriptions-item>
          <n-descriptions-item v-if="order.remark" label="备注" :span="2">{{ order.remark }}</n-descriptions-item>
          <n-descriptions-item v-if="order.payTime" label="支付时间">{{ formatTime(order.payTime) }}</n-descriptions-item>
          <n-descriptions-item v-if="order.deliverTime" label="发货时间">{{ formatTime(order.deliverTime) }}</n-descriptions-item>
          <n-descriptions-item v-if="order.receiveTime" label="收货时间">{{ formatTime(order.receiveTime) }}</n-descriptions-item>
        </n-descriptions>
        <n-divider>商品信息</n-divider>
        <n-table :bordered="false">
          <thead>
            <tr><th>商品</th><th style="width: 120px">单价</th><th style="width: 100px">数量</th><th style="width: 120px">小计</th></tr>
          </thead>
          <tbody>
            <tr v-for="item in order.orderItems" :key="item.id">
              <td>
                <div class="product-cell">
                  <img :src="item.productImage" class="product-img" />
                  <span>{{ item.productName }}</span>
                </div>
              </td>
              <td>¥{{ item.price }}</td>
              <td>{{ item.quantity }}</td>
              <td class="price">¥{{ item.totalPrice }}</td>
            </tr>
          </tbody>
        </n-table>
        <div class="order-total">
          <span>订单总额：<b>¥{{ order.totalAmount }}</b></span>
          <span>实付金额：<b class="pay-amount">¥{{ order.payAmount }}</b></span>
        </div>
        <div class="order-actions">
          <n-button v-if="order.status === 0" type="primary" @click="handlePay">去支付</n-button>
          <n-button v-if="order.status === 0" @click="handleCancel">取消订单</n-button>
          <n-button v-if="order.status === 2" type="primary" @click="handleReceive">确认收货</n-button>
          <n-button @click="router.push('/order')">返回列表</n-button>
        </div>
      </n-card>
    </div>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { useMessage, useDialog } from 'naive-ui'
import { getOrderDetail, payOrder, cancelOrder, receiveOrder } from '@/api/order'
import { formatTime, orderStatusMap } from '@/utils/format'

const route = useRoute()
const router = useRouter()
const message = useMessage()
const dialog = useDialog()

const order = ref(null)

async function fetchData() {
  order.value = await getOrderDetail(route.params.id)
}

async function handlePay() {
  dialog.info({
    title: '支付确认', content: `确认支付 ¥${order.value.payAmount} 吗？`, positiveText: '确认支付', negativeText: '取消',
    onPositiveClick: async () => { await payOrder(order.value.id); message.success('支付成功'); fetchData() }
  })
}

async function handleCancel() {
  dialog.warning({
    title: '取消订单', content: '确定取消该订单吗？', positiveText: '确定', negativeText: '取消',
    onPositiveClick: async () => { await cancelOrder(order.value.id); message.success('已取消'); fetchData() }
  })
}

async function handleReceive() {
  dialog.info({
    title: '确认收货', content: '确认已收到商品吗？', positiveText: '确认', negativeText: '取消',
    onPositiveClick: async () => { await receiveOrder(order.value.id); message.success('已确认收货'); fetchData() }
  })
}

onMounted(fetchData)
</script>

<style scoped>
.order-detail-page { padding: 20px; }
.page-container { max-width: 900px; margin: 0 auto; }
.product-cell { display: flex; align-items: center; gap: 12px; }
.product-img { width: 60px; height: 60px; object-fit: cover; border-radius: 4px; }
.price { color: #e53935; font-weight: bold; }
.order-total { display: flex; justify-content: flex-end; gap: 30px; margin-top: 20px; font-size: 16px; }
.pay-amount { font-size: 24px; color: #e53935; }
.order-actions { display: flex; justify-content: center; gap: 16px; margin-top: 30px; }
</style>
