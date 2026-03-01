package io.github.dbsearchaccel.plugin;

import java.io.Serializable;

/**
 * 插件配置实体.
 * <p>
 * 定义插件的配置信息，包括名称、版本、优先级、启用状态等.
 * </p>
 *
 * @author DBSearchAccel Team
 * @since 1.0.0
 */
public class DsaPluginConfig implements Serializable, Comparable<DsaPluginConfig> {

    private static final long serialVersionUID = 1L;

    /**
     * 插件名称
     */
    private String name;

    /**
     * 插件版本
     */
    private String version;

    /**
     * 插件优先级
     */
    private int priority;

    /**
     * 插件类型
     */
    private String type;

    /**
     * 是否启用
     */
    private boolean enabled;

    /**
     * 插件类名
     */
    private String className;

    /**
     * 场景编码（可选，用于场景级插件）
     */
    private String sceneCode;

    /**
     * 扩展配置
     */
    private String extendedConfig;

    public DsaPluginConfig() {
    }

    public DsaPluginConfig(String name, String version, int priority) {
        this.name = name;
        this.version = version;
        this.priority = priority;
        this.enabled = true;
    }

    @Override
    public int compareTo(DsaPluginConfig other) {
        return Integer.compare(this.priority, other.priority);
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getVersion() {
        return version;
    }

    public void setVersion(String version) {
        this.version = version;
    }

    public int getPriority() {
        return priority;
    }

    public void setPriority(int priority) {
        this.priority = priority;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public boolean isEnabled() {
        return enabled;
    }

    public void setEnabled(boolean enabled) {
        this.enabled = enabled;
    }

    public String getClassName() {
        return className;
    }

    public void setClassName(String className) {
        this.className = className;
    }

    public String getSceneCode() {
        return sceneCode;
    }

    public void setSceneCode(String sceneCode) {
        this.sceneCode = sceneCode;
    }

    public String getExtendedConfig() {
        return extendedConfig;
    }

    public void setExtendedConfig(String extendedConfig) {
        this.extendedConfig = extendedConfig;
    }
}
