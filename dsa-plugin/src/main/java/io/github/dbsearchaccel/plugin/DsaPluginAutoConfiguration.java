package io.github.dbsearchaccel.plugin;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;

/**
 * 插件模块自动配置.
 * <p>
 * Spring Boot自动配置类，自动注册插件相关Bean.
 * </p>
 *
 * @author DBSearchAccel Team
 * @since 1.0.0
 */
@Configuration
@ComponentScan(basePackages = "io.github.dbsearchaccel.plugin")
public class DsaPluginAutoConfiguration {

    /**
     * 配置SPI插件加载器.
     *
     * @return 插件加载器
     */
    @Bean
    public DsaPluginLoader dsaPluginLoader() {
        return new DsaPluginLoader();
    }
}
