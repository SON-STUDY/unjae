package org.son.monitor.common.config;

import io.micrometer.core.instrument.MeterRegistry;
import io.micrometer.core.instrument.config.MeterFilter;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.actuate.autoconfigure.metrics.MeterRegistryCustomizer;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class MetricsConfig {

    /**
     * 모든 메트릭에 application 공통 태그 추가 + 불필요한 URI 필터링
     */
    @Bean
    public MeterRegistryCustomizer<MeterRegistry> metricsCommonTags(
            @Value("${spring.application.name:monitor}") String appName) {
        return registry -> registry.config()
                .commonTags("application", appName)
                // actuator 엔드포인트, static 리소스는 메트릭 제외
                .meterFilter(MeterFilter.deny(id -> {
                    String uri = id.getTag("uri");
                    return uri != null && (
                            uri.startsWith("/actuator") ||
                            uri.startsWith("/favicon") ||
                            uri.equals("/error")
                    );
                }));
    }
}
