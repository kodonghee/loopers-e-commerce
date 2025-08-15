#!/usr/bin/env bash
set -euo pipefail

SCRIPT_PATH="${1:-as_is_products_detail_test.js}"         # k6 스크립트 파일
TEST_NAME="${2:-product-detail}"   # 결과 파일 prefix
RESULT_DIR="${RESULT_DIR:-detail_results}"

mkdir -p "$RESULT_DIR"

TS="$(date +%Y%m%d_%H%M%S)"
OUT_BASE="$RESULT_DIR/${TEST_NAME}_${TS}"

# ----- 기본값 (실행 시 env로 덮어쓰기 가능) -----
: "${BASE_URL:=http://localhost:8080}"
: "${PRODUCT_ID:=1}"
: "${P95_MS:=25000}"           # p95 임계치(ms), 실행 시 조정 가능
: "${DISCARD:=0}"              # 1이면 응답 바디 버림
# -------------------------------------------------

# Prometheus Remote Write 설정
: "${PROM_RW_URL:=http://localhost:9090/api/v1/write}"   # Prometheus 주소
: "${PROM_TREND_STATS:=p(95),avg,max}"                   # 변환할 통계
: "${PROM_NATIVE_HISTOGRAM:=0}"                          # 1=네이티브 히스토그램

# 임계치 스킵 스위치
EXTRA_FLAGS=""
DISABLE_THRESHOLDS_ENV="0"
if [[ "${SKIP_THRESHOLDS:-0}" == "1" ]]; then
  EXTRA_FLAGS="--no-thresholds"
  DISABLE_THRESHOLDS_ENV="1"
fi

K6_OUTPUTS=(-o "experimental-prometheus-rw" -o "json=${OUT_BASE}.metrics.json")

# k6 환경변수 설정 (Prometheus Remote Write용)
export K6_PROMETHEUS_RW_SERVER_URL="${PROM_RW_URL}"
export K6_PROMETHEUS_RW_TREND_STATS="${PROM_TREND_STATS}"

echo "▶ Running k6 (DETAIL ONLY)"
echo "   - BASE_URL    : $BASE_URL"
echo "   - PRODUCT_ID  : $PRODUCT_ID"
echo "   - P95_MS      : $P95_MS"
echo "   - DISCARD     : $DISCARD"
echo "   - OUT_BASE    : $OUT_BASE"
echo "   - PROM_RW_URL : $PROM_RW_URL"
echo "   - PROM_STATS  : $PROM_TREND_STATS"
[[ "$PROM_NATIVE_HISTOGRAM" == "1" ]] && echo "   - PROM_NATIVE_HISTOGRAM: ON"
if [[ -n "$EXTRA_FLAGS" ]]; then
  echo "   - THRESHOLDS  : SKIPPED (k6 + script)"
fi

k6 run $EXTRA_FLAGS \
  "${K6_OUTPUTS[@]}" \
  -e OUT_BASE="$OUT_BASE" \
  -e BASE_URL="$BASE_URL" \
  -e PRODUCT_ID="$PRODUCT_ID" \
  -e P95_MS="$P95_MS" \
  -e DISCARD="$DISCARD" \
  -e DISABLE_THRESHOLDS="$DISABLE_THRESHOLDS_ENV" \
  "$SCRIPT_PATH"

echo
echo "✅ Done!"
echo "   - Summary JSON : ${OUT_BASE}.summary.json"
echo "   - HTML Report  : ${OUT_BASE}.summary.html"
echo "   - Raw metrics  : ${OUT_BASE}.metrics.json"
echo
echo "📈 Grafana에서 k6 대시보드 확인:"
echo "   - Import Dashboard ID: 19665 (Prometheus용)"
echo "   - Prometheus Data Source 선택"
