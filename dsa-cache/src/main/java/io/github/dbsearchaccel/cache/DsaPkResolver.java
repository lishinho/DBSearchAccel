package io.github.dbsearchaccel.cache;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * 主键解析器接口.
 * <p>
 * 负责从方法参数中解析主键值，支持多种参数类型.
 * </p>
 *
 * @author DBSearchAccel Team
 * @since 1.0.0
 */
public interface DsaPkResolver {

    /**
     * 从参数中解析主键值.
     *
     * @param param     方法参数
     * @param pkField   主键字段名
     * @return 主键值列表
     */
    List<String> resolve(Object param, String pkField);

    /**
     * 从多个参数中解析主键值.
     *
     * @param params    方法参数数组
     * @param pkField   主键字段名
     * @param paramIndex 参数索引
     * @return 主键值列表
     */
    List<String> resolve(Object[] params, String pkField, int paramIndex);
}
