import http from 'k6/http';
import { check, sleep } from 'k6';

export const options = {
  vus: Number(__ENV.VUS || '10'),
  duration: __ENV.DUR || '30s',
  thresholds: {},                  // ✅ 임계치 없음
  discardResponseBodies: (__ENV.DISCARD || '1') === '1', // 기본 바디 삭제(성능만)
  summaryTrendStats: ['avg','med','p(90)','p(95)','max'],
};

export default function () {
  const url = __ENV.URL; // 전체 URL을 환경변수로 받음
  const res = http.get(url, { headers: { Accept: 'application/json' } });
  check(res, { '200': r => r.status === 200 });
  sleep(0.1); // 살짝 페이싱
}
