package io.github.dbsearchaccel.fallback;

import io.github.dbsearchaccel.infra.db.DsaDbAccessor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * 降级模块配置类.
 * <p>
 * 配置降级服务相关Bean.
 * </p>
 *
 * @author DBSearchAccel Team
 * @since 1.0.0
 */
@Configuration
public class DsaFallbackConfig {

    /**
     * 创建降级服务Bean.
     *
     * @param dbAccessor 数据库访问器
     * @return DsaFallbackService实例
     */
    @Bean
    public DsaFallbackService dsaFallbackService(DsaDbAccessor dbAccessor) {
        return new DsaFallbackServiceImpl(dbAccessor);
    }
}
