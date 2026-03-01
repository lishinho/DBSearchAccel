package io.github.dbsearchaccel.plugin;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;

/**
 * 插件执行器.
 * <p>
 * 负责按优先级链式执行插件.
 * </p>
 *
 * @author DBSearchAccel Team
 * @since 1.0.0
 */
@Component
public class DsaPluginExecutor {

    private static final Logger LOGGER = LoggerFactory.getLogger(DsaPluginExecutor.class);

    @Autowired
    private DsaPluginRegistry pluginRegistry;

    /**
     * 执行过滤插件链.
     *
     * @param context 过滤上下文
     * @return 执行结果列表
     */
    public List<DsaPluginResult> executeFilterPlugins(DsaFilterPlugin.FilterContext context) {
        List<DsaFilterPlugin> plugins = pluginRegistry.getPluginsByType(DsaFilterPlugin.class);
        return executeFilterChain(plugins, context);
    }

    /**
     * 执行降级插件链.
     *
     * @param context 降级上下文
     * @return 执行结果列表
     */
    public List<DsaPluginResult> executeFallbackPlugins(DsaFallbackPlugin.FallbackContext context) {
        List<DsaFallbackPlugin> plugins = pluginRegistry.getPluginsByType(DsaFallbackPlugin.class);
        return executeFallbackChain(plugins, context);
    }

    /**
     * 执行同步插件链.
     *
     * @param context 同步上下文
     * @return 执行结果列表
     */
    public List<DsaPluginResult> executeSyncPlugins(DsaSyncPlugin.SyncContext context) {
        List<DsaSyncPlugin> plugins = pluginRegistry.getPluginsByType(DsaSyncPlugin.class);
        return executeSyncChain(plugins, context);
    }

    /**
     * 执行过滤插件链.
     *
     * @param plugins 插件列表
     * @param context 上下文
     * @return 执行结果列表
     */
    private List<DsaPluginResult> executeFilterChain(List<DsaFilterPlugin> plugins,
            DsaFilterPlugin.FilterContext context) {
        List<DsaPluginResult> results = new ArrayList<>();
        if (plugins == null || plugins.isEmpty()) {
            return results;
        }

        for (DsaFilterPlugin plugin : plugins) {
            if (!plugin.isEnabled()) {
                LOGGER.debug("Plugin disabled, skipping: {}", plugin.getName());
                continue;
            }

            try {
                long startTime = System.currentTimeMillis();
                DsaPluginResult result = plugin.execute(context);
                results.add(result);

                long elapsed = System.currentTimeMillis() - startTime;
                LOGGER.debug("Plugin executed: {} ({}ms)", plugin.getName(), elapsed);

                if (!result.isContinueChain()) {
                    LOGGER.info("Plugin chain stopped by: {}", plugin.getName());
                    break;
                }
            } catch (Exception e) {
                LOGGER.error("Plugin execution failed: {}", plugin.getName(), e);
                DsaPluginResult errorResult = DsaPluginResult.failure("Plugin execution failed: " + e.getMessage());
                errorResult.setContinueChain(true);
                results.add(errorResult);
            }
        }

        return results;
    }

    /**
     * 执行降级插件链.
     *
     * @param plugins 插件列表
     * @param context 上下文
     * @return 执行结果列表
     */
    private List<DsaPluginResult> executeFallbackChain(List<DsaFallbackPlugin> plugins,
            DsaFallbackPlugin.FallbackContext context) {
        List<DsaPluginResult> results = new ArrayList<>();
        if (plugins == null || plugins.isEmpty()) {
            return results;
        }

        for (DsaFallbackPlugin plugin : plugins) {
            if (!plugin.isEnabled()) {
                LOGGER.debug("Plugin disabled, skipping: {}", plugin.getName());
                continue;
            }

            try {
                long startTime = System.currentTimeMillis();
                DsaPluginResult result = plugin.execute(context);
                results.add(result);

                long elapsed = System.currentTimeMillis() - startTime;
                LOGGER.debug("Plugin executed: {} ({}ms)", plugin.getName(), elapsed);

                if (!result.isContinueChain()) {
                    LOGGER.info("Plugin chain stopped by: {}", plugin.getName());
                    break;
                }
            } catch (Exception e) {
                LOGGER.error("Plugin execution failed: {}", plugin.getName(), e);
                DsaPluginResult errorResult = DsaPluginResult.failure("Plugin execution failed: " + e.getMessage());
                errorResult.setContinueChain(true);
                results.add(errorResult);
            }
        }

        return results;
    }

    /**
     * 执行同步插件链.
     *
     * @param plugins 插件列表
     * @param context 上下文
     * @return 执行结果列表
     */
    private List<DsaPluginResult> executeSyncChain(List<DsaSyncPlugin> plugins,
            DsaSyncPlugin.SyncContext context) {
        List<DsaPluginResult> results = new ArrayList<>();
        if (plugins == null || plugins.isEmpty()) {
            return results;
        }

        for (DsaSyncPlugin plugin : plugins) {
            if (!plugin.isEnabled()) {
                LOGGER.debug("Plugin disabled, skipping: {}", plugin.getName());
                continue;
            }

            try {
                long startTime = System.currentTimeMillis();
                DsaPluginResult result = plugin.execute(context);
                results.add(result);

                long elapsed = System.currentTimeMillis() - startTime;
                LOGGER.debug("Plugin executed: {} ({}ms)", plugin.getName(), elapsed);

                if (!result.isContinueChain()) {
                    LOGGER.info("Plugin chain stopped by: {}", plugin.getName());
                    break;
                }
            } catch (Exception e) {
                LOGGER.error("Plugin execution failed: {}", plugin.getName(), e);
                DsaPluginResult errorResult = DsaPluginResult.failure("Plugin execution failed: " + e.getMessage());
                errorResult.setContinueChain(true);
                results.add(errorResult);
            }
        }

        return results;
    }

    /**
     * 执行单个插件.
     *
     * @param pluginName 插件名称
     * @param context    上下文
     * @return 执行结果
     */
    public DsaPluginResult executePlugin(String pluginName, DsaPluginContext context) {
        DsaPlugin<?, ?> plugin = pluginRegistry.getPlugin(pluginName);
        if (plugin == null) {
            return DsaPluginResult.failure("Plugin not found: " + pluginName);
        }

        if (!plugin.isEnabled()) {
            return DsaPluginResult.failure("Plugin disabled: " + pluginName);
        }

        try {
            if (plugin instanceof DsaFilterPlugin && context instanceof DsaFilterPlugin.FilterContext) {
                return ((DsaFilterPlugin) plugin).execute((DsaFilterPlugin.FilterContext) context);
            } else if (plugin instanceof DsaFallbackPlugin && context instanceof DsaFallbackPlugin.FallbackContext) {
                return ((DsaFallbackPlugin) plugin).execute((DsaFallbackPlugin.FallbackContext) context);
            } else if (plugin instanceof DsaSyncPlugin && context instanceof DsaSyncPlugin.SyncContext) {
                return ((DsaSyncPlugin) plugin).execute((DsaSyncPlugin.SyncContext) context);
            } else {
                return DsaPluginResult.failure("Plugin context type mismatch");
            }
        } catch (Exception e) {
            LOGGER.error("Plugin execution failed: {}", pluginName, e);
            return DsaPluginResult.failure("Plugin execution failed: " + e.getMessage());
        }
    }
}
