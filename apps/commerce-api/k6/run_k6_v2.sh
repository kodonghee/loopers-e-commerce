#!/usr/bin/env bash
set -euo pipefail

SCRIPT_DIR="$(cd "$(dirname "$0")" && pwd)"
SCRIPT_PATH="${1:-$SCRIPT_DIR/as_is_products_select_test_v2.js}"   # k6 스크립트 파일
TEST_NAME="${2:-product-detail}"                                # 결과 파일 prefix
RESULT_DIR="${RESULT_DIR:-detail_results}"

mkdir -p "$RESULT_DIR"

TS="$(date +%Y%m%d_%H%M%S)"
OUT_BASE="$RESULT_DIR/${TEST_NAME}_${TS}"

# ----- 기본값 (실행 시 env로 덮어쓰기 가능) -----
: "${BASE_URL:=http://localhost:8080}"
: "${BRAND_ID:=200}"
: "${PAGE:=0}"
: "${PAGE_SIZE:=20}"
: "${SORT:=latest}"            # latest | price_asc | likes_desc
: "${P95_MS:=25000}"         # p95 임계치(ms), 실행 시 조정 가능
: "${DISCARD:=1}"            # 1이면 응답 바디 버림 (기본: 켬)
: "${WARM_COUNT:=50}"        # 워밍 요청 수
# -------------------------------------------------

: "${PROM_RW_ENABLE:=0}"
: "${PROM_RW_URL:=http://localhost:9090/api/v1/write}"
: "${PROM_TREND_STATS:=p(95),avg,max}"


K6_OUTPUTS=(-o "experimental-prometheus-rw")
export K6_PROMETHEUS_RW_SERVER_URL="${PROM_RW_URL}"
export K6_PROMETHEUS_RW_TREND_STATS="${PROM_TREND_STATS}"


# --- Redis FLUSH 옵션 (기본: 끔; Warm 측정 시 절대 켜지 마세요) ---
: "${FLUSH_BEFORE:=0}"                       # 1이면 k6 전에 FLUSHDB 수행
: "${REDIS_HOST:=localhost}"
: "${REDIS_PORT:=6379}"
: "${REDIS_DB:=0}"
: "${REDIS_CONTAINER:=dev-redis-master}"

echo "▶ Running k6"
echo "  SCRIPT      : $SCRIPT_PATH"
echo "  BASE_URL    : $BASE_URL"
echo "  BRAND_ID    : $BRAND_ID"
echo "  PAGE        : $PAGE"
echo "  PAGE_SIZE   : $PAGE_SIZE"
echo "  SORT        : $SORT"
echo "  WARM_COUNT  : $WARM_COUNT"
echo "  P95_MS      : $P95_MS"
echo "  DISCARD     : $DISCARD"
echo "  OUT_BASE    : $OUT_BASE"
echo "  FLUSH_BEFORE: $FLUSH_BEFORE"

[[ -f "$SCRIPT_PATH" ]] || { echo "❌ k6 script not found: $SCRIPT_PATH"; exit 1; }

flush_redis() {
  echo "🧹 FLUSH Redis DB $REDIS_DB ..."
  if docker ps -a --format '{{.Names}}' | grep -qw "$REDIS_CONTAINER"; then
    docker exec "$REDIS_CONTAINER" redis-cli -n "$REDIS_DB" FLUSHDB
  elif command -v redis-cli >/dev/null 2>&1; then
    redis-cli -h "$REDIS_HOST" -p "$REDIS_PORT" -n "$REDIS_DB" FLUSHDB
  else
    echo "ℹ️ no redis-cli/container — skip FLUSH"
  fi
}

if [[ "$FLUSH_BEFORE" == "1" ]]; then
  flush_redis || true
fi

k6 run \
  "${K6_OUTPUTS[@]}" \
  -e OUT_BASE="$OUT_BASE" \
  -e BASE_URL="$BASE_URL" \
  -e BRAND_ID="$BRAND_ID" \
  -e PAGE="$PAGE" \
  -e PAGE_SIZE="$PAGE_SIZE" \
  -e SORT="$SORT" \
  -e P95_MS="$P95_MS" \
  -e DISCARD="$DISCARD" \
  -e WARM_COUNT="$WARM_COUNT" \
  "$SCRIPT_PATH"

echo
echo "✅ Done!"
echo "  Summary JSON : ${OUT_BASE}.summary.json"
