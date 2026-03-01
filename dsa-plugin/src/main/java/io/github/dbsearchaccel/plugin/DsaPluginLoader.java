package io.github.dbsearchaccel.plugin;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.List;
import java.util.ServiceLoader;

/**
 * SPI插件加载器.
 * <p>
 * 基于Java ServiceLoader机制加载插件实现类.
 * </p>
 *
 * @author DBSearchAccel Team
 * @since 1.0.0
 */
public class DsaPluginLoader {

    private static final Logger LOGGER = LoggerFactory.getLogger(DsaPluginLoader.class);

    /**
     * 加载所有插件.
     *
     * @return 插件列表
     */
    @SuppressWarnings("unchecked")
    public List<DsaPlugin<?, ?>> loadPlugins() {
        List<DsaPlugin<?, ?>> plugins = new ArrayList<>();

        loadPluginsByType(DsaFilterPlugin.class, plugins);
        loadPluginsByType(DsaFallbackPlugin.class, plugins);
        loadPluginsByType(DsaSyncPlugin.class, plugins);

        ServiceLoader<DsaPlugin> genericLoader = ServiceLoader.load(DsaPlugin.class);
        for (DsaPlugin<?, ?> plugin : genericLoader) {
            if (!containsPlugin(plugins, plugin.getName())) {
                plugins.add(plugin);
                LOGGER.info("Loaded generic plugin via SPI: {}", plugin.getName());
            }
        }

        LOGGER.info("Total plugins loaded via SPI: {}", plugins.size());
        return plugins;
    }

    /**
     * 加载指定类型的插件.
     *
     * @param pluginType 插件类型
     * @param plugins    插件列表
     */
    private <P extends DsaPlugin<?, ?>> void loadPluginsByType(Class<P> pluginType, List<DsaPlugin<?, ?>> plugins) {
        try {
            ServiceLoader<P> loader = ServiceLoader.load(pluginType);
            for (P plugin : loader) {
                if (!containsPlugin(plugins, plugin.getName())) {
                    plugins.add(plugin);
                    LOGGER.info("Loaded {} plugin via SPI: {}", pluginType.getSimpleName(), plugin.getName());
                }
            }
        } catch (Exception e) {
            LOGGER.error("Failed to load plugins of type: {}", pluginType.getName(), e);
        }
    }

    /**
     * 检查插件是否已存在.
     *
     * @param plugins 插件列表
     * @param name    插件名称
     * @return true表示已存在
     */
    private boolean containsPlugin(List<DsaPlugin<?, ?>> plugins, String name) {
        return plugins.stream().anyMatch(p -> p.getName().equals(name));
    }

    /**
     * 加载指定类型的插件.
     *
     * @param pluginClass 插件类
     * @param <P>         插件类型
     * @return 插件列表
     */
    public <P extends DsaPlugin<?, ?>> List<P> loadPlugins(Class<P> pluginClass) {
        List<P> plugins = new ArrayList<>();
        try {
            ServiceLoader<P> loader = ServiceLoader.load(pluginClass);
            for (P plugin : loader) {
                plugins.add(plugin);
                LOGGER.info("Loaded plugin via SPI: {} ({})", plugin.getName(), pluginClass.getSimpleName());
            }
        } catch (Exception e) {
            LOGGER.error("Failed to load plugins of type: {}", pluginClass.getName(), e);
        }
        return plugins;
    }
}
