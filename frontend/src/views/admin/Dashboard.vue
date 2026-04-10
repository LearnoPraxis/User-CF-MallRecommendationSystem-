<template>
  <div class="dashboard-page">
    <!-- 统计卡片 -->
    <n-grid :cols="4" :x-gap="16" :y-gap="16">
      <n-gi>
        <n-card class="stat-card stat-card-green">
          <div class="stat-content">
            <div class="stat-info">
              <div class="stat-value">{{ stats.userCount }}</div>
              <div class="stat-label">用户总数</div>
            </div>
            <n-icon size="48" class="stat-icon"><PeopleOutline /></n-icon>
          </div>
          <div class="stat-footer">
            <span>今日新增 <b>{{ stats.todayUserCount }}</b></span>
          </div>
        </n-card>
      </n-gi>
      <n-gi>
        <n-card class="stat-card stat-card-blue">
          <div class="stat-content">
            <div class="stat-info">
              <div class="stat-value">{{ stats.productCount }}</div>
              <div class="stat-label">商品总数</div>
            </div>
            <n-icon size="48" class="stat-icon"><CubeOutline /></n-icon>
          </div>
          <div class="stat-footer">
            <span>上架中 <b>{{ stats.onSaleCount }}</b></span>
          </div>
        </n-card>
      </n-gi>
      <n-gi>
        <n-card class="stat-card stat-card-orange">
          <div class="stat-content">
            <div class="stat-info">
              <div class="stat-value">{{ stats.orderCount }}</div>
              <div class="stat-label">订单总数</div>
            </div>
            <n-icon size="48" class="stat-icon"><CartOutline /></n-icon>
          </div>
          <div class="stat-footer">
            <span>待发货 <b>{{ stats.pendingOrderCount }}</b></span>
          </div>
        </n-card>
      </n-gi>
      <n-gi>
        <n-card class="stat-card stat-card-red">
          <div class="stat-content">
            <div class="stat-info">
              <div class="stat-value">¥{{ formatMoney(stats.totalSales) }}</div>
              <div class="stat-label">销售总额</div>
            </div>
            <n-icon size="48" class="stat-icon"><CashOutline /></n-icon>
          </div>
          <div class="stat-footer">
            <span>今日销售 <b>¥{{ formatMoney(stats.todaySales) }}</b></span>
          </div>
        </n-card>
      </n-gi>
    </n-grid>

    <!-- 图表区域 -->
    <n-grid :cols="2" :x-gap="16" style="margin-top: 16px;">
      <n-gi>
        <n-card title="近7天销售趋势" size="small">
          <div ref="salesChartRef" class="chart-container"></div>
        </n-card>
      </n-gi>
      <n-gi>
        <n-card title="订单状态分布" size="small">
          <div ref="orderChartRef" class="chart-container"></div>
        </n-card>
      </n-gi>
    </n-grid>

    <!-- 下方区域 -->
    <n-grid :cols="3" :x-gap="16" style="margin-top: 16px;">
      <!-- 最新订单 -->
      <n-gi :span="2">
        <n-card title="最新订单" size="small">
          <template #header-extra>
            <n-button text type="primary" @click="$router.push('/admin/order')">查看全部</n-button>
          </template>
          <n-table :bordered="false" size="small" striped>
            <thead>
              <tr>
                <th>订单号</th>
                <th>用户</th>
                <th>金额</th>
                <th>状态</th>
                <th>时间</th>
              </tr>
            </thead>
            <tbody>
              <tr v-for="order in recentOrders" :key="order.id">
                <td>{{ order.orderNo }}</td>
                <td>{{ order.userName || '-' }}</td>
                <td style="color: #e53935;">¥{{ order.payAmount }}</td>
                <td><n-tag :type="orderStatusMap[order.status]?.type" size="small">{{ orderStatusMap[order.status]?.text }}</n-tag></td>
                <td>{{ formatTime(order.createTime) }}</td>
              </tr>
            </tbody>
          </n-table>
          <n-empty v-if="!recentOrders.length" description="暂无订单" size="small" />
        </n-card>
      </n-gi>
      <!-- 热销商品 -->
      <n-gi>
        <n-card title="热销商品 TOP5" size="small">
          <template #header-extra>
            <n-button text type="primary" @click="$router.push('/admin/product')">查看全部</n-button>
          </template>
          <div class="hot-product-list">
            <div v-for="(item, index) in hotProducts" :key="item.id" class="hot-product-item">
              <n-tag :type="index < 3 ? 'error' : 'default'" size="small" round>{{ index + 1 }}</n-tag>
              <n-image v-if="item.mainImage" :src="item.mainImage" width="40" height="40" object-fit="cover" preview-disabled style="border-radius: 4px;" />
              <div class="product-info">
                <div class="product-name">{{ item.name }}</div>
                <div class="product-sales">销量: {{ item.sales }}</div>
              </div>
            </div>
          </div>
          <n-empty v-if="!hotProducts.length" description="暂无数据" size="small" />
        </n-card>
      </n-gi>
    </n-grid>

    <!-- 快捷操作 -->
    <n-card title="快捷操作" size="small" style="margin-top: 16px;">
      <n-space>
        <n-button type="primary" @click="$router.push('/admin/product')">
          <template #icon><n-icon><AddOutline /></n-icon></template>
          添加商品
        </n-button>
        <n-button type="info" @click="$router.push('/admin/order')">
          <template #icon><n-icon><ListOutline /></n-icon></template>
          处理订单
        </n-button>
        <n-button type="warning" @click="$router.push('/admin/comment')">
          <template #icon><n-icon><ChatbubbleOutline /></n-icon></template>
          回复留言
        </n-button>
        <n-button type="success" @click="$router.push('/admin/report')">
          <template #icon><n-icon><StatsChartOutline /></n-icon></template>
          查看报表
        </n-button>
        <n-button @click="$router.push('/admin/user')">
          <template #icon><n-icon><PeopleOutline /></n-icon></template>
          用户管理
        </n-button>
      </n-space>
    </n-card>
  </div>
