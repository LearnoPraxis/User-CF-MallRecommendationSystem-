package com.mall.service.impl;

import cn.hutool.core.util.IdUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.mall.dto.OrderDTO;
import com.mall.entity.Cart;
import com.mall.entity.OrderInfo;
import com.mall.entity.OrderItem;
import com.mall.entity.Product;
import com.mall.exception.BusinessException;
import com.mall.mapper.OrderInfoMapper;
import com.mall.mapper.OrderItemMapper;
import com.mall.security.SecurityUtil;
import com.mall.service.CartService;
import com.mall.service.OrderService;
import com.mall.service.ProductService;
import com.mall.service.RecommendService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

/**
 * 订单服务实现类
 */
@Service
public class OrderServiceImpl extends ServiceImpl<OrderInfoMapper, OrderInfo> implements OrderService {

    @Autowired
    private OrderItemMapper orderItemMapper;

    @Autowired
    private CartService cartService;

    @Autowired
    private ProductService productService;

    @Autowired
    private RecommendService recommendService;

    @Override
    @Transactional
    public OrderInfo createOrder(OrderDTO dto) {
        Long userId = SecurityUtil.getCurrentUserId();
        // 获取选中的购物车项
        List<Cart> selectedCarts = cartService.getSelectedCarts();
        if (selectedCarts.isEmpty()) {
            throw new BusinessException("请选择要购买的商品");
        }
        // 计算总金额
        BigDecimal totalAmount = BigDecimal.ZERO;
        for (Cart cart : selectedCarts) {
            Product product = cart.getProduct();
            if (product == null || product.getStatus() != 1) {
                throw new BusinessException("商品[" + (product != null ? product.getName() : "") + "]已下架");
            }
            if (product.getStock() < cart.getQuantity()) {
                throw new BusinessException("商品[" + product.getName() + "]库存不足");
            }
            totalAmount = totalAmount.add(product.getPrice().multiply(new BigDecimal(cart.getQuantity())));
        }
        // 创建订单
        OrderInfo order = new OrderInfo();
        order.setOrderNo(IdUtil.getSnowflakeNextIdStr());
        order.setUserId(userId);
        order.setTotalAmount(totalAmount);
        order.setPayAmount(totalAmount);
        order.setStatus(0); // 待付款
        order.setReceiverName(dto.getReceiverName());
        order.setReceiverPhone(dto.getReceiverPhone());
        order.setReceiverAddress(dto.getReceiverAddress());
        order.setRemark(dto.getRemark());
        this.save(order);
        // 创建订单明细
        for (Cart cart : selectedCarts) {
            Product product = cart.getProduct();
            OrderItem item = new OrderItem();
            item.setOrderId(order.getId());
            item.setProductId(product.getId());
            item.setProductName(product.getName());
            item.setProductImage(product.getMainImage());
            item.setPrice(product.getPrice());
            item.setQuantity(cart.getQuantity());
            item.setTotalPrice(product.getPrice().multiply(new BigDecimal(cart.getQuantity())));
            orderItemMapper.insert(item);
            // 扣减库存
            Product updateProduct = new Product();
            updateProduct.setId(product.getId());
            updateProduct.setStock(product.getStock() - cart.getQuantity());
            productService.updateById(updateProduct);
        }
        // 清空已购买的购物车项
        for (Cart cart : selectedCarts) {
            cartService.removeById(cart.getId());
        }
        return order;
    }

    @Override
    public IPage<OrderInfo> pageUserOrders(Integer page, Integer size, Integer status) {
        Long userId = SecurityUtil.getCurrentUserId();
        Page<OrderInfo> pageParam = new Page<>(page, size);
        IPage<OrderInfo> result = baseMapper.selectOrderPage(pageParam, userId, status, null);
        // 补充订单项数据
        for (OrderInfo order : result.getRecords()) {
            List<OrderItem> items = orderItemMapper.selectList(
                    new LambdaQueryWrapper<OrderItem>().eq(OrderItem::getOrderId, order.getId()));
            order.setOrderItems(items);
        }
        return result;
    }

