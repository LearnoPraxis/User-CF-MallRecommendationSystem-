package com.mall.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.mall.entity.BrowseHistory;
import com.mall.entity.Product;
import com.mall.entity.UserProductRating;
import com.mall.mapper.BrowseHistoryMapper;
import com.mall.mapper.ProductMapper;
import com.mall.mapper.UserProductRatingMapper;
import com.mall.security.SecurityUtil;
import com.mall.service.ProductService;
import com.mall.service.RecommendService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.*;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

/**
 * 推荐服务实现类
 * 支持调用Python协同过滤服务，Java实现作为备用
 */
@Slf4j
@Service
public class RecommendServiceImpl implements RecommendService {

    @Autowired
    private UserProductRatingMapper userProductRatingMapper;

    @Autowired
    private ProductMapper productMapper;

    @Autowired
    private BrowseHistoryMapper browseHistoryMapper;

    @Autowired
    private ProductService productService;

    @Autowired
    private RedisTemplate<String, Object> redisTemplate;

    @Autowired
    private ObjectMapper objectMapper;

    @Value("${recommend.python-service-url:http://localhost:5000}")
    private String pythonServiceUrl;

    @Value("${recommend.use-python-service:true}")
    private boolean usePythonService;

    private RestTemplate restTemplate = new RestTemplate();

    // 用户行为权重
    private static final BigDecimal BROWSE_WEIGHT = new BigDecimal("0.1");
    private static final BigDecimal CART_WEIGHT = new BigDecimal("0.3");
    private static final BigDecimal FAVORITE_WEIGHT = new BigDecimal("0.3");
    private static final BigDecimal BUY_WEIGHT = new BigDecimal("0.5");
    private static final BigDecimal COMMENT_WEIGHT = new BigDecimal("0.2");

    // 缓存Key前缀
    private static final String RECOMMEND_CACHE_PREFIX = "recommend:user:";
    private static final String HOT_RECOMMEND_KEY = "recommend:hot";

    @Override
    public List<Product> getPersonalizedRecommendations(Integer limit) {
        Long userId = SecurityUtil.getCurrentUserId();
        
        log.info("========== 个性化推荐请求 ==========");
        log.info("用户ID: {}, 请求数量: {}", userId, limit);
        
        if (userId == null) {
            log.info("用户未登录，返回热门推荐");
            return getHotRecommendations(limit);
        }

        // 尝试调用Python推荐服务
        if (usePythonService) {
            log.info("尝试调用Python协同过滤服务...");
            List<Product> pythonResult = callPythonRecommendService(userId, limit);
            if (pythonResult != null && !pythonResult.isEmpty()) {
                log.info("Python服务返回 {} 个推荐商品", pythonResult.size());
                return pythonResult;
            }
            log.warn("Python服务调用失败或无结果，降级使用Java实现");
        }

        // Java实现的协同过滤（备用）
        log.info("使用Java协同过滤算法...");
        return getPersonalizedRecommendationsJava(userId, limit);
    }

    /**
     * 调用Python推荐服务
     */
    private List<Product> callPythonRecommendService(Long userId, Integer limit) {
        try {
            String url = pythonServiceUrl + "/api/recommend/personalized?user_id=" + userId + "&limit=" + limit;
            log.info("请求Python服务: {}", url);
            
            ResponseEntity<String> response = restTemplate.getForEntity(url, String.class);
            
            if (response.getStatusCode().is2xxSuccessful() && response.getBody() != null) {
                JsonNode root = objectMapper.readTree(response.getBody());
                
                String algorithm = root.has("algorithm") ? root.get("algorithm").asText() : "unknown";
                String message = root.has("message") ? root.get("message").asText() : "";
                
                log.info("Python服务响应 - 算法: {}, 消息: {}", algorithm, message);
                
                if (root.has("data") && root.get("data").isArray()) {
                    List<Product> products = new ArrayList<>();
                    for (JsonNode node : root.get("data")) {
                        Product product = new Product();
                        product.setId(node.get("id").asLong());
                        product.setName(node.get("name").asText());
                        product.setMainImage(node.has("mainImage") ? node.get("mainImage").asText() : null);
                        product.setPrice(node.has("price") ? new BigDecimal(node.get("price").asText()) : BigDecimal.ZERO);
                        product.setSales(node.has("sales") ? node.get("sales").asInt() : 0);
                        product.setStock(node.has("stock") ? node.get("stock").asInt() : 0);
                        products.add(product);
                    }
                    return products;
                }
            }
        } catch (Exception e) {
            log.error("调用Python推荐服务失败: {}", e.getMessage());
        }
        return null;
    }

