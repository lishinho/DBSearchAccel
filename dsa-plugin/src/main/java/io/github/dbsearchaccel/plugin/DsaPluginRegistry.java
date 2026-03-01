package io.github.dbsearchaccel.plugin;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

/**
 * 插件注册中心.
 * <p>
 * 负责插件的注册、管理和查找，支持按类型和场景查找插件.
 * </p>
 *
 * @author DBSearchAccel Team
 * @since 1.0.0
 */
@Component
public class DsaPluginRegistry {

    private static final Logger LOGGER = LoggerFactory.getLogger(DsaPluginRegistry.class);

    /**
     * 所有插件缓存（按名称索引）
     */
    private final Map<String, DsaPlugin<?, ?>> pluginCache = new ConcurrentHashMap<>();

    /**
     * 插件配置缓存（按名称索引）
     */
    private final Map<String, DsaPluginConfig> configCache = new ConcurrentHashMap<>();

    /**
     * 按类型分组的插件（按优先级排序）
     */
    private final Map<String, List<DsaPlugin<?, ?>>> pluginsByType = new ConcurrentHashMap<>();

    /**
     * SPI加载器
     */
    @Autowired(required = false)
    private DsaPluginLoader pluginLoader;

    /**
     * Spring管理的插件列表
     */
    @Autowired(required = false)
    private List<DsaPlugin<?, ?>> springPlugins;

    /**
     * 初始化插件注册中心.
     */
    public void initialize() {
        LOGGER.info("Initializing DsaPluginRegistry...");

        loadSpiPlugins();

        loadSpringPlugins();

        rebuildTypeIndex();

        initializePlugins();

        LOGGER.info("DsaPluginRegistry initialized, total plugins: {}", pluginCache.size());
    }

    /**
     * 销毁插件注册中心.
     */
    public void destroy() {
        LOGGER.info("Destroying DsaPluginRegistry...");

        for (DsaPlugin<?, ?> plugin : pluginCache.values()) {
            try {
                plugin.destroy();
            } catch (Exception e) {
                LOGGER.error("Failed to destroy plugin: {}", plugin.getName(), e);
            }
        }

        pluginCache.clear();
        configCache.clear();
        pluginsByType.clear();

        LOGGER.info("DsaPluginRegistry destroyed");
    }

    /**
     * 加载SPI插件.
     */
    private void loadSpiPlugins() {
        if (pluginLoader == null) {
            return;
        }

        List<DsaPlugin<?, ?>> spiPlugins = pluginLoader.loadPlugins();
        for (DsaPlugin<?, ?> plugin : spiPlugins) {
            registerPlugin(plugin);
        }
    }

    /**
     * 加载Spring管理的插件.
     */
    private void loadSpringPlugins() {
        if (springPlugins == null || springPlugins.isEmpty()) {
            return;
        }

        for (DsaPlugin<?, ?> plugin : springPlugins) {
            registerPlugin(plugin);
        }
    }

    /**
     * 注册插件.
     *
     * @param plugin 插件实例
     */
    public void registerPlugin(DsaPlugin<?, ?> plugin) {
        if (plugin == null) {
            return;
        }

        String name = plugin.getName();
        if (pluginCache.containsKey(name)) {
            LOGGER.warn("Plugin already registered, will be replaced: {}", name);
        }

        pluginCache.put(name, plugin);

        DsaPluginConfig config = createConfig(plugin);
        configCache.put(name, config);

        rebuildTypeIndex();

        LOGGER.info("Plugin registered: {} (version={}, priority={}, enabled={})",
                name, plugin.getVersion(), plugin.getPriority(), plugin.isEnabled());
    }

    /**
     * 注销插件.
     *
     * @param name 插件名称
     */
    public void unregisterPlugin(String name) {
        if (name == null) {
            return;
        }

        DsaPlugin<?, ?> plugin = pluginCache.remove(name);
        if (plugin != null) {
            try {
                plugin.destroy();
            } catch (Exception e) {
                LOGGER.error("Failed to destroy plugin: {}", name, e);
            }
            configCache.remove(name);
            rebuildTypeIndex();
            LOGGER.info("Plugin unregistered: {}", name);
        }
    }

    /**
     * 获取插件.
     *
     * @param name 插件名称
     * @return 插件实例
     */
    public DsaPlugin<?, ?> getPlugin(String name) {
        return pluginCache.get(name);
    }

    /**
     * 获取插件配置.
     *
     * @param name 插件名称
     * @return 插件配置
     */
    public DsaPluginConfig getPluginConfig(String name) {
        return configCache.get(name);
    }

