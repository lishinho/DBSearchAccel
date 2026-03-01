package io.github.dbsearchaccel.plugin;

/**
 * 插件基础接口.
 * <p>
 * 所有插件必须实现此接口，定义插件的生命周期方法.
 * </p>
 *
 * @param <C> 插件上下文类型
 * @param <R> 插件执行结果类型
 * @author DBSearchAccel Team
 * @since 1.0.0
 */
public interface DsaPlugin<C extends DsaPluginContext, R extends DsaPluginResult> {

    /**
     * 获取插件名称.
     *
     * @return 插件名称
     */
    String getName();

    /**
     * 获取插件版本.
     *
     * @return 插件版本
     */
    default String getVersion() {
        return "1.0.0";
    }

    /**
     * 获取插件优先级.
     * <p>
     * 数值越小优先级越高，默认为100
     * </p>
     *
     * @return 优先级
     */
    default int getPriority() {
        return 100;
    }

    /**
     * 插件是否启用.
     *
     * @return true表示启用
     */
    default boolean isEnabled() {
        return true;
    }

    /**
     * 初始化插件.
     * <p>
     * 插件加载后调用，用于初始化资源
     * </p>
     */
    default void initialize() {
    }

    /**
     * 销毁插件.
     * <p>
     * 插件卸载前调用，用于释放资源
     * </p>
     */
    default void destroy() {
    }

    /**
     * 执行插件逻辑.
     *
     * @param context 插件上下文
     * @return 执行结果
     */
    R execute(C context);

    /**
     * 是否支持指定上下文.
     *
     * @param context 插件上下文
     * @return true表示支持
     */
    default boolean supports(C context) {
        return true;
    }
}
