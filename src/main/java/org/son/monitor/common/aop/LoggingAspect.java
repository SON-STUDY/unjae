package org.son.monitor.common.aop;

import io.micrometer.core.instrument.MeterRegistry;
import io.micrometer.core.instrument.Timer;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.reflect.MethodSignature;
import org.springframework.stereotype.Component;

import java.util.concurrent.TimeUnit;

@Slf4j
@Aspect
@Component
@RequiredArgsConstructor
public class LoggingAspect {

    private final MeterRegistry meterRegistry;

    @Around("execution(* org.son.monitor.*.application.*.*(..))")
    public Object logService(ProceedingJoinPoint joinPoint) throws Throwable {
        MethodSignature signature = (MethodSignature) joinPoint.getSignature();
        String className = signature.getDeclaringType().getSimpleName();
        String methodName = signature.getName();
        String[] paramNames = signature.getParameterNames();
        Object[] args = joinPoint.getArgs();

        boolean isWrite = methodName.startsWith("create")
                || methodName.startsWith("update")
                || methodName.startsWith("delete");

        if (isWrite) {
            log.info("[{}] {} 호출 - {}", className, methodName, namedArgs(paramNames, args));
        } else {
            log.debug("[{}] {} 호출 - {}", className, methodName, namedArgs(paramNames, args));
        }

        long start = System.currentTimeMillis();
        String status = "success";
        try {
            Object result = joinPoint.proceed();
            long elapsed = System.currentTimeMillis() - start;

            if (isWrite) {
                log.info("[{}] {} 완료 ({}ms) → {}", className, methodName, elapsed, result);
            } else {
                log.debug("[{}] {} 완료 ({}ms)", className, methodName, elapsed);
            }
            return result;
        } catch (Throwable t) {
            status = "error";
            throw t;
        } finally {
            long elapsed = System.currentTimeMillis() - start;
            // 서비스 레이어 실행 시간 기록 (p95/p99 집계용 히스토그램)
            Timer.builder("service.method.duration")
                    .description("Service layer method execution time")
                    .tag("class", className)
                    .tag("method", methodName)
                    .tag("status", status)
                    .publishPercentileHistogram()
                    .register(meterRegistry)
                    .record(elapsed, TimeUnit.MILLISECONDS);
        }
    }

    private String namedArgs(String[] names, Object[] args) {
        if (args == null || args.length == 0) return "()";
        StringBuilder sb = new StringBuilder("(");
        for (int i = 0; i < args.length; i++) {
            if (names != null && i < names.length) {
                sb.append(names[i]).append("=");
            }
            sb.append(args[i]);
            if (i < args.length - 1) sb.append(", ");
        }
        sb.append(")");
        return sb.toString();
    }
}
