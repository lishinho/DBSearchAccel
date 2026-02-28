package io.github.dbsearchaccel.core.aggregate;

import io.github.dbsearchaccel.common.model.request.DsaRequest;
import io.github.dbsearchaccel.common.model.response.DsaPageInfo;
import io.github.dbsearchaccel.infra.db.DsaDbAccessor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.*;
import java.util.stream.Collectors;

public class DsaDataAggregateServiceImpl implements DsaDataAggregateService {

    private static final Logger log = LoggerFactory.getLogger(DsaDataAggregateServiceImpl.class);

    private final DsaDbAccessor dbAccessor;

    public DsaDataAggregateServiceImpl(DsaDbAccessor dbAccessor) {
        this.dbAccessor = dbAccessor;
    }

    @Override
    public List<String> aggregateIds(List<String> esIds, List<String> redisIds, DsaRequest request) {
        Set<String> aggregatedIds = new LinkedHashSet<>();

        if (redisIds != null && !redisIds.isEmpty()) {
            aggregatedIds.addAll(redisIds);
        }

        if (esIds != null && !esIds.isEmpty()) {
            aggregatedIds.addAll(esIds);
        }

        List<String> sortedIds = new ArrayList<>(aggregatedIds);

        if (request.getSortField() != null) {
            log.debug("Sort ids by field: {}, order: {}", request.getSortField(), request.getSortOrder());
        }

        log.debug("Aggregated ids count: {}", sortedIds.size());
        return sortedIds;
    }

    @Override
    public <T> List<T> queryDetails(List<String> ids, String table, String idField, Class<T> clazz) {
        if (ids == null || ids.isEmpty()) {
            return new ArrayList<>();
        }

        int pageNum = 1;
        int pageSize = ids.size();

        int from = 0;
        int to = ids.size();

        List<String> pageIds = ids.subList(from, to);
        log.debug("Query details from table: {}, idField: {}, count: {}", table, idField, pageIds.size());

        return dbAccessor.queryByIds(table, pageIds, idField, clazz);
    }

    @Override
    public List<Map<String, Object>> queryDetails(List<String> ids, String table, String idField) {
        if (ids == null || ids.isEmpty()) {
            return new ArrayList<>();
        }

        log.debug("Query details from table: {}, idField: {}, count: {}", table, idField, ids.size());
        return dbAccessor.queryByIds(table, ids, idField);
    }

    @Override
    public DsaPageInfo buildPageInfo(long total, int pageNum, int pageSize) {
        return DsaPageInfo.of(total, pageNum, pageSize);
    }
}
