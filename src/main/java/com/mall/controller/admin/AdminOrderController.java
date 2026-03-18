package com.mall.controller.admin;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.mall.common.PageResult;
import com.mall.common.Result;
import com.mall.entity.OrderInfo;
import com.mall.service.OrderService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

/**
 * 管理员-订单管理控制器
 */
@Api(tags = "管理员-订单管理")
@RestController
@RequestMapping("/api/admin/order")
public class AdminOrderController {

    @Autowired
    private OrderService orderService;

    @ApiOperation("分页查询订单")
    @GetMapping("/list")
    public Result<PageResult<OrderInfo>> list(
            @RequestParam(defaultValue = "1") Integer page,
            @RequestParam(defaultValue = "10") Integer size,
            @RequestParam(required = false) Integer status,
            @RequestParam(required = false) String orderNo) {
        IPage<OrderInfo> pageResult = orderService.pageAllOrders(page, size, status, orderNo);
        return Result.success(PageResult.of(pageResult));
    }

    @ApiOperation("获取订单详情")
    @GetMapping("/detail/{id}")
    public Result<OrderInfo> detail(@PathVariable Long id) {
        OrderInfo order = orderService.getOrderDetail(id);
        return Result.success(order);
    }

    @ApiOperation("发货")
    @PostMapping("/deliver/{id}")
    public Result<Void> deliver(@PathVariable Long id) {
        orderService.deliverOrder(id);
        return Result.success();
    }

    @ApiOperation("删除订单")
    @DeleteMapping("/{id}")
    public Result<Void> delete(@PathVariable Long id) {
        orderService.deleteOrder(id);
        return Result.success();
    }

    @ApiOperation("修改订单备注")
    @PutMapping("/remark/{id}")
    public Result<Void> updateRemark(@PathVariable Long id, @RequestBody Map<String, String> params) {
        String remark = params.get("remark");
        orderService.updateOrderRemark(id, remark);
        return Result.success();
    }

    // ========== 报表统计 ==========

    @ApiOperation("销售趋势统计（按日期）")
    @GetMapping("/stats/sales")
    public Result<List<Map<String, Object>>> salesStats(
            @RequestParam @DateTimeFormat(pattern = "yyyy-MM-dd HH:mm:ss") LocalDateTime startTime,
            @RequestParam @DateTimeFormat(pattern = "yyyy-MM-dd HH:mm:ss") LocalDateTime endTime) {
        List<Map<String, Object>> stats = orderService.getSalesStatsByDate(startTime, endTime);
        return Result.success(stats);
    }

    @ApiOperation("商品销量排行")
    @GetMapping("/stats/product-rank")
    public Result<List<Map<String, Object>>> productRank(
            @RequestParam @DateTimeFormat(pattern = "yyyy-MM-dd HH:mm:ss") LocalDateTime startTime,
            @RequestParam @DateTimeFormat(pattern = "yyyy-MM-dd HH:mm:ss") LocalDateTime endTime,
            @RequestParam(defaultValue = "10") Integer limit) {
        List<Map<String, Object>> rank = orderService.getProductSalesRank(startTime, endTime, limit);
        return Result.success(rank);
    }

    @ApiOperation("分类销售占比")
    @GetMapping("/stats/category-ratio")
    public Result<List<Map<String, Object>>> categoryRatio(
            @RequestParam @DateTimeFormat(pattern = "yyyy-MM-dd HH:mm:ss") LocalDateTime startTime,
            @RequestParam @DateTimeFormat(pattern = "yyyy-MM-dd HH:mm:ss") LocalDateTime endTime) {
        List<Map<String, Object>> ratio = orderService.getCategorySalesRatio(startTime, endTime);
        return Result.success(ratio);
    }

    @ApiOperation("订单状态统计")
    @GetMapping("/stats/status")
    public Result<List<Map<String, Object>>> orderStatusStats(
            @RequestParam @DateTimeFormat(pattern = "yyyy-MM-dd HH:mm:ss") LocalDateTime startTime,
            @RequestParam @DateTimeFormat(pattern = "yyyy-MM-dd HH:mm:ss") LocalDateTime endTime) {
        List<Map<String, Object>> stats = orderService.getOrderStatusStats(startTime, endTime);
        return Result.success(stats);
    }
}
