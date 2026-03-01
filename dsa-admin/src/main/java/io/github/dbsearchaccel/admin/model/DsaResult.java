package io.github.dbsearchaccel.admin.model;

import lombok.Data;
import java.io.Serializable;

/**
 * 统一返回结果封装
 *
 * @param <T> 数据类型
 * @author DBSearchAccel Team
 */
@Data
public class DsaResult<T> implements Serializable {

    private static final long serialVersionUID = 1L;

    private Integer code;

    private String message;

    private T data;

    public static <T> DsaResult<T> success(T data) {
        DsaResult<T> result = new DsaResult<>();
        result.setCode(200);
        result.setMessage("success");
        result.setData(data);
        return result;
    }

    public static <T> DsaResult<T> success() {
        return success(null);
    }

    public static <T> DsaResult<T> error(String message) {
        DsaResult<T> result = new DsaResult<>();
        result.setCode(500);
        result.setMessage(message);
        return result;
    }

    public static <T> DsaResult<T> error(Integer code, String message) {
        DsaResult<T> result = new DsaResult<>();
        result.setCode(code);
        result.setMessage(message);
        return result;
    }
}
