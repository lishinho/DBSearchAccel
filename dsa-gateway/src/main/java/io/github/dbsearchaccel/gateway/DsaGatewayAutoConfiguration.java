package io.github.dbsearchaccel.gateway;

import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;

/**
 * 网关模块自动配置.
 * <p>
 * Spring Boot自动配置类，自动注册网关相关Bean.
 * </p>
 *
 * @author DBSearchAccel Team
 * @since 1.0.0
 */
@Configuration
@ComponentScan(basePackages = "io.github.dbsearchaccel.gateway")
@EnableConfigurationProperties(DsaGatewayProperties.class)
public class DsaGatewayAutoConfiguration {

    @Bean
    public DsaRequestValidator dsaRequestValidator() {
        return new DsaRequestValidatorImpl();
    }

    @Bean
    public DsaRateLimiter dsaRateLimiter() {
        return new DsaRateLimiterImpl();
    }
}
