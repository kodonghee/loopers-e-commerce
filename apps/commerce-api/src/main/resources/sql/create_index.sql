CREATE INDEX idx_product__brand_created_id_desc
	ON product (brand_id, created_at DESC, id DESC);

CREATE INDEX idx_product__brand_price_id
  	ON product (brand_id, price, id DESC);

CREATE INDEX idx_pls__likecount_product
  	ON product_like_summary (like_count DESC, product_id);

CREATE INDEX idx_product__created_id_desc
	ON product (created_at DESC, id DESC);

CREATE INDEX idx_product__price_id
	ON product (price, id DESC);