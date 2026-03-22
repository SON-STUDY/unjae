package org.son.monitor.common.filter;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;

@Slf4j
@Component
@Order(2) // MdcLoggingFilter(1) 이후 실행 → MDC traceId/userId 사용 가능
public class RequestLoggingFilter extends OncePerRequestFilter {

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
            throws ServletException, IOException {
        long start = System.currentTimeMillis();
        try {
            filterChain.doFilter(request, response);
        } finally {
            long elapsed = System.currentTimeMillis() - start;
            int status = response.getStatus();
            String method = request.getMethod();
            String uri = request.getRequestURI();

            if (status >= 500) {
                log.error("HTTP {} {} {} ({}ms)", method, uri, status, elapsed);
            } else if (status >= 400) {
                log.warn("HTTP {} {} {} ({}ms)", method, uri, status, elapsed);
            } else {
                log.info("HTTP {} {} {} ({}ms)", method, uri, status, elapsed);
            }
        }
    }

    // actuator 엔드포인트는 로깅 제외 (메트릭 스크랩 노이즈 방지)
    @Override
    protected boolean shouldNotFilter(HttpServletRequest request) {
        String uri = request.getRequestURI();
        return uri.startsWith("/actuator");
    }
}