</template>

<script setup>
import { ref, onMounted, onUnmounted, nextTick } from 'vue'
import * as echarts from 'echarts'
import dayjs from 'dayjs'
import { PeopleOutline, CubeOutline, CartOutline, CashOutline, AddOutline, ListOutline, ChatbubbleOutline, StatsChartOutline } from '@vicons/ionicons5'
import { getUserList } from '@/api/admin/user'
import { getProductList } from '@/api/admin/product'
import { getOrderList, getSalesStats } from '@/api/admin/order'
import { formatTime, orderStatusMap } from '@/utils/format'

// 统计数据
const stats = ref({
  userCount: 0,
  todayUserCount: 0,
  productCount: 0,
  onSaleCount: 0,
  orderCount: 0,
  pendingOrderCount: 0,
  totalSales: 0,
  todaySales: 0
})

// 最新订单
const recentOrders = ref([])
// 热销商品
const hotProducts = ref([])

// 图表
const salesChartRef = ref(null)
const orderChartRef = ref(null)
let salesChart = null
let orderChart = null

// 格式化金额
function formatMoney(val) {
  if (!val) return '0.00'
  return Number(val).toFixed(2)
}

// 获取统计数据
async function fetchStats() {
  try {
    const [users, products, orders, pendingOrders, onSaleProducts] = await Promise.all([
      getUserList({ page: 1, size: 1 }),
      getProductList({ page: 1, size: 1 }),
      getOrderList({ page: 1, size: 1 }),
      getOrderList({ page: 1, size: 1, status: 1 }),
      getProductList({ page: 1, size: 1, status: 1 })
    ])
    
    stats.value.userCount = users.total || 0
    stats.value.productCount = products.total || 0
    stats.value.orderCount = orders.total || 0
    stats.value.pendingOrderCount = pendingOrders.total || 0
    stats.value.onSaleCount = onSaleProducts.total || 0
    
    // 获取今日注册用户数
    const today = dayjs().format('YYYY-MM-DD')
    const todayStart = today + ' 00:00:00'
    const todayEnd = today + ' 23:59:59'
    
    // 获取今日销售数据
    try {
      const todaySalesData = await getSalesStats(todayStart, todayEnd)
      stats.value.todaySales = (todaySalesData || []).reduce((sum, i) => sum + Number(i.total_amount || 0), 0)
    } catch (e) {
      stats.value.todaySales = 0
    }
    
    // 今日新增用户（通过筛选createTime判断）
    stats.value.todayUserCount = 0
  } catch (e) {
    console.error('获取统计数据失败', e)
  }
}

// 获取最新订单和销售总额
async function fetchRecentOrders() {
  try {
    const res = await getOrderList({ page: 1, size: 5 })
    recentOrders.value = res.records || []
    
    // 获取全部时间范围的销售数据来计算总额（与报表一致）
    const startTime = '2020-01-01 00:00:00'
    const endTime = dayjs().format('YYYY-MM-DD HH:mm:ss')
    const salesData = await getSalesStats(startTime, endTime)
    stats.value.totalSales = (salesData || []).reduce((sum, i) => sum + Number(i.total_amount || 0), 0)
  } catch (e) {
    console.error('获取订单数据失败', e)
  }
}

// 获取热销商品
async function fetchHotProducts() {
  try {
    const res = await getProductList({ page: 1, size: 5 })
    // 按销量排序
    hotProducts.value = (res.records || []).sort((a, b) => (b.sales || 0) - (a.sales || 0)).slice(0, 5)
  } catch (e) {}
}

