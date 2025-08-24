import http from 'k6/http';
import { check, sleep } from 'k6';
import { Counter, Trend } from 'k6/metrics';

// ===== 실행 시 바꿔쓸 수 있는 환경변수 (기본값) =====
const BASE_URL   = __ENV.BASE_URL   || 'http://localhost:8080';
const BRAND_ID  = __ENV.BRAND_ID  || '200';
const PAGE      = __ENV.PAGE      || '0';
const PAGE_SIZE = __ENV.PAGE_SIZE || '20';
const SORT      = __ENV.SORT      || 'latest';    // latest | price_asc | likes_desc
const WARM_COUNT = Number(__ENV.WARM_COUNT || '50'); // 🔥 워밍 요청 수

// 임계치 런타임 설정
const DISABLE_THRESHOLDS = (__ENV.DISABLE_THRESHOLDS || '0') === '1';
const P95_MS = Number(__ENV.P95_MS || '25000');    // 필요시 실행 때 조정
const DISCARD = (__ENV.DISCARD || '1') === '1';    // 기본: 바디 버림

// ===== 커스텀 메트릭 =====
const detailLatency = new Trend('latency_detail');
const detailErrors  = new Counter('errors_detail');
const http2xx = new Counter('http_2xx');
const http4xx = new Counter('http_4xx');
const http5xx = new Counter('http_5xx');

// options는 런타임 env로 동적으로 구성
export const options = {
  discardResponseBodies: DISCARD,
  stages: [
    { duration: '15s', target: 10 }, // warm-up
    { duration: '45s', target: 30 }, // load
    { duration: '15s', target: 0  }, // cool-down
  ],
  thresholds: DISABLE_THRESHOLDS ? {} : {
    'latency_detail': [`p(95)<${P95_MS}`],
    'errors_detail':  ['count==0'],
  },
  tags: { app: 'loopers-commerce' },
};

// 🔥 테스트 시작 전에 캐시 워밍
export function setup() {
  const url = `${BASE_URL}/api/v1/products?brandId=${BRAND_ID}&sort=${SORT}&page=${PAGE}&size=${PAGE_SIZE}`;
  for (let i = 0; i < WARM_COUNT; i++) {
    http.get(url, { headers: { 'Accept': 'application/json' } });
  }
}

export default function () {
  const url =  `${BASE_URL}/api/v1/products?brandId=${BRAND_ID}&sort=${SORT}&page=${PAGE}&size=${PAGE_SIZE}`;
  const res = http.get(url, {
    headers: { 'Accept': 'application/json' },
    tags: { endpoint: 'detail' },
  });

  detailLatency.add(res.timings.duration);

  if (res.status >= 200 && res.status < 300) http2xx.add(1);
  else if (res.status >= 400 && res.status < 500) http4xx.add(1);
  else if (res.status >= 500) http5xx.add(1);

  const ok = check(res, {
    'detail: 200': (r) => r.status === 200,
  });
  if (!ok) detailErrors.add(1);

  // 0.2~0.8s 랜덤 think time
  sleep(Math.random() * 0.6 + 0.2);
}

export function handleSummary(data) {
  const base = __ENV.OUT_BASE || `detail_results/run_${Date.now()}`;
  return {
    [`${base}.summary.json`]: JSON.stringify(data, null, 2),
    [`${base}.summary.html`]: htmlReport(data),
  };
}