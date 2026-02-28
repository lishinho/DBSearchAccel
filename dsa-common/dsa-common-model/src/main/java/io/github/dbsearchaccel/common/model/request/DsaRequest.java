package io.github.dbsearchaccel.common.model.request;

import io.github.dbsearchaccel.common.core.enums.DsaSceneType;
import lombok.Data;

import java.io.Serializable;
import java.util.Map;

/**
 * 统一请求参数实体.
 * <p>
 * 封装中间件统一接受的请求参数，包含场景类型、查询条件、分页排序等.
 * </p>
 *
 * @author DBSearchAccel Team
 * @since 1.0.0
 */
@Data
public class DsaRequest implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * 链路追踪ID.
     * <p>
     * 用于全链路追踪，     * </p>
     */
    private String traceId;

    /**
     * 业务场景类型.
     * <p>
     * 用于场景路由匹配
     * </p>
     */
    private DsaSceneType sceneType;

    /**
     * 业务场景编码.
     * <p>
     * 当sceneType为CUSTOM时使用
     * </p>
     */
    private String sceneCode;

    /**
     * 查询条件参数.
     * <p>
     * Key为字段名，Value为字段值
     * </p>
     */
    private Map<String, Object> queryParams;

    /**
     * 页码.
     * <p>
     * 默认值为1
     * </p>
     */
    private Integer pageNum = 1;

    /**
     * 每页大小.
     * <p>
     * 默认值为10
     * </p>
     */
    private Integer pageSize = 10;

    /**
     * 排序字段.
     */
    private String sortField;

    /**
     * 排序方式.
     * <p>
     * 可选值：ASC、DESC，默认为DESC
     * </p>
     */
    private String sortOrder = "DESC";

    /**
     * 灰度标识.
     * <p>
     * 用于灰度规则匹配，如商户ID、用户ID
     * </p>
     */
    private String grayKey;

    /**
     * 是否强制降级.
     * <p>
     * 为true时直接走数据库查询
     * </p>
     */
    private boolean forceFallback = false;

    /**
     * 获取场景类型编码.
     * <p>
     * 优先返回sceneType的编码，若sceneType为null则返回sceneCode
     * </p>
     *
     * @return 场景类型编码
     */
    public String getSceneTypeCode() {
        if (sceneType != null) {
            return sceneType.getCode();
        }
        return sceneCode;
    }

    /**
     * 计算分页偏移量.
     *
     * @return 偏移量 = (pageNum - 1) * pageSize
     */
    public int getOffset() {
        return (pageNum - 1) * pageSize;
    }
}
