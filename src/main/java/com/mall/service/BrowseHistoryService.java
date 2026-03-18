package com.mall.service;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.service.IService;
import com.mall.entity.BrowseHistory;

/**
 * 浏览记录服务接口
 */
public interface BrowseHistoryService extends IService<BrowseHistory> {

    /**
     * 记录浏览
     */
    void recordBrowse(Long productId);

    /**
     * 分页查询用户浏览记录
     */
    IPage<BrowseHistory> pageUserHistory(Integer page, Integer size);

    /**
     * 删除浏览记录
     */
    void deleteHistory(Long id);

    /**
     * 清空浏览记录
     */
    void clearHistory();
}
