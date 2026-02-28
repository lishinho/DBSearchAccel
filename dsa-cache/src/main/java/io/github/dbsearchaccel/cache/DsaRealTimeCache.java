package io.github.dbsearchaccel.cache;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * 实时缓存注解.
 * <p>
 * 标注在业务增删改方法上，用于自动采集主键ID并更新到Redis缓存.
 * 配合DsaCacheAspect切面实现AOP拦截.
 * </p>
 *
 * <p>使用示例：</p>
 * <pre>
 * &#64;DsaRealTimeCache(sceneCode = "ORDER_QUERY", operation = CacheOperation.INSERT)
 * public void insertOrder(Order order) {
 *     orderMapper.insert(order);
 * }
 * </pre>
 *
 * @author DBSearchAccel Team
 * @since 1.0.0
 */
@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
public @interface DsaRealTimeCache {

    /**
     * 场景编码.
     * <p>
     * 用于关联场景配置，确定缓存key前缀等
     * </p>
     *
     * @return 场景编码
     */
    String sceneCode();

    /**
     * 缓存操作类型.
     *
     * @return 操作类型
     */
    CacheOperation operation();

    /**
     * 主键字段名.
     * <p>
     * 默认为"id"，用于从方法参数中提取主键值
     * </p>
     *
     * @return 主键字段名
     */
    String pkField() default "id";

    /**
     * 参数索引.
     * <p>
     * 指定从第几个参数提取主键，从0开始，默认为0
     * </p>
     *
     * @return 参数索引
     */
    int paramIndex() default 0;

    /**
     * 是否异步执行.
     * <p>
     * 默认为true，异步更新缓存不影响主流程性能
     * </p>
     *
     * @return true表示异步执行
     */
    boolean async() default true;

    /**
     * 缓存操作类型枚举.
     */
    enum CacheOperation {
        /**
         * 插入操作-添加主键到缓存.
         */
        INSERT,

        /**
         * 更新操作-更新缓存中的主键.
         */
        UPDATE,

        /**
         * 删除操作-从缓存中移除主键.
         */
        DELETE
    }
}
