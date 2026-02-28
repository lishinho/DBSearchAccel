package io.github.dbsearchaccel.core.filter;

import io.github.dbsearchaccel.common.model.request.DsaRequest;

import java.util.List;

/**
 * 查询校验器接口.
 * <p>
 * 负责对请求参数进行合法性校验，包括：
 * - 必填参数检查
 * - 参数格式校验
 * - 参数范围校验
 * - 业务规则校验
 * </p>
 *
 * @author DBSearchAccel Team
 * @since 1.0.0
 */
public interface DsaQueryValidator {

    /**
     * 校验请求参数.
     *
     * @param request 请求参数
     * @return 校验结果，true表示通过
     * @throws io.github.dbsearchaccel.common.core.exception.DsaException 校验失败时抛出
     */
    boolean validate(DsaRequest request);

    /**
     * 获取校验错误信息.
     *
     * @return 错误信息列表
     */
    List<String> getErrors();

    /**
     * 清空错误信息.
     */
    void clearErrors();
}
