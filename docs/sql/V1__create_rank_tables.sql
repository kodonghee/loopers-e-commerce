CREATE TABLE mv_product_rank_weekly (
  product_id     BIGINT NOT NULL,
  like_count     INT    NOT NULL,
  order_count    INT    NOT NULL,
  order_quantity INT    NOT NULL,
  view_count     INT    NOT NULL,
  updated_at     DATETIME NOT NULL,
  PRIMARY KEY (product_id)
);

CREATE TABLE mv_product_rank_monthly (
  product_id     BIGINT NOT NULL,
  like_count     INT    NOT NULL,
  order_count    INT    NOT NULL,
  order_quantity INT    NOT NULL,
  view_count     INT    NOT NULL,
  updated_at     DATETIME NOT NULL,
  PRIMARY KEY (product_id)
);

CREATE TABLE history_product_rank_weekly (
  product_id     BIGINT NOT NULL,
  like_count     INT    NOT NULL,
  order_count    INT    NOT NULL,
  order_quantity INT    NOT NULL,
  view_count     INT    NOT NULL,
  year_week      VARCHAR(10) NOT NULL,
  created_at     DATETIME NOT NULL,
  PRIMARY KEY (product_id, year_week)
);

CREATE TABLE history_product_rank_monthly (
  product_id     BIGINT NOT NULL,
  like_count     INT    NOT NULL,
  order_count    INT    NOT NULL,
  order_quantity INT    NOT NULL,
  view_count     INT    NOT NULL,
  year_month     VARCHAR(7) NOT NULL,
  created_at     DATETIME NOT NULL,
  PRIMARY KEY (product_id, year_month)
);