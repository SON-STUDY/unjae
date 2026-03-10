package org.son.monitor.common.aop;

import lombok.extern.slf4j.Slf4j;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.reflect.MethodSignature;
import org.springframework.stereotype.Component;

@Slf4j
@Aspect
@Component
public class LoggingAspect {

    // application 패키지 하위 모든 메서드 대상
    @Around("execution(* org.son.monitor.*.application.*.*(..))")
    public Object logService(ProceedingJoinPoint joinPoint) throws Throwable {
        MethodSignature signature = (MethodSignature) joinPoint.getSignature();
        String className = signature.getDeclaringType().getSimpleName();
        String methodName = signature.getName();
        Object[] args = joinPoint.getArgs();

        log.info("[{}] {}{} 호출", className, methodName, argsToString(args));

        long start = System.currentTimeMillis();
        try {
            Object result = joinPoint.proceed();
            long elapsed = System.currentTimeMillis() - start;
            log.info("[{}] {} 완료 ({}ms)", className, methodName, elapsed);
            return result;
        } catch (Exception e) {
            long elapsed = System.currentTimeMillis() - start;
            log.error("[{}] {} 예외 발생 ({}ms) - {}", className, methodName, elapsed, e.getMessage());
            throw e;
        }
    }

    private String argsToString(Object[] args) {
        if (args == null || args.length == 0) return "()";
        StringBuilder sb = new StringBuilder("(");
        for (int i = 0; i < args.length; i++) {
            sb.append(args[i]);
            if (i < args.length - 1) sb.append(", ");
        }
        sb.append(")");
        return sb.toString();
    }
}