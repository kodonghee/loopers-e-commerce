USE loopers;

SET @N := 1000;  -- 넣을 개수
SET @BRAND_CNT := (SELECT COUNT(*) FROM brand);
SET SESSION cte_max_recursion_depth = 1000000;  -- 100만까지 허용

INSERT INTO brand (name, created_at, updated_at, deleted_at)
SELECT
    CONCAT('브랜드-', LPAD(n, 4, '0')),
    NOW(6), NOW(6), NULL
FROM (
         WITH RECURSIVE seq(n) AS (
             SELECT 1
             UNION ALL
             SELECT n+1 FROM seq WHERE n < (SELECT @N)
         )
         SELECT n FROM seq
     ) AS s;
commit;

-- 브랜드 인입 이후에 다시 계산
SET @BRAND_CNT := (SELECT COUNT(*) FROM brand);

SET @START := 4500001;
SET @COUNT := 500000;

INSERT INTO product (name, stock_value, price, brand_id, created_at, updated_at, deleted_at)
SELECT
    CONCAT(
      ELT(1 + (rn % 12), '블랙','화이트','네이비','그레이','베이지','카키','레드','핑크','옐로우','퍼플','브라운','민트'),
      ' ',
      ELT(1 + (rn % 30),
          '티셔츠','셔츠','블라우스','후디','맨투맨','니트','가디건','베스트','청바지','슬랙스',
          '조거팬츠','트레이닝팬츠','스커트','원피스','코트','재킷','블레이저','바람막이','패딩','트렌치코트',
          '러닝화','스니커즈','구두','로퍼','부츠','샌들','백팩','크로스백','모자','머플러'
      ),
      ' ',
      LPAD(rn, 7, '0')
    ) AS name,
    1 + (rn * 7 % 1000)        AS stock_value,              -- 1~1000 분포
    5000 + (rn * 123457 % 1500000) AS price,                -- 5천~150만 분포
    br.id                      AS brand_id,
    NOW(6), NOW(6), NULL
FROM (
  WITH RECURSIVE seq(n) AS (
    SELECT @START
    UNION ALL
    SELECT n+1 FROM seq WHERE n < (@START + @COUNT - 1)
  )
  SELECT n AS rn FROM seq
) AS s
JOIN (
  SELECT id, ROW_NUMBER() OVER (ORDER BY id) AS r
  FROM brand
) AS br
  ON br.r = ((s.rn - 1) % @BRAND_CNT) + 1;


-- 좋아요 테이블 데이터 인입
INSERT IGNORE INTO product_like (user_id, product_id, created_at, updated_at)
WITH RECURSIVE u(n, user_str) AS (
    SELECT 1, LPAD('1', 7, '0')
    UNION ALL
    SELECT n+1, LPAD(CAST(n+1 AS CHAR), 7, '0')
    FROM u
    WHERE n < 1000000
),
r(n) AS (
    SELECT 1 UNION ALL SELECT 2 UNION ALL SELECT 3 UNION ALL SELECT 4
),
pairs AS (
    SELECT
        CONCAT('user', u.user_str) AS uid,
        1 + FLOOR(
            POW(((CRC32(CONCAT(u.n,'-',r.n)) % 1000000) / 1000000), 2.2) * 5000000
        ) AS pid
    FROM u
    JOIN r
)
SELECT p.uid, p.pid, NOW(6), NOW(6)
FROM pairs p
JOIN product prod ON prod.id = p.pid;   -- 존재하는 product만 통과

commit;


INSERT INTO product_like_summary (product_id, like_count)
SELECT pl.product_id, COUNT(*)
FROM product_like pl
GROUP BY pl.product_id
ON DUPLICATE KEY UPDATE like_count = VALUES(like_count);