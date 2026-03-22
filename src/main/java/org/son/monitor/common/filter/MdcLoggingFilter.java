package org.son.monitor.common.filter;

import com.github.f4b6a3.tsid.TsidCreator;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.slf4j.MDC;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;

@Component
@Order(1)
public class MdcLoggingFilter extends OncePerRequestFilter {

    private static final String TRACE_ID = "traceId";
    private static final String USER_ID = "userId";
    private static final String UPSTREAM_TRACE_HEADER = "X-Trace-Id";
    private static final String USER_ID_HEADER = "X-User-Id";

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
            throws ServletException, IOException {
        try {
            // 업스트림 게이트웨이가 이미 traceId를 심었다면 그것을 사용, 없으면 TSID 신규 생성
            String upstreamTrace = request.getHeader(UPSTREAM_TRACE_HEADER);
            String traceId = (upstreamTrace != null && !upstreamTrace.isBlank())
                    ? upstreamTrace
                    : TsidCreator.getTsid().toLowerCase();

            String userId = request.getHeader(USER_ID_HEADER);

            MDC.put(TRACE_ID, traceId);
            MDC.put(USER_ID, userId != null ? userId : "-");

            // 응답 헤더에도 traceId를 실어서 클라이언트가 로그 추적 가능하게
            response.setHeader(UPSTREAM_TRACE_HEADER, traceId);

            filterChain.doFilter(request, response);
        } finally {
            MDC.clear();
        }
    }
}