    @Override
    public IPage<OrderInfo> pageAllOrders(Integer page, Integer size, Integer status, String orderNo) {
        Page<OrderInfo> pageParam = new Page<>(page, size);
        IPage<OrderInfo> result = baseMapper.selectOrderPage(pageParam, null, status, orderNo);
        // 补充订单项数据
        for (OrderInfo order : result.getRecords()) {
            List<OrderItem> items = orderItemMapper.selectList(
                    new LambdaQueryWrapper<OrderItem>().eq(OrderItem::getOrderId, order.getId()));
            order.setOrderItems(items);
        }
        return result;
    }

    @Override
    public OrderInfo getOrderDetail(Long id) {
        return baseMapper.selectOrderDetail(id);
    }

    @Override
    @Transactional
    public void payOrder(Long id) {
        OrderInfo order = this.getById(id);
        if (order == null) {
            throw new BusinessException("订单不存在");
        }
        if (order.getStatus() != 0) {
            throw new BusinessException("订单状态不正确");
        }
        order.setStatus(1); // 待发货
        order.setPayTime(LocalDateTime.now());
        this.updateById(order);
        // 更新商品销量并记录用户行为
        List<OrderItem> items = orderItemMapper.selectList(
                new LambdaQueryWrapper<OrderItem>().eq(OrderItem::getOrderId, id));
        for (OrderItem item : items) {
            Product product = productService.getById(item.getProductId());
            if (product != null) {
                Product update = new Product();
                update.setId(product.getId());
                update.setSales(product.getSales() + item.getQuantity());
                productService.updateById(update);
            }
            // 记录购买行为
            recommendService.updateUserProductRating(order.getUserId(), item.getProductId(), "buy");
        }
    }

    @Override
    public void deliverOrder(Long id) {
        OrderInfo order = this.getById(id);
        if (order == null) {
            throw new BusinessException("订单不存在");
        }
        if (order.getStatus() != 1) {
            throw new BusinessException("订单状态不正确");
        }
        order.setStatus(2); // 待收货
        order.setDeliverTime(LocalDateTime.now());
        this.updateById(order);
    }

    @Override
    public void receiveOrder(Long id) {
        OrderInfo order = this.getById(id);
        if (order == null) {
            throw new BusinessException("订单不存在");
        }
        if (order.getStatus() != 2) {
            throw new BusinessException("订单状态不正确");
        }
        order.setStatus(3); // 已完成
        order.setReceiveTime(LocalDateTime.now());
        this.updateById(order);
    }

    @Override
    @Transactional
    public void cancelOrder(Long id) {
        OrderInfo order = this.getById(id);
        if (order == null) {
            throw new BusinessException("订单不存在");
        }
        if (order.getStatus() != 0) {
            throw new BusinessException("只能取消待付款订单");
        }
        order.setStatus(4); // 已取消
        this.updateById(order);
        // 恢复库存
        List<OrderItem> items = orderItemMapper.selectList(
                new LambdaQueryWrapper<OrderItem>().eq(OrderItem::getOrderId, id));
        for (OrderItem item : items) {
            Product product = productService.getById(item.getProductId());
            if (product != null) {
                Product update = new Product();
                update.setId(product.getId());
                update.setStock(product.getStock() + item.getQuantity());
                productService.updateById(update);
            }
        }
    }

    @Override
    @Transactional
    public void deleteOrder(Long id) {
        // 删除订单明细
        orderItemMapper.delete(new LambdaQueryWrapper<OrderItem>()
                .eq(OrderItem::getOrderId, id));
        // 删除订单
        this.removeById(id);
    }

    @Override
    public void updateOrderRemark(Long id, String remark) {
        OrderInfo order = new OrderInfo();
        order.setId(id);
        order.setRemark(remark);
        this.updateById(order);
    }

    @Override
    public List<Map<String, Object>> getSalesStatsByDate(LocalDateTime startTime, LocalDateTime endTime) {
        return baseMapper.selectSalesStatsByDate(startTime, endTime);
    }

    @Override
    public List<Map<String, Object>> getProductSalesRank(LocalDateTime startTime, LocalDateTime endTime, Integer limit) {
        return baseMapper.selectProductSalesRank(startTime, endTime, limit);
    }

    @Override
    public List<Map<String, Object>> getCategorySalesRatio(LocalDateTime startTime, LocalDateTime endTime) {
        return baseMapper.selectCategorySalesRatio(startTime, endTime);
    }

    @Override
    public List<Map<String, Object>> getOrderStatusStats(LocalDateTime startTime, LocalDateTime endTime) {
        return baseMapper.selectOrderStatusStats(startTime, endTime);
    }
}
