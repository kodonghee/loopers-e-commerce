import http from 'k6/http';
import { check, sleep } from 'k6';
import { Trend } from 'k6/metrics';

export const options = {

  stages: [
    { duration: '1m', target: 20 }, // 1분동안 가상 사용자 0 → 20 까지 올림 (램프업)
    { duration: '3m', target: 20 }, // 3분동안 20명 유지
    { duration: '1m', target: 0 },  // 1분동안 가상 사용자 20 → 0 으로 내림 (쿨다운)
  ],
  thresholds: {
    http_req_duration: ['p(95)<300'], // AS-IS는 일단 크게: 95%가 300ms 미만 이면 통과
    'checks{scope:products}': ['rate>0.99'],
  },
};

const tProductList = new Trend('popular_list_ms');

const BASE_URL = __ENV.BASE_URL || 'http://localhost:8080';
const BRAND_ID = __ENV.BRAND_ID || '';
const SIZE = __ENV.SIZE || '20';

// 프로젝트 API 규격에 맞춰 엔드포인트/파라미터 확인해서 수정해도 됨
// 예: GET /api/v1/products?brandId=...&sort=likes_desc&page=0&size=50
export default function () {
  const url = `${BASE_URL}/api/v1/products?sort=likes_desc&page=0&size=${SIZE}` +
              (BRAND_ID ? `&brandId=${BRAND_ID}` : '');

  const res = http.get(url, { tags: { scope: 'products' } });

  check(res, {
    'status 200': (r) => r.status === 200,
    'has items': (r) => {
      try {
        const json = r.json();
        // ApiResponse.success(...) 구조라면 data 필드에 리스트가 있을 가능성
        const data = json?.data ?? json;
        return Array.isArray(data) ? data.length > 0 : true;
      } catch (_) { return false; }
    },
  }, { scope: 'products' });

  tPopularity.add(res.timings.duration);
  sleep(1); // 약간의 간격
}

// 실행 요약을 JSON 파일로 저장(후분석용)
export function handleSummary(data) {
  return {
    'as_is_summary.json': JSON.stringify(data, null, 2),
  };
}