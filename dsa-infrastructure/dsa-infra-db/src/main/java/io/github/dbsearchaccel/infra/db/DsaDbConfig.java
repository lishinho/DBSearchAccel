package io.github.dbsearchaccel.infra.db;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.jdbc.core.JdbcTemplate;

import javax.sql.DataSource;

/**
 * 数据库配置类.
 * <p>
 * 配置数据库访问相关Bean.
 * </p>
 *
 * @author DBSearchAccel Team
 * @since 1.0.0
 */
@Configuration
public class DsaDbConfig {

    /**
     * 创建JdbcTemplate Bean.
     *
     * @param dataSource 数据源
     * @return JdbcTemplate实例
     */
    @Bean
    public JdbcTemplate jdbcTemplate(DataSource dataSource) {
        return new JdbcTemplate(dataSource);
    }

    /**
     * 创建DSA数据库访问器Bean.
     *
     * @param jdbcTemplate JdbcTemplate
     * @return DsaDbAccessor实例
     */
    @Bean
    public DsaDbAccessor dsaDbAccessor(JdbcTemplate jdbcTemplate) {
        return new DsaDbAccessorImpl(jdbcTemplate);
    }
}
