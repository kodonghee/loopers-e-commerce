-- 1) 단일/복합 선택도
SELECT
  COUNT(*)                                           AS total_rows,
  COUNT(DISTINCT brand_id)                           AS distinct_brand_id,
  ROUND(COUNT(DISTINCT brand_id)/COUNT(*), 6)        AS selectivity_brand_id,
  COUNT(DISTINCT price)                              AS distinct_price,
  ROUND(COUNT(DISTINCT price)/COUNT(*), 6)           AS selectivity_price,
  COUNT(DISTINCT brand_id, price)                    AS distinct_brand_price,
  ROUND(COUNT(DISTINCT brand_id, price)/COUNT(*), 6) AS selectivity_brand_price
FROM product;

-- 2) 브랜드 상위 편중
WITH total AS (SELECT COUNT(*) AS total_rows FROM product)
SELECT
  ROW_NUMBER() OVER (ORDER BY COUNT(*) DESC) AS `rank`,
  p.brand_id,
  COUNT(*)                                   AS n,
  ROUND(100 * COUNT(*) / t.total_rows, 2)    AS pct
FROM product p
CROSS JOIN total t
GROUP BY p.brand_id, t.total_rows
ORDER BY n DESC
LIMIT 10;