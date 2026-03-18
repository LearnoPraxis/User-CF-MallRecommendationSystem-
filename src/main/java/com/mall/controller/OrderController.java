package com.mall.controller;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.mall.common.PageResult;
import com.mall.common.Result;
import com.mall.dto.OrderDTO;
import com.mall.entity.OrderInfo;
import com.mall.service.OrderService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

/**
 * 订单控制器
 */
@Api(tags = "订单管理")
@RestController
@RequestMapping("/api/order")
public class OrderController {

    @Autowired
    private OrderService orderService;

    @ApiOperation("创建订单")
    @PostMapping("/create")
    public Result<OrderInfo> create(@Validated @RequestBody OrderDTO dto) {
        OrderInfo order = orderService.createOrder(dto);
        return Result.success(order);
    }

    @ApiOperation("分页查询用户订单")
    @GetMapping("/list")
    public Result<PageResult<OrderInfo>> list(
            @RequestParam(defaultValue = "1") Integer page,
            @RequestParam(defaultValue = "10") Integer size,
            @RequestParam(required = false) Integer status) {
        IPage<OrderInfo> pageResult = orderService.pageUserOrders(page, size, status);
        return Result.success(PageResult.of(pageResult));
    }

    @ApiOperation("获取订单详情")
    @GetMapping("/detail/{id}")
    public Result<OrderInfo> detail(@PathVariable Long id) {
        OrderInfo order = orderService.getOrderDetail(id);
        return Result.success(order);
    }

    @ApiOperation("支付订单")
    @PostMapping("/pay/{id}")
    public Result<Void> pay(@PathVariable Long id) {
        orderService.payOrder(id);
        return Result.success();
    }

    @ApiOperation("确认收货")
    @PostMapping("/receive/{id}")
    public Result<Void> receive(@PathVariable Long id) {
        orderService.receiveOrder(id);
        return Result.success();
    }

    @ApiOperation("取消订单")
    @PostMapping("/cancel/{id}")
    public Result<Void> cancel(@PathVariable Long id) {
        orderService.cancelOrder(id);
        return Result.success();
    }
}
