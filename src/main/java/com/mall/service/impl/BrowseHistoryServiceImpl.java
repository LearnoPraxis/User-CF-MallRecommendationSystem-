package com.mall.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.mall.entity.BrowseHistory;
import com.mall.mapper.BrowseHistoryMapper;
import com.mall.security.SecurityUtil;
import com.mall.service.BrowseHistoryService;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;

/**
 * 浏览记录服务实现类
 */
@Service
public class BrowseHistoryServiceImpl extends ServiceImpl<BrowseHistoryMapper, BrowseHistory> 
        implements BrowseHistoryService {

    @Override
    public void recordBrowse(Long productId) {
        Long userId = SecurityUtil.getCurrentUserId();
        if (userId == null) {
            return;
        }
        // 检查今天是否已有记录
        LocalDateTime today = LocalDateTime.now().withHour(0).withMinute(0).withSecond(0);
        long count = this.count(new LambdaQueryWrapper<BrowseHistory>()
                .eq(BrowseHistory::getUserId, userId)
                .eq(BrowseHistory::getProductId, productId)
                .ge(BrowseHistory::getBrowseTime, today));
        
        if (count > 0) {
            // 今天已有记录，更新浏览时间
            BrowseHistory updateEntity = new BrowseHistory();
            updateEntity.setBrowseTime(LocalDateTime.now());
            this.update(updateEntity, new LambdaQueryWrapper<BrowseHistory>()
                    .eq(BrowseHistory::getUserId, userId)
                    .eq(BrowseHistory::getProductId, productId)
                    .ge(BrowseHistory::getBrowseTime, today));
        } else {
            // 新增记录
            BrowseHistory history = new BrowseHistory();
            history.setUserId(userId);
            history.setProductId(productId);
            history.setBrowseTime(LocalDateTime.now());
            history.setDuration(0);
            this.save(history);
        }
    }

    @Override
    public IPage<BrowseHistory> pageUserHistory(Integer page, Integer size) {
        Long userId = SecurityUtil.getCurrentUserId();
        Page<BrowseHistory> pageParam = new Page<>(page, size);
        return baseMapper.selectHistoryPage(pageParam, userId);
    }

    @Override
    public void deleteHistory(Long id) {
        this.removeById(id);
    }

    @Override
    public void clearHistory() {
        Long userId = SecurityUtil.getCurrentUserId();
        this.remove(new LambdaQueryWrapper<BrowseHistory>()
                .eq(BrowseHistory::getUserId, userId));
    }
}
