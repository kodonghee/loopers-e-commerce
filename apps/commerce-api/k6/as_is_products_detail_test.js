import http from 'k6/http';
import { check, sleep } from 'k6';
import { Counter, Trend } from 'k6/metrics';
import { htmlReport } from 'https://raw.githubusercontent.com/benc-uk/k6-reporter/main/dist/bundle.js';

// ===== 실행 시 바꿔쓸 수 있는 환경변수 (기본값) =====
const BASE_URL  = __ENV.BASE_URL  || 'http://localhost:8080';
const PRODUCT_ID = __ENV.PRODUCT_ID || '1';

// 임계치 런타임 설정
const DISABLE_THRESHOLDS = (__ENV.DISABLE_THRESHOLDS || '0') === '1';
const P95_MS = Number(__ENV.P95_MS || '25000');    // 필요시 실행 때 조정
const DISCARD = (__ENV.DISCARD || '0') === '1';    // 응답 바디 버리기(성능 측정만)

// ===== 커스텀 메트릭 =====
const detailLatency = new Trend('latency_detail');
const detailErrors  = new Counter('errors_detail');
const http2xx     = new Counter('http_2xx');
const http4xx     = new Counter('http_4xx');
const http5xx     = new Counter('http_5xx');

// options는 런타임 env로 동적으로 구성
export const options = {
  discardResponseBodies: DISCARD,
  stages: [
    { duration: '15s', target: 10 }, // warm-up
    { duration: '45s', target: 30 }, // load
    { duration: '15s', target: 0  }, // cool-down
  ],
  thresholds: DISABLE_THRESHOLDS ? {} : {
    // 개선 과정에서 단계적으로 낮추면 됨
    'latency_detail': [`p(95)<${P95_MS}`],
    'errors_detail':  ['count==0'],
  },
  tags: { app: 'loopers-commerce' },
};

export default function () {
  const url = `${BASE_URL}/api/v1/products/${PRODUCT_ID}`;

  const res = http.get(url, {
    headers: { 'Accept': 'application/json' },
    tags: { endpoint: 'detail' },
  });

  // 메트릭 기록
  detailLatency.add(res.timings.duration);

  // 상태코드 분포
  if (res.status >= 200 && res.status < 300) http2xx.add(1);
  else if (res.status >= 400 && res.status < 500) http4xx.add(1);
  else if (res.status >= 500) http5xx.add(1);

  // 기본 체크 (응답 스키마에 맞게 필요 시 수정)
  const ok = check(res, {
    'detail: 200': (r) => r.status === 200,
    'detail: body exists': (r) => !!r.body && r.body.length > 0,
  });

  if (!ok) detailErrors.add(1);

  // 0.2~0.8s 랜덤 think time
  sleep(Math.random() * 0.6 + 0.2);
}

// 종료 시 요약 JSON + HTML 리포트 자동 생성
export function handleSummary(data) {
  const base = __ENV.OUT_BASE || `detail_results/run_${Date.now()}`;
  return {
    [`${base}.summary.json`]: JSON.stringify(data, null, 2),
    [`${base}.summary.html`]: htmlReport(data),
  };
}