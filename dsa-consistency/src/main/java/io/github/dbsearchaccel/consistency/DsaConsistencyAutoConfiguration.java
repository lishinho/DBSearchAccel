package io.github.dbsearchaccel.consistency;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.annotation.EnableAsync;

/**
 * 一致性校验模块自动配置.
 * <p>
 * Spring Boot自动配置类，自动注册校验相关Bean.
 * </p>
 *
 * @author DBSearchAccel Team
 * @since 1.0.0
 */
@Configuration
@EnableAsync
@ComponentScan(basePackages = "io.github.dbsearchaccel.consistency")
public class DsaConsistencyAutoConfiguration {

    @Bean
    public DsaPrimaryKeyChecker dsaPrimaryKeyChecker() {
        return new DsaPrimaryKeyCheckerImpl();
    }

    @Bean
    public DsaFieldChecker dsaFieldChecker() {
        return new DsaFieldCheckerImpl();
    }

    @Bean
    public DsaCheckReportGenerator dsaCheckReportGenerator() {
        return new DsaCheckReportGeneratorImpl();
    }

    @Bean
    public DsaConsistencyCheckService dsaConsistencyCheckService() {
        return new DsaConsistencyCheckServiceImpl();
    }
}
