package com.mall.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.mall.entity.BrowseHistory;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

/**
 * 浏览记录Mapper接口
 */
@Mapper
public interface BrowseHistoryMapper extends BaseMapper<BrowseHistory> {

    /**
     * 分页查询用户浏览记录（带商品信息）
     */
    IPage<BrowseHistory> selectHistoryPage(Page<BrowseHistory> page, @Param("userId") Long userId);

    /**
     * 插入或更新浏览记录（同一天内只保留一条）
     */
    void insertOrUpdateBrowse(@Param("userId") Long userId, @Param("productId") Long productId);
}
