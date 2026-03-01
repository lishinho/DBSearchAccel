package io.github.dbsearchaccel.plugin;

import java.io.Serializable;
import java.util.HashMap;
import java.util.Map;

/**
 * 插件上下文.
 * <p>
 * 插件执行时传递的上下文信息，包含场景编码、请求参数等.
 * </p>
 *
 * @author DBSearchAccel Team
 * @since 1.0.0
 */
public class DsaPluginContext implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * 场景编码
     */
    private String sceneCode;

    /**
     * 操作类型
     */
    private String operationType;

    /**
     * 扩展属性
     */
    private Map<String, Object> attributes;

    public DsaPluginContext() {
        this.attributes = new HashMap<>(16);
    }

    public DsaPluginContext(String sceneCode, String operationType) {
        this();
        this.sceneCode = sceneCode;
        this.operationType = operationType;
    }

    public String getSceneCode() {
        return sceneCode;
    }

    public void setSceneCode(String sceneCode) {
        this.sceneCode = sceneCode;
    }

    public String getOperationType() {
        return operationType;
    }

    public void setOperationType(String operationType) {
        this.operationType = operationType;
    }

    public Map<String, Object> getAttributes() {
        return attributes;
    }

    public void setAttributes(Map<String, Object> attributes) {
        this.attributes = attributes;
    }

    /**
     * 设置属性.
     *
     * @param key   属性键
     * @param value 属性值
     */
    public void setAttribute(String key, Object value) {
        if (this.attributes == null) {
            this.attributes = new HashMap<>(16);
        }
        this.attributes.put(key, value);
    }

    /**
     * 获取属性.
     *
     * @param key 属性键
     * @return 属性值
     */
    public Object getAttribute(String key) {
        if (this.attributes == null) {
            return null;
        }
        return this.attributes.get(key);
    }

    /**
     * 获取属性.
     *
     * @param key   属性键
     * @param clazz 属性类型
     * @param <T>   属性类型
     * @return 属性值
     */
    @SuppressWarnings("unchecked")
    public <T> T getAttribute(String key, Class<T> clazz) {
        Object value = getAttribute(key);
        if (value != null && clazz.isInstance(value)) {
            return (T) value;
        }
        return null;
    }

    /**
     * 移除属性.
     *
     * @param key 属性键
     */
    public void removeAttribute(String key) {
        if (this.attributes != null) {
            this.attributes.remove(key);
        }
    }

    /**
     * 清空属性.
     */
    public void clearAttributes() {
        if (this.attributes != null) {
            this.attributes.clear();
        }
    }
}
