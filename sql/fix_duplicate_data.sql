-- =============================================
-- 修复重复数据脚本
-- =============================================

-- 1. 查看重复数据
SELECT '=== 检查重复数据 ===' as info;

SELECT 'cart 表重复数据:' as info;
SELECT user_id, product_id, COUNT(*) as cnt 
FROM cart 
GROUP BY user_id, product_id 
HAVING cnt > 1;

SELECT 'user_product_rating 表重复数据:' as info;
SELECT user_id, product_id, COUNT(*) as cnt 
FROM user_product_rating 
GROUP BY user_id, product_id 
HAVING cnt > 1;

SELECT 'user_favorite 表重复数据:' as info;
SELECT user_id, product_id, COUNT(*) as cnt 
FROM user_favorite 
GROUP BY user_id, product_id 
HAVING cnt > 1;

-- 2. 删除重复数据（保留id最小的记录）
SELECT '=== 删除重复数据 ===' as info;

-- 删除 cart 重复数据
DELETE t1 FROM cart t1
INNER JOIN cart t2
WHERE t1.user_id = t2.user_id 
  AND t1.product_id = t2.product_id 
  AND t1.id > t2.id;

-- 删除 user_product_rating 重复数据
DELETE t1 FROM user_product_rating t1
INNER JOIN user_product_rating t2
WHERE t1.user_id = t2.user_id 
  AND t1.product_id = t2.product_id 
  AND t1.id > t2.id;

-- 删除 user_favorite 重复数据
DELETE t1 FROM user_favorite t1
INNER JOIN user_favorite t2
WHERE t1.user_id = t2.user_id 
  AND t1.product_id = t2.product_id 
  AND t1.id > t2.id;

SELECT '=== 清理完成 ===' as info;
