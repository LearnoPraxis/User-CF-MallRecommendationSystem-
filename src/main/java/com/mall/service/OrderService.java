package com.mall.service;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.service.IService;
import com.mall.dto.OrderDTO;
import com.mall.entity.OrderInfo;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

/**
 * 订单服务接口
 */
public interface OrderService extends IService<OrderInfo> {

    /**
     * 创建订单
     */
    OrderInfo createOrder(OrderDTO dto);

    /**
     * 分页查询用户订单
     */
    IPage<OrderInfo> pageUserOrders(Integer page, Integer size, Integer status);

    /**
     * 分页查询所有订单（管理端）
     */
    IPage<OrderInfo> pageAllOrders(Integer page, Integer size, Integer status, String orderNo);

    /**
     * 获取订单详情
     */
    OrderInfo getOrderDetail(Long id);

    /**
     * 支付订单
     */
    void payOrder(Long id);

    /**
     * 发货
     */
    void deliverOrder(Long id);

    /**
     * 确认收货
     */
    void receiveOrder(Long id);

    /**
     * 取消订单
     */
    void cancelOrder(Long id);

    /**
     * 删除订单
     */
    void deleteOrder(Long id);

    /**
     * 修改订单备注
     */
    void updateOrderRemark(Long id, String remark);

    /**
     * 统计销售数据（按日期）
     */
    List<Map<String, Object>> getSalesStatsByDate(LocalDateTime startTime, LocalDateTime endTime);

    /**
     * 统计商品销量排行
     */
    List<Map<String, Object>> getProductSalesRank(LocalDateTime startTime, LocalDateTime endTime, Integer limit);

    /**
     * 统计分类销售占比
     */
    List<Map<String, Object>> getCategorySalesRatio(LocalDateTime startTime, LocalDateTime endTime);

    /**
     * 统计订单状态分布
     */
    List<Map<String, Object>> getOrderStatusStats(LocalDateTime startTime, LocalDateTime endTime);
}
