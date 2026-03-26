package org.son.monitor.common.aop;

import io.micrometer.core.instrument.MeterRegistry;
import io.micrometer.core.instrument.Timer;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.reflect.MethodSignature;
import org.son.monitor.common.exception.BusinessException;
import org.springframework.stereotype.Component;

import java.util.Collection;
import java.util.concurrent.TimeUnit;
import java.util.regex.Pattern;

@Slf4j
@Aspect
@Component
@RequiredArgsConstructor
public class LoggingAspect {

    private final MeterRegistry meterRegistry;

    // password, token, accessToken, refreshToken 필드값 마스킹
    private static final Pattern SENSITIVE_PATTERN = Pattern.compile(
            "(?i)(password|token|accessToken|refreshToken)=([^,)]+)"
    );

    /**
     * @Logging 어노테이션이 붙은 메서드만 AOP 적용.
     * - write(create/update/delete): INFO 레벨
     * - read(find/get): DEBUG 레벨
     */
    @Around("@annotation(org.son.monitor.common.annotation.Logging)")
    public Object logService(ProceedingJoinPoint joinPoint) throws Throwable {
        MethodSignature signature = (MethodSignature) joinPoint.getSignature();
        String className = signature.getDeclaringType().getSimpleName();
        String methodName = signature.getName();
        String[] paramNames = signature.getParameterNames();
        Object[] args = joinPoint.getArgs();

        boolean isWrite = methodName.startsWith("create")
                || methodName.startsWith("update")
                || methodName.startsWith("delete");

        String maskedArgs = maskSensitive(namedArgs(paramNames, args));

        if (isWrite) {
            log.info("[{}] {} args={}", className, methodName, maskedArgs);
        } else {
            log.debug("[{}] {} args={}", className, methodName, maskedArgs);
        }

        long start = System.currentTimeMillis();
        String status = "success";
        try {
            Object result = joinPoint.proceed();
            long elapsed = System.currentTimeMillis() - start;

            if (isWrite) {
                log.info("[{}] {} completed ({}ms) result={}", className, methodName, elapsed, formatResult(result));
            } else {
                log.debug("[{}] {} completed ({}ms) result={}", className, methodName, elapsed, formatResult(result));
            }
            return result;
        } catch (BusinessException t) {
            status = "error";
            throw t;
        } catch (Throwable t) {
            status = "error";
            long elapsed = System.currentTimeMillis() - start;
            log.warn("[{}] {} FAIL ({}ms) exception={} message={}", className, methodName, elapsed, t.getClass().getSimpleName(), t.getMessage());
            throw t;
        } finally {
            long elapsed = System.currentTimeMillis() - start;
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

    /** 파라미터 이름=값 형식으로 조합 */
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

    /** password, token 등 민감 필드를 [REDACTED] 로 치환 */
    private String maskSensitive(String raw) {
        return SENSITIVE_PATTERN.matcher(raw).replaceAll("$1=[REDACTED]");
    }

    /**
     * 결과값 포맷.
     * - Collection/배열: size만 출력 (로그 공해 방지)
     * - 그 외: toString()
     */
    private String formatResult(Object result) {
        if (result == null) return "null";
        if (result instanceof Collection<?> col) {
            return "List(size=" + col.size() + ")";
        }
        if (result instanceof Object[] arr) {
            return "Array(size=" + arr.length + ")";
        }
        return String.valueOf(result);
    }
}
