#!/usr/bin/env bash
set -euo pipefail

SCRIPT_DIR="$(cd "$(dirname "$0")" && pwd)"
SCRIPT_PATH="${1:-$SCRIPT_DIR/as_is_products_detail_test.js}"   # k6 스크립트 파일
TEST_NAME="${2:-product-detail}"                                # 결과 파일 prefix
RESULT_DIR="${RESULT_DIR:-detail_results}"

mkdir -p "$RESULT_DIR"

TS="$(date +%Y%m%d_%H%M%S)"
OUT_BASE="$RESULT_DIR/${TEST_NAME}_${TS}"

# ----- 기본값 (실행 시 env로 덮어쓰기 가능) -----
: "${BASE_URL:=http://localhost:8080}"
: "${PRODUCT_ID:=1}"
: "${P95_MS:=25000}"         # p95 임계치(ms), 실행 시 조정 가능
: "${DISCARD:=1}"            # 1이면 응답 바디 버림 (기본: 켬)
: "${WARM_COUNT:=50}"        # 워밍 요청 수
# -------------------------------------------------

# --- Redis FLUSH 옵션 (기본: 끔; Warm 측정 시 절대 켜지 마세요) ---
: "${FLUSH_BEFORE:=0}"                       # 1이면 k6 전에 FLUSHDB 수행
: "${REDIS_HOST:=localhost}"
: "${REDIS_PORT:=6379}"
: "${REDIS_DB:=0}"
: "${REDIS_CONTAINER:=dev-redis-master}"

echo "▶ Running k6"
echo "  SCRIPT      : $SCRIPT_PATH"
echo "  BASE_URL    : $BASE_URL"
echo "  PRODUCT_ID  : $PRODUCT_ID"
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
  -e OUT_BASE="$OUT_BASE" \
  -e BASE_URL="$BASE_URL" \
  -e PRODUCT_ID="$PRODUCT_ID" \
  -e P95_MS="$P95_MS" \
  -e DISCARD="$DISCARD" \
  -e WARM_COUNT="$WARM_COUNT" \
  "$SCRIPT_PATH"

echo
echo "✅ Done!"
echo "  Summary JSON : ${OUT_BASE}.summary.json"
