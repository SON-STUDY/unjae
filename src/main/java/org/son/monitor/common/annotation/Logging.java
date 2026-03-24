package org.son.monitor.common.annotation;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * 서비스 메서드에 붙이면 AOP가 실행 시간·파라미터·결과를 로깅합니다.
 * 민감 필드(password, secret 등)는 자동으로 **** 마스킹 처리됩니다.
 */
@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
public @interface Logging {
}
