-- 최신 주간 랭킹 (최근 7일치, 서비스 API용)
CREATE TABLE mv_product_rank_weekly (
  product_id   BIGINT NOT NULL,
  like_count   INT    NOT NULL,
  order_count  INT    NOT NULL,
  view_count   INT    NOT NULL,
  year_week    VARCHAR(10) NOT NULL,   -- 예: 2025W37
  updated_at   DATETIME NOT NULL,
  PRIMARY KEY (product_id, year_week)
);

-- 과거 주간 랭킹 (히스토리 보관용)
CREATE TABLE history_product_rank_weekly (
  product_id   BIGINT NOT NULL,
  like_count   INT    NOT NULL,
  order_count  INT    NOT NULL,
  view_count   INT    NOT NULL,
  year_week    VARCHAR(10) NOT NULL,
  created_at   DATETIME NOT NULL,
  PRIMARY KEY (product_id, year_week)
);

-- 최신 월간 랭킹 (최근 30일치, 서비스 API용)
CREATE TABLE mv_product_rank_monthly (
  product_id   BIGINT NOT NULL,
  like_count   INT    NOT NULL,
  order_count  INT    NOT NULL,
  view_count   INT    NOT NULL,
  year_month   VARCHAR(7) NOT NULL,   -- 예: 2025-09
  updated_at   DATETIME NOT NULL,
  PRIMARY KEY (product_id, year_month)
);

-- 과거 월간 랭킹 (히스토리 보관용)
CREATE TABLE history_product_rank_monthly (
  product_id   BIGINT NOT NULL,
  like_count   INT    NOT NULL,
  order_count  INT    NOT NULL,
  view_count   INT    NOT NULL,
  year_month   VARCHAR(7) NOT NULL,
  created_at   DATETIME NOT NULL,
  PRIMARY KEY (product_id, year_month)
);