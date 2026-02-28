package io.github.dbsearchaccel.core.aggregate;

import io.github.dbsearchaccel.common.model.request.DsaRequest;
import io.github.dbsearchaccel.common.model.response.DsaPageInfo;

import java.util.List;
import java.util.Map;

/**
 * 数据聚合服务接口.
 * <p>
 * 负责将ES查询的全量主键与Redis缓存的实时主键进行聚合，
 * 并根据聚合后的主键ID从数据库查询详情.
 * </p>
 *
 * @author DBSearchAccel Team
 * @since 1.0.0
 */
public interface DsaDataAggregateService {

    /**
     * 聚合ES主键和Redis主键.
     * <p>
     * 将ES查询的全量主键与Redis缓存的实时主键合并、去重
     * </p>
     *
     * @param esIds    ES主键列表
     * @param redisIds Redis主键列表
     * @param request  请求参数
     * @return 聚合后的主键列表
     */
    List<String> aggregateIds(List<String> esIds, List<String> redisIds, DsaRequest request);

    /**
     * 根据主键ID列表查询详情（指定实体类）.
     *
     * @param <T>     实体类型
     * @param ids     主键ID列表
     * @param table   表名
     * @param idField 主键字段名
     * @param clazz   实体类
     * @return 实体列表
     */
    <T> List<T> queryDetails(List<String> ids, String table, String idField, Class<T> clazz);

    /**
     * 根据主键ID列表查询详情（返回Map）.
     *
     * @param ids     主键ID列表
     * @param table   表名
     * @param idField 主键字段名
     * @return Map列表
     */
    List<Map<String, Object>> queryDetails(List<String> ids, String table, String idField);

    /**
     * 构建分页信息.
     *
     * @param total    总记录数
     * @param pageNum  页码
     * @param pageSize 每页大小
     * @return 分页信息
     */
    DsaPageInfo buildPageInfo(long total, int pageNum, int pageSize);
}
