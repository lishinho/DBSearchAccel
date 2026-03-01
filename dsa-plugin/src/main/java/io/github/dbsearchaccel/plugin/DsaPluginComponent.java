package io.github.dbsearchaccel.plugin;

import org.springframework.stereotype.Component;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * 插件注册注解.
 * <p>
 * 用于标记插件实现类，配合Spring自动注册到插件中心.
 * </p>
 *
 * @author DBSearchAccel Team
 * @since 1.0.0
 */
@Target({ElementType.TYPE})
@Retention(RetentionPolicy.RUNTIME)
@Documented
@Component
public @interface DsaPluginComponent {

    /**
     * 插件名称.
     *
     * @return 插件名称
     */
    String name();

    /**
     * 插件版本.
     *
     * @return 插件版本
     */
    String version() default "1.0.0";

    /**
     * 插件优先级.
     * <p>
     * 数值越小优先级越高
     * </p>
     *
     * @return 优先级
     */
    int priority() default 100;

    /**
     * 插件类型.
     *
     * @return 插件类型
     */
    PluginType type() default PluginType.FILTER;

    /**
     * 是否启用.
     *
     * @return true表示启用
     */
    boolean enabled() default true;

    /**
     * 插件类型枚举.
     */
    enum PluginType {
        /**
         * 过滤插件
         */
        FILTER,
        /**
         * 降级插件
         */
        FALLBACK,
        /**
         * 同步插件
         */
        SYNC
    }
}
