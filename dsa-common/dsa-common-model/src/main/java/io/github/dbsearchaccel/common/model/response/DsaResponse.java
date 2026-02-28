package io.github.dbsearchaccel.common.model.response;

import io.github.dbsearchaccel.common.core.enums.DsaResultCode;
import lombok.Data;

import java.io.Serializable;
import java.util.List;

/**
 * 统一返回结果实体.
 * <p>
 * 封装中间件统一返回结果，包含状态码、消息、数据、分页信息等.
 * </p>
 *
 * @param <T> 数据类型
 * @author DBSearchAccel Team
 * @since 1.0.0
 */
@Data
public class DsaResponse<T> implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * 状态码.
     */
    private String code;

    /**
     * 消息.
     */
    private String message;

    /**
     * 数据.
     */
    private T data;

    /**
     * 分页信息.
     */
    private DsaPageInfo pageInfo;

    /**
     * 链路追踪ID.
     */
    private String traceId;

    /**
     * 是否来自降级.
     * <p>
     * 为true表示结果来自降级后的数据库查询
     * </p>
     */
    private boolean fromFallback;

    /**
     * 耗时（毫秒）.
     */
    private long costMillis;

    /**
     * 构建成功响应.
     *
     * @param data 数据
     * @param <T>  数据类型
     * @return 成功响应
     */
    public static <T> DsaResponse<T> success(T data) {
        DsaResponse<T> response = new DsaResponse<>();
        response.setCode(DsaResultCode.SUCCESS.getCode());
        response.setMessage(DsaResultCode.SUCCESS.getMessage());
        response.setData(data);
        response.setFromFallback(false);
        return response;
    }

    /**
     * 构建成功响应（带分页）.
     *
     * @param data     数据
     * @param pageInfo 分页信息
     * @param <T>      数据类型
     * @return 成功响应
     */
    public static <T> DsaResponse<T> success(T data, DsaPageInfo pageInfo) {
        DsaResponse<T> response = success(data);
        response.setPageInfo(pageInfo);
        return response;
    }

    /**
     * 构建错误响应.
     *
     * @param code    错误码
     * @param message 错误消息
     * @param <T>     数据类型
     * @return 错误响应
     */
    public static <T> DsaResponse<T> error(String code, String message) {
        DsaResponse<T> response = new DsaResponse<>();
        response.setCode(code);
        response.setMessage(message);
        response.setFromFallback(false);
        return response;
    }

    /**
     * 构建错误响应.
     *
     * @param resultCode 结果状态码枚举
     * @param <T>        数据类型
     * @return 错误响应
     */
    public static <T> DsaResponse<T> error(DsaResultCode resultCode) {
        return error(resultCode.getCode(), resultCode.getMessage());
    }

    /**
     * 构建降级响应.
     *
     * @param data 数据
     * @param <T>  数据类型
     * @return 降级响应
     */
    public static <T> DsaResponse<T> fallback(T data) {
        DsaResponse<T> response = success(data);
        response.setFromFallback(true);
        return response;
    }

    /**
     * 判断是否成功.
     *
     * @return true表示成功
     */
    public boolean isSuccess() {
        return DsaResultCode.SUCCESS.getCode().equals(code);
    }
}