    /**
     * Java实现的协同过滤推荐（备用方案）
     */
    private List<Product> getPersonalizedRecommendationsJava(Long userId, Integer limit) {
        log.info("========== Java协同过滤算法开始 ==========");
        
        // 尝试从缓存获取
        String cacheKey = RECOMMEND_CACHE_PREFIX + userId;
        @SuppressWarnings("unchecked")
        List<Long> cachedIds = (List<Long>) redisTemplate.opsForValue().get(cacheKey);
        if (cachedIds != null && !cachedIds.isEmpty()) {
            log.info("从缓存获取推荐结果: {} 个商品ID", cachedIds.size());
            List<Product> products = productService.getProductsByIds(cachedIds);
            return products.stream().limit(limit).collect(Collectors.toList());
        }

        // 基于协同过滤算法计算推荐
        List<Long> recommendedIds = calculateCollaborativeFilteringForUser(userId, limit * 2);
        
        if (recommendedIds.isEmpty()) {
            log.info("协同过滤无结果，返回热门推荐");
            return getHotRecommendations(limit);
        }

        // 缓存推荐结果（1小时）
        redisTemplate.opsForValue().set(cacheKey, recommendedIds, 1, TimeUnit.HOURS);
        log.info("推荐结果已缓存，过期时间: 1小时");

        List<Product> products = productService.getProductsByIds(recommendedIds);
        log.info("========== Java协同过滤算法完成，返回 {} 个商品 ==========", products.size());
        return products.stream().limit(limit).collect(Collectors.toList());
    }

    /**
     * 基于用户的协同过滤算法
     */
    private List<Long> calculateCollaborativeFilteringForUser(Long userId, Integer limit) {
        log.info("开始计算用户 {} 的协同过滤推荐", userId);
        
        // 获取当前用户的评分数据
        List<UserProductRating> userRatings = userProductRatingMapper.selectByUserId(userId);
        log.info("用户 {} 的评分记录数: {}", userId, userRatings.size());
        
        if (userRatings.isEmpty()) {
            log.info("用户无评分记录，无法进行协同过滤");
            return Collections.emptyList();
        }

        // 获取所有用户的评分数据
        List<UserProductRating> allRatings = userProductRatingMapper.selectAllRatings();
        log.info("系统总评分记录数: {}", allRatings.size());
        
        // 按用户分组
        Map<Long, List<UserProductRating>> userRatingsMap = allRatings.stream()
                .collect(Collectors.groupingBy(UserProductRating::getUserId));
        log.info("参与评分的用户数: {}", userRatingsMap.size());

        // 当前用户已交互的商品
        Set<Long> userInteractedProducts = userRatings.stream()
                .map(UserProductRating::getProductId)
                .collect(Collectors.toSet());

        // 当前用户的评分Map
        Map<Long, BigDecimal> currentUserRatings = userRatings.stream()
                .collect(Collectors.toMap(UserProductRating::getProductId, UserProductRating::getRating));

        // 计算与其他用户的相似度
        log.info("计算用户相似度（余弦相似度）...");
        Map<Long, Double> userSimilarities = new HashMap<>();
        for (Map.Entry<Long, List<UserProductRating>> entry : userRatingsMap.entrySet()) {
            Long otherUserId = entry.getKey();
            if (otherUserId.equals(userId)) {
                continue;
            }
            Map<Long, BigDecimal> otherUserRatings = entry.getValue().stream()
                    .collect(Collectors.toMap(UserProductRating::getProductId, UserProductRating::getRating));
            
            double similarity = calculateCosineSimilarity(currentUserRatings, otherUserRatings);
            if (similarity > 0) {
                userSimilarities.put(otherUserId, similarity);
            }
        }
        log.info("找到 {} 个相似用户", userSimilarities.size());

        // 按相似度排序，取前N个相似用户
        List<Long> similarUsers = userSimilarities.entrySet().stream()
                .sorted(Map.Entry.<Long, Double>comparingByValue().reversed())
                .limit(20)
                .map(Map.Entry::getKey)
                .collect(Collectors.toList());

        if (!similarUsers.isEmpty()) {
            log.info("Top 5 相似用户: {}", similarUsers.subList(0, Math.min(5, similarUsers.size())));
        }

        // 计算推荐商品的预测评分
        log.info("计算商品预测评分...");
        Map<Long, Double> productScores = new HashMap<>();
        for (Long similarUserId : similarUsers) {
            double similarity = userSimilarities.get(similarUserId);
            List<UserProductRating> similarUserRatings = userRatingsMap.get(similarUserId);
            
            for (UserProductRating rating : similarUserRatings) {
                Long productId = rating.getProductId();
                if (!userInteractedProducts.contains(productId)) {
                    double score = similarity * rating.getRating().doubleValue();
                    productScores.merge(productId, score, Double::sum);
                }
            }
        }
        log.info("候选推荐商品数: {}", productScores.size());

        // 按预测评分排序
        List<Long> result = productScores.entrySet().stream()
                .sorted(Map.Entry.<Long, Double>comparingByValue().reversed())
                .limit(limit)
                .map(Map.Entry::getKey)
                .collect(Collectors.toList());
        
        log.info("最终推荐商品ID: {}", result);
        return result;
    }

