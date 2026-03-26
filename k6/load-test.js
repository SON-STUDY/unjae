import http from 'k6/http';
import { check, sleep } from 'k6';
import { Counter } from 'k6/metrics';

// 5xx만 진짜 실패로 별도 추적
const serverErrors = new Counter('server_errors');

const BASE_URL = __ENV.K6_BASE_URL || 'http://localhost:8080';

export const options = {
  stages: [
    { duration: '20s', target: 20 },  // 램프업
    { duration: '2m',  target: 30 },  // 30 VU 유지
    { duration: '20s', target: 0 },   // 램프다운
  ],
  thresholds: {
    http_req_failed:   ['rate<0.30'],  // 4xx 포함 30% 이하 (의도된 404/400 ~25%)
    http_req_duration: ['p(95)<2000'], // p95 2초 이하
    server_errors:     ['count<10'],   // 5xx는 10건 미만
  },
};

const JSON_HEADERS = { 'Content-Type': 'application/json' };

// ── setup: 유저 1명 + 게시글 1개 생성 → postId, userId 반환 ─────────────
export function setup() {
  // 유저 생성
  const userRes = http.post(
    `${BASE_URL}/api/users`,
    JSON.stringify({ name: 'k6-user', email: `k6-${Date.now()}@test.com` }),
    { headers: JSON_HEADERS }
  );
  const userId = userRes.status === 201 ? userRes.json('data.id') : null;
  console.log(`setup: user → status=${userRes.status}, userId=${userId}`);

  if (!userId) {
    console.error('유저 생성 실패. 응답: ' + userRes.body);
    return { userId: null, postId: null };
  }

  // 게시글 생성 (댓글 테스트용 postId 확보)
  const postRes = http.post(
    `${BASE_URL}/api/posts`,
    JSON.stringify({ title: 'k6-setup-post', content: 'setup content' }),
    { headers: { ...JSON_HEADERS, 'X-User-Id': String(userId) } }
  );
  const postId = postRes.status === 201 ? postRes.json('data.id') : null;
  console.log(`setup: post  → status=${postRes.status}, postId=${postId}`);

  return { userId, postId };
}

// ── 메인 시나리오 ─────────────────────────────────────────────────────────
export default function (data) {
  const { userId, postId } = data;
  const headers = { ...JSON_HEADERS, 'X-User-Id': String(userId) };
  const rand = Math.random();

  let res;

  if (rand < 0.25) {
    // 25% — 게시글 목록 조회
    res = http.get(`${BASE_URL}/api/posts`);
    check(res, { 'GET /api/posts 200': r => r.status === 200 });

  } else if (rand < 0.40) {
    // 15% — 유저 목록 조회
    res = http.get(`${BASE_URL}/api/users`);
    check(res, { 'GET /api/users 200': r => r.status === 200 });

  } else if (rand < 0.50) {
    // 10% — 댓글 목록 조회
    res = http.get(`${BASE_URL}/api/comments?postId=${postId}`);
    check(res, { 'GET /api/comments 200': r => r.status === 200 });

  } else if (rand < 0.65) {
    // 15% — 게시글 생성
    res = http.post(
      `${BASE_URL}/api/posts`,
      JSON.stringify({ title: `k6-post-${Date.now()}`, content: 'load test content' }),
      { headers }
    );
    check(res, { 'POST /api/posts 201': r => r.status === 201 });

  } else if (rand < 0.75) {
    // 10% — 댓글 생성
    res = http.post(
      `${BASE_URL}/api/comments`,
      JSON.stringify({ postId: postId, content: `k6-comment-${Date.now()}` }),
      { headers }
    );
    check(res, { 'POST /api/comments 201': r => r.status === 201 });

  } else if (rand < 0.85) {
    // 10% — 존재하지 않는 게시글 → 404 (의도된 에러)
    res = http.get(`${BASE_URL}/api/posts/9999999`);

  } else if (rand < 0.92) {
    // 7% — validation 실패 → 400 (의도된 에러)
    res = http.post(
      `${BASE_URL}/api/users`,
      JSON.stringify({ name: '' }),
      { headers: JSON_HEADERS }
    );

  } else if (rand < 0.96) {
    // 4% — 다른 유저가 게시글 수정 시도 → 403 POST_FORBIDDEN
    res = http.put(
      `${BASE_URL}/api/posts/${postId}`,
      JSON.stringify({ title: 'hacked-title', content: 'hacked' }),
      { headers: { ...JSON_HEADERS, 'X-User-Id': String(userId + 99999) } }
    );
    check(res, { 'PUT /api/posts 403': r => r.status === 403 });

  } else {
    // 4% — X-User-Id 없이 게시글 생성 → 에러 유발
    res = http.post(
      `${BASE_URL}/api/posts`,
      JSON.stringify({ title: 'no-header', content: 'test' }),
      { headers: JSON_HEADERS }
    );
  }

  // 5xx만 진짜 실패로 카운트
  if (res && res.status >= 500) {
    serverErrors.add(1);
  }

  sleep(0.1); // 100ms 간격 → VU 30명 기준 ~300 RPS
}

// ── 종료 후 요약 ──────────────────────────────────────────────────────────
export function handleSummary(data) {
  const reqs  = data.metrics.http_reqs.values.count;
  const fails = data.metrics.http_req_failed.values.rate * 100;
  const p95   = data.metrics.http_req_duration.values['p(95)'];

  console.log('━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━');
  console.log(`총 요청수   : ${reqs}`);
  console.log(`에러율      : ${fails.toFixed(2)}%`);
  console.log(`p95 레이턴시: ${p95.toFixed(0)}ms`);
  console.log('━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━');

  return {};
}