    /**
     * 获取指定类型的插件列表（按优先级排序）.
     *
     * @param type 插件类型
     * @return 插件列表
     */
    @SuppressWarnings("unchecked")
    public <P extends DsaPlugin<?, ?>> List<P> getPluginsByType(Class<P> type) {
        String typeName = type.getSimpleName();
        List<DsaPlugin<?, ?>> plugins = pluginsByType.get(typeName);
        if (plugins == null) {
            return Collections.emptyList();
        }
        return plugins.stream()
                .filter(p -> type.isInstance(p) && p.isEnabled())
                .map(p -> (P) p)
                .collect(Collectors.toList());
    }

    /**
     * 获取所有插件.
     *
     * @return 插件列表
     */
    public List<DsaPlugin<?, ?>> getAllPlugins() {
        return new ArrayList<>(pluginCache.values());
    }

    /**
     * 获取所有启用的插件.
     *
     * @return 插件列表
     */
    public List<DsaPlugin<?, ?>> getEnabledPlugins() {
        return pluginCache.values().stream()
                .filter(DsaPlugin::isEnabled)
                .collect(Collectors.toList());
    }

    /**
     * 启用插件.
     *
     * @param name 插件名称
     */
    public void enablePlugin(String name) {
        DsaPluginConfig config = configCache.get(name);
        if (config != null) {
            config.setEnabled(true);
            LOGGER.info("Plugin enabled: {}", name);
        }
    }

    /**
     * 禁用插件.
     *
     * @param name 插件名称
     */
    public void disablePlugin(String name) {
        DsaPluginConfig config = configCache.get(name);
        if (config != null) {
            config.setEnabled(false);
            LOGGER.info("Plugin disabled: {}", name);
        }
    }

    /**
     * 创建插件配置.
     *
     * @param plugin 插件实例
     * @return 插件配置
     */
    private DsaPluginConfig createConfig(DsaPlugin<?, ?> plugin) {
        DsaPluginConfig config = new DsaPluginConfig();
        config.setName(plugin.getName());
        config.setVersion(plugin.getVersion());
        config.setPriority(plugin.getPriority());
        config.setEnabled(plugin.isEnabled());
        config.setClassName(plugin.getClass().getName());

        if (plugin instanceof DsaFilterPlugin) {
            config.setType("FILTER");
        } else if (plugin instanceof DsaFallbackPlugin) {
            config.setType("FALLBACK");
        } else if (plugin instanceof DsaSyncPlugin) {
            config.setType("SYNC");
        } else {
            config.setType("GENERIC");
        }

        return config;
    }

    /**
     * 重建类型索引.
     */
    private void rebuildTypeIndex() {
        pluginsByType.clear();

        Map<String, List<DsaPlugin<?, ?>>> filterPlugins = new HashMap<>();
        Map<String, List<DsaPlugin<?, ?>>> fallbackPlugins = new HashMap<>();
        Map<String, List<DsaPlugin<?, ?>>> syncPlugins = new HashMap<>();

        for (DsaPlugin<?, ?> plugin : pluginCache.values()) {
            if (plugin instanceof DsaFilterPlugin) {
                filterPlugins.computeIfAbsent("DsaFilterPlugin", k -> new ArrayList<>()).add(plugin);
            } else if (plugin instanceof DsaFallbackPlugin) {
                fallbackPlugins.computeIfAbsent("DsaFallbackPlugin", k -> new ArrayList<>()).add(plugin);
            } else if (plugin instanceof DsaSyncPlugin) {
                syncPlugins.computeIfAbsent("DsaSyncPlugin", k -> new ArrayList<>()).add(plugin);
            }
        }

        sortAndPut(filterPlugins, "DsaFilterPlugin");
        sortAndPut(fallbackPlugins, "DsaFallbackPlugin");
        sortAndPut(syncPlugins, "DsaSyncPlugin");
    }

    /**
     * 排序并放入索引.
     */
    private void sortAndPut(Map<String, List<DsaPlugin<?, ?>>> map, String key) {
        List<DsaPlugin<?, ?>> list = map.get(key);
        if (list != null) {
            list.sort(Comparator.comparingInt(DsaPlugin::getPriority));
            pluginsByType.put(key, list);
        }
    }

    /**
     * 初始化所有插件.
     */
    private void initializePlugins() {
        for (DsaPlugin<?, ?> plugin : pluginCache.values()) {
            try {
                plugin.initialize();
            } catch (Exception e) {
                LOGGER.error("Failed to initialize plugin: {}", plugin.getName(), e);
            }
        }
    }
}