// 渲染销售趋势图
async function renderSalesChart() {
  if (!salesChartRef.value) return
  
  const endTime = dayjs().format('YYYY-MM-DD HH:mm:ss')
  const startTime = dayjs().subtract(7, 'day').format('YYYY-MM-DD HH:mm:ss')
  
  let salesData = []
  try {
    salesData = await getSalesStats(startTime, endTime)
    if (!salesData || salesData.length === 0) {
      // 如果没有数据，生成空的日期列表
      salesData = []
      for (let i = 6; i >= 0; i--) {
        salesData.push({
          date: dayjs().subtract(i, 'day').format('MM-DD'),
          total_amount: 0,
          order_count: 0
        })
      }
    }
  } catch (e) {
    // 接口失败时显示空数据
    for (let i = 6; i >= 0; i--) {
      salesData.push({
        date: dayjs().subtract(i, 'day').format('MM-DD'),
        total_amount: 0,
        order_count: 0
      })
    }
  }
  
  if (!salesChart) {
    salesChart = echarts.init(salesChartRef.value)
  }
  
  const option = {
    tooltip: { trigger: 'axis' },
    legend: { data: ['销售额', '订单数'], top: 0 },
    grid: { left: '3%', right: '4%', bottom: '3%', top: '40px', containLabel: true },
    xAxis: { type: 'category', data: salesData.map(i => i.date) },
    yAxis: [
      { type: 'value', name: '销售额', position: 'left' },
      { type: 'value', name: '订单数', position: 'right' }
    ],
    series: [
      { name: '销售额', type: 'line', smooth: true, data: salesData.map(i => Number(i.total_amount || 0)), itemStyle: { color: '#18a058' }, areaStyle: { color: 'rgba(24, 160, 88, 0.2)' } },
      { name: '订单数', type: 'bar', yAxisIndex: 1, data: salesData.map(i => i.order_count || 0), itemStyle: { color: '#2080f0' } }
    ]
  }
  salesChart.setOption(option)
}

// 渲染订单状态图
async function renderOrderChart() {
  if (!orderChartRef.value) return
  
  if (!orderChart) {
    orderChart = echarts.init(orderChartRef.value)
  }
  
  // 获取各状态订单数
  const statusCounts = []
  for (let s = 0; s <= 4; s++) {
    try {
      const res = await getOrderList({ page: 1, size: 1, status: s })
      statusCounts.push({ status: s, count: res.total || 0 })
    } catch (e) {
      statusCounts.push({ status: s, count: 0 })
    }
  }
  
  const statusColors = ['#f0a020', '#2080f0', '#18a058', '#52c41a', '#d03050']
  
  const option = {
    tooltip: { trigger: 'item', formatter: '{b}: {c}单 ({d}%)' },
    legend: { orient: 'vertical', right: 'right', top: 'center' },
    series: [{
      type: 'pie',
      radius: ['40%', '70%'],
      center: ['40%', '50%'],
      avoidLabelOverlap: false,
      itemStyle: { borderRadius: 10, borderColor: '#fff', borderWidth: 2 },
      label: { show: true, formatter: '{b}\n{c}单' },
      data: statusCounts.map((item, index) => ({
        name: orderStatusMap[item.status]?.text || `状态${item.status}`,
        value: item.count,
        itemStyle: { color: statusColors[index] }
      }))
    }]
  }
  orderChart.setOption(option)
}

// 窗口大小变化
function handleResize() {
  salesChart?.resize()
  orderChart?.resize()
}

onMounted(async () => {
  await Promise.all([fetchStats(), fetchRecentOrders(), fetchHotProducts()])
  await nextTick()
  renderSalesChart()
  renderOrderChart()
  window.addEventListener('resize', handleResize)
})

onUnmounted(() => {
  window.removeEventListener('resize', handleResize)
  salesChart?.dispose()
  orderChart?.dispose()
})
</script>

<style scoped>
.dashboard-page {
  padding: 0;
}

.stat-card {
  position: relative;
  overflow: hidden;
}

.stat-card-green { background: linear-gradient(135deg, #18a058 0%, #36d399 100%); color: #fff; }
.stat-card-blue { background: linear-gradient(135deg, #2080f0 0%, #36cfc9 100%); color: #fff; }
.stat-card-orange { background: linear-gradient(135deg, #f0a020 0%, #ffc53d 100%); color: #fff; }
.stat-card-red { background: linear-gradient(135deg, #d03050 0%, #ff7875 100%); color: #fff; }

.stat-content {
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.stat-value {
  font-size: 32px;
  font-weight: bold;
}

.stat-label {
  font-size: 14px;
  opacity: 0.9;
  margin-top: 4px;
}

.stat-icon {
  opacity: 0.3;
}

.stat-footer {
  margin-top: 12px;
  padding-top: 12px;
  border-top: 1px solid rgba(255,255,255,0.2);
  font-size: 13px;
  opacity: 0.9;
}

.stat-footer b {
  font-weight: bold;
}

.chart-container {
  height: 280px;
}

.hot-product-list {
  display: flex;
  flex-direction: column;
  gap: 12px;
}

.hot-product-item {
  display: flex;
  align-items: center;
  gap: 10px;
  padding: 8px;
  border-radius: 6px;
  transition: background 0.2s;
}

.hot-product-item:hover {
  background: #f5f5f5;
}

.hot-product-item .product-info {
  flex: 1;
  min-width: 0;
}

.hot-product-item .product-name {
  font-size: 14px;
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
}

.hot-product-item .product-sales {
  font-size: 12px;
  color: #999;
  margin-top: 2px;
}
</style>