    /**
     * 计算余弦相似度
     */
    private double calculateCosineSimilarity(Map<Long, BigDecimal> ratings1, Map<Long, BigDecimal> ratings2) {
        Set<Long> commonProducts = new HashSet<>(ratings1.keySet());
        commonProducts.retainAll(ratings2.keySet());

        if (commonProducts.isEmpty()) {
            return 0.0;
        }

        double dotProduct = 0.0;
        double norm1 = 0.0;
        double norm2 = 0.0;

        for (Long productId : commonProducts) {
            double r1 = ratings1.get(productId).doubleValue();
            double r2 = ratings2.get(productId).doubleValue();
            dotProduct += r1 * r2;
            norm1 += r1 * r1;
            norm2 += r2 * r2;
        }

        if (norm1 == 0 || norm2 == 0) {
            return 0.0;
        }

        return dotProduct / (Math.sqrt(norm1) * Math.sqrt(norm2));
    }

    @Override
    public List<Product> getHotRecommendations(Integer limit) {
        log.info("========== 热门推荐请求 ==========");
        
        // 尝试从缓存获取
        @SuppressWarnings("unchecked")
        List<Long> cachedIds = (List<Long>) redisTemplate.opsForValue().get(HOT_RECOMMEND_KEY);
        if (cachedIds != null && !cachedIds.isEmpty()) {
            log.info("从缓存获取热门商品: {} 个", cachedIds.size());
            List<Product> products = productService.getProductsByIds(cachedIds);
            return products.stream().limit(limit).collect(Collectors.toList());
        }

        // 查询热门商品（按销量排序）
        log.info("从数据库查询热门商品...");
        List<Product> hotProducts = productMapper.selectHotProducts(limit * 2);
        log.info("查询到 {} 个热门商品", hotProducts.size());
        
        // 缓存结果（30分钟）
        List<Long> productIds = hotProducts.stream()
                .map(Product::getId)
                .collect(Collectors.toList());
        redisTemplate.opsForValue().set(HOT_RECOMMEND_KEY, productIds, 30, TimeUnit.MINUTES);

        return hotProducts.stream().limit(limit).collect(Collectors.toList());
    }

