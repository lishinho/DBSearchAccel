package io.github.dbsearchaccel.plugin;

import java.io.Serializable;

/**
 * 插件执行结果.
 * <p>
 * 封装插件执行的结果信息.
 * </p>
 *
 * @author DBSearchAccel Team
 * @since 1.0.0
 */
public class DsaPluginResult implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * 执行是否成功
     */
    private boolean success;

    /**
     * 结果数据
     */
    private Object data;

    /**
     * 错误信息
     */
    private String errorMessage;

    /**
     * 是否继续执行后续插件
     */
    private boolean continueChain;

    public DsaPluginResult() {
        this.success = true;
        this.continueChain = true;
    }

    public DsaPluginResult(boolean success, Object data) {
        this();
        this.success = success;
        this.data = data;
    }

    public DsaPluginResult(boolean success, String errorMessage) {
        this();
        this.success = success;
        this.errorMessage = errorMessage;
        this.continueChain = false;
    }

    /**
     * 创建成功结果.
     *
     * @param data 结果数据
     * @return 插件结果
     */
    public static DsaPluginResult success(Object data) {
        return new DsaPluginResult(true, data);
    }

    /**
     * 创建成功结果（继续执行）.
     *
     * @param data          结果数据
     * @param continueChain 是否继续执行
     * @return 插件结果
     */
    public static DsaPluginResult success(Object data, boolean continueChain) {
        DsaPluginResult result = new DsaPluginResult(true, data);
        result.setContinueChain(continueChain);
        return result;
    }

    /**
     * 创建失败结果.
     *
     * @param errorMessage 错误信息
     * @return 插件结果
     */
    public static DsaPluginResult failure(String errorMessage) {
        return new DsaPluginResult(false, errorMessage);
    }

    /**
     * 创建失败结果（中断执行）.
     *
     * @param errorMessage  错误信息
     * @param continueChain 是否继续执行
     * @return 插件结果
     */
    public static DsaPluginResult failure(String errorMessage, boolean continueChain) {
        DsaPluginResult result = new DsaPluginResult(false, errorMessage);
        result.setContinueChain(continueChain);
        return result;
    }

    public boolean isSuccess() {
        return success;
    }

    public void setSuccess(boolean success) {
        this.success = success;
    }

    public Object getData() {
        return data;
    }

    public void setData(Object data) {
        this.data = data;
    }

    public String getErrorMessage() {
        return errorMessage;
    }

    public void setErrorMessage(String errorMessage) {
        this.errorMessage = errorMessage;
    }

    public boolean isContinueChain() {
        return continueChain;
    }

    public void setContinueChain(boolean continueChain) {
        this.continueChain = continueChain;
    }
}
