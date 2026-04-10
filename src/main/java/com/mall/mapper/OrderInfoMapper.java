package com.mall.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.mall.entity.OrderInfo;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

/**
 * 订单Mapper接口
 */
@Mapper
public interface OrderInfoMapper extends BaseMapper<OrderInfo> {

    /**
     * 分页查询订单（带用户名）
     */
    IPage<OrderInfo> selectOrderPage(Page<OrderInfo> page, @Param("userId") Long userId,
                                      @Param("status") Integer status, @Param("orderNo") String orderNo);

    /**
     * 查询订单详情（带订单项）
     */
    OrderInfo selectOrderDetail(@Param("id") Long id);

    /**
     * 统计销售数据（按日期分组）
     */
    List<Map<String, Object>> selectSalesStatsByDate(@Param("startTime") LocalDateTime startTime,
                                                      @Param("endTime") LocalDateTime endTime);

    /**
     * 统计商品销量排行
     */
    List<Map<String, Object>> selectProductSalesRank(@Param("startTime") LocalDateTime startTime,
                                                      @Param("endTime") LocalDateTime endTime,
                                                      @Param("limit") Integer limit);

    /**
     * 统计分类销售占比
     */
    List<Map<String, Object>> selectCategorySalesRatio(@Param("startTime") LocalDateTime startTime,
                                                        @Param("endTime") LocalDateTime endTime);

    /**
     * 统计订单状态分布
     */
    List<Map<String, Object>> selectOrderStatusStats(@Param("startTime") LocalDateTime startTime,
                                                      @Param("endTime") LocalDateTime endTime);
}