    @Override
    public List<Product> getRecommendationsByBrowseHistory(Integer limit) {
        Long userId = SecurityUtil.getCurrentUserId();
        
        log.info("========== 基于浏览记录推荐 ==========");
        log.info("用户ID: {}", userId);
        
        if (userId == null) {
            log.info("用户未登录，返回热门推荐");
            return getHotRecommendations(limit);
        }

        // 获取用户最近浏览的商品
        List<BrowseHistory> histories = browseHistoryMapper.selectList(
                new LambdaQueryWrapper<BrowseHistory>()
                        .eq(BrowseHistory::getUserId, userId)
                        .orderByDesc(BrowseHistory::getBrowseTime)
                        .last("LIMIT 10"));

        log.info("用户最近浏览记录数: {}", histories.size());

        if (histories.isEmpty()) {
            log.info("无浏览记录，返回热门推荐");
            return getHotRecommendations(limit);
        }

        // 获取浏览商品的分类
        Set<Long> browsedProductIds = histories.stream()
                .map(BrowseHistory::getProductId)
                .collect(Collectors.toSet());
        
        List<Product> browsedProducts = productService.getProductsByIds(new ArrayList<>(browsedProductIds));
        Set<Long> categoryIds = browsedProducts.stream()
                .map(Product::getCategoryId)
                .collect(Collectors.toSet());

        log.info("浏览商品涉及分类: {}", categoryIds);

        // 推荐同分类的其他商品
        List<Product> recommendations = new ArrayList<>();
        for (Long categoryId : categoryIds) {
            List<Product> categoryProducts = productMapper.selectList(
                    new LambdaQueryWrapper<Product>()
                            .eq(Product::getCategoryId, categoryId)
                            .eq(Product::getStatus, 1)
                            .notIn(Product::getId, browsedProductIds)
                            .orderByDesc(Product::getSales)
                            .last("LIMIT " + limit));
            recommendations.addAll(categoryProducts);
        }

        // 去重并限制数量
        List<Product> result = recommendations.stream()
                .distinct()
                .limit(limit)
                .collect(Collectors.toList());
        
        log.info("基于浏览记录推荐 {} 个商品", result.size());
        return result;
    }

    @Override
    public void updateUserProductRating(Long userId, Long productId, String actionType) {
        if (userId == null || productId == null) {
            return;
        }

        log.info("更新用户行为评分: userId={}, productId={}, action={}", userId, productId, actionType);

        // 根据行为类型获取权重
        BigDecimal weight;
        switch (actionType) {
            case "browse":
                weight = BROWSE_WEIGHT;
                break;
            case "cart":
                weight = CART_WEIGHT;
                break;
            case "favorite":
                weight = FAVORITE_WEIGHT;
                break;
            case "buy":
                weight = BUY_WEIGHT;
                break;
            case "comment":
                weight = COMMENT_WEIGHT;
                break;
            default:
                return;
        }

        // 使用 INSERT ON DUPLICATE KEY UPDATE 避免并发问题
        userProductRatingMapper.insertOrUpdateRating(userId, productId, actionType, weight);

        // 清除用户推荐缓存
        String cacheKey = RECOMMEND_CACHE_PREFIX + userId;
        redisTemplate.delete(cacheKey);
        
        // 通知Python服务清除缓存
        if (usePythonService) {
            try {
                restTemplate.postForEntity(
                        pythonServiceUrl + "/api/recommend/clear_cache?user_id=" + userId,
                        null, String.class);
            } catch (Exception e) {
                log.warn("通知Python服务清除缓存失败: {}", e.getMessage());
            }
        }
    }

    @Override
    public void calculateCollaborativeFiltering() {
        log.info("========== 开始批量计算协同过滤推荐 ==========");
        
        // 如果使用Python服务，触发Python训练
        if (usePythonService) {
            try {
                log.info("触发Python服务模型训练...");
                restTemplate.postForEntity(pythonServiceUrl + "/api/recommend/train", null, String.class);
                log.info("Python服务训练请求已发送");
                return;
            } catch (Exception e) {
                log.warn("触发Python训练失败，使用Java计算: {}", e.getMessage());
            }
        }
        
        // Java实现的批量计算
        List<UserProductRating> allRatings = userProductRatingMapper.selectAllRatings();
        Set<Long> userIds = allRatings.stream()
                .map(UserProductRating::getUserId)
                .collect(Collectors.toSet());

        log.info("需要计算推荐的用户数: {}", userIds.size());

        for (Long userId : userIds) {
            try {
                List<Long> recommendations = calculateCollaborativeFilteringForUser(userId, 20);
                if (!recommendations.isEmpty()) {
                    String cacheKey = RECOMMEND_CACHE_PREFIX + userId;
                    redisTemplate.opsForValue().set(cacheKey, recommendations, 2, TimeUnit.HOURS);
                }
            } catch (Exception e) {
                log.error("计算用户 {} 的推荐失败: {}", userId, e.getMessage());
            }
        }
        
        log.info("========== 协同过滤推荐计算完成 ==========");
    }
}
