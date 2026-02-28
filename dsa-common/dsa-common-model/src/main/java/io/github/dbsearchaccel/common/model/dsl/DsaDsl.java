package io.github.dbsearchaccel.common.model.dsl;

import lombok.Data;

import java.io.Serializable;
import java.util.List;
import java.util.Map;

/**
 * ES DSL封装实体.
 * <p>
 * 封装Elasticsearch查询DSL的相关参数，包括索引、查询条件、排序、分页等.
 * </p>
 *
 * @author DBSearchAccel Team
 * @since 1.0.0
 */
@Data
public class DsaDsl implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * 索引名称.
     */
    private String index;

    /**
     * 查询条件.
     * <p>
     * ES DSL查询条件的Map表示
     * </p>
     */
    private Map<String, Object> query;

    /**
     * 排序条件列表.
     */
    private List<Map<String, Object>> sorts;

    /**
     * 分页起始位置.
     */
    private Integer from;

    /**
     * 分页大小.
     */
    private Integer size;

    /**
     * 返回字段包含列表.
     */
    private String[] sourceIncludes;

    /**
     * 返回字段排除列表.
     */
    private String[] sourceExcludes;

    /**
     * 静态工厂方法.
     *
     * @param index 索引名称
     * @return DsaDsl实例
     */
    public static DsaDsl of(String index) {
        DsaDsl dsl = new DsaDsl();
        dsl.setIndex(index);
        return dsl;
    }

    /**
     * 设置查询条件.
     *
     * @param query 查询条件
     * @return 当前实例
     */
    public DsaDsl query(Map<String, Object> query) {
        this.query = query;
        return this;
    }

    /**
     * 设置分页起始位置.
     *
     * @param from 分页起始位置
     * @return 当前实例
     */
    public DsaDsl from(Integer from) {
        this.from = from;
        return this;
    }

    /**
     * 设置分页大小.
     *
     * @param size 分页大小
     * @return 当前实例
     */
    public DsaDsl size(Integer size) {
        this.size = size;
        return this;
    }

    /**
     * 添加排序条件.
     *
     * @param field 排序字段
     * @param order 排序方式（ASC/DESC）
     * @return 当前实例
     */
    public DsaDsl addSort(String field, String order) {
        if (sorts == null) {
            sorts = new java.util.ArrayList<>();
        }
        Map<String, Object> sortItem = new java.util.HashMap<>();
        Map<String, Object> sortConfig = new java.util.HashMap<>();
        sortConfig.put("order", order);
        sortItem.put(field, sortConfig);
        sorts.add(sortItem);
        return this;
    }
}
