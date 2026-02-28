package io.github.dbsearchaccel.infra.es;

import io.github.dbsearchaccel.common.core.exception.DsaEsException;
import io.github.dbsearchaccel.common.model.dsl.DsaDsl;
import org.elasticsearch.action.admin.indices.delete.DeleteIndexRequest;
import org.elasticsearch.action.bulk.BulkRequest;
import org.elasticsearch.action.bulk.BulkResponse;
import org.elasticsearch.action.delete.DeleteRequest;
import org.elasticsearch.action.delete.DeleteResponse;
import org.elasticsearch.action.get.GetRequest;
import org.elasticsearch.action.get.GetResponse;
import org.elasticsearch.action.index.IndexRequest;
import org.elasticsearch.action.index.IndexResponse;
import org.elasticsearch.action.search.SearchRequest;
import org.elasticsearch.action.search.SearchResponse;
import org.elasticsearch.action.support.master.AcknowledgedResponse;
import org.elasticsearch.client.RequestOptions;
import org.elasticsearch.client.RestHighLevelClient;
import org.elasticsearch.client.indices.CreateIndexRequest;
import org.elasticsearch.client.indices.CreateIndexResponse;
import org.elasticsearch.client.indices.GetIndexRequest;
import org.elasticsearch.index.query.QueryBuilders;
import org.elasticsearch.search.SearchHit;
import org.elasticsearch.search.builder.SearchSourceBuilder;
import org.elasticsearch.search.sort.SortOrder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class DsaEsClientImpl implements DsaEsClient {

    private static final Logger log = LoggerFactory.getLogger(DsaEsClientImpl.class);

    private final RestHighLevelClient client;

    public DsaEsClientImpl(RestHighLevelClient client) {
        this.client = client;
    }

    @Override
    public List<String> queryIds(DsaDsl dsl) {
        try {
            SearchRequest request = buildSearchRequest(dsl);
            request.source().fetchSource(false);

            SearchResponse response = client.search(request, RequestOptions.DEFAULT);
            List<String> ids = new ArrayList<>();
            for (SearchHit hit : response.getHits().getHits()) {
                ids.add(hit.getId());
            }
            return ids;
        } catch (IOException e) {
            log.error("ES query ids error, index: {}", dsl.getIndex(), e);
            throw new DsaEsException("ES query ids failed", e);
        }
    }

    @Override
    public List<Map<String, Object>> query(DsaDsl dsl) {
        try {
            SearchRequest request = buildSearchRequest(dsl);

            SearchResponse response = client.search(request, RequestOptions.DEFAULT);
            List<Map<String, Object>> results = new ArrayList<>();
            for (SearchHit hit : response.getHits().getHits()) {
                Map<String, Object> source = hit.getSourceAsMap();
                source.put("_id", hit.getId());
                results.add(source);
            }
            return results;
        } catch (IOException e) {
            log.error("ES query error, index: {}", dsl.getIndex(), e);
            throw new DsaEsException("ES query failed", e);
        }
    }

    @Override
    public void index(String index, String id, Map<String, Object> doc) {
        try {
            IndexRequest request = new IndexRequest(index).id(id).source(doc);
            IndexResponse response = client.index(request, RequestOptions.DEFAULT);
            log.debug("ES index success, index: {}, id: {}, result: {}", index, id, response.getResult());
        } catch (IOException e) {
            log.error("ES index error, index: {}, id: {}", index, id, e);
            throw new DsaEsException("ES index failed", e);
        }
    }

    @Override
    public void bulkIndex(String index, List<Map<String, Object>> docs, String idField) {
        try {
            BulkRequest bulkRequest = new BulkRequest();
            for (Map<String, Object> doc : docs) {
                String id = String.valueOf(doc.get(idField));
                IndexRequest request = new IndexRequest(index).id(id).source(doc);
                bulkRequest.add(request);
            }
            BulkResponse response = client.bulk(bulkRequest, RequestOptions.DEFAULT);
            if (response.hasFailures()) {
                log.error("ES bulk index has failures: {}", response.buildFailureMessage());
                throw new DsaEsException("ES bulk index has failures");
            }
            log.debug("ES bulk index success, index: {}, count: {}", index, docs.size());
        } catch (IOException e) {
            log.error("ES bulk index error, index: {}", index, e);
            throw new DsaEsException("ES bulk index failed", e);
        }
    }

    @Override
    public void delete(String index, String id) {
        try {
            DeleteRequest request = new DeleteRequest(index, id);
            DeleteResponse response = client.delete(request, RequestOptions.DEFAULT);
            log.debug("ES delete success, index: {}, id: {}, result: {}", index, id, response.getResult());
        } catch (IOException e) {
            log.error("ES delete error, index: {}, id: {}", index, id, e);
            throw new DsaEsException("ES delete failed", e);
        }
    }

    @Override
    public void bulkDelete(String index, List<String> ids) {
        try {
            BulkRequest bulkRequest = new BulkRequest();
            for (String id : ids) {
                DeleteRequest request = new DeleteRequest(index, id);
                bulkRequest.add(request);
            }
            BulkResponse response = client.bulk(bulkRequest, RequestOptions.DEFAULT);
            if (response.hasFailures()) {
                log.error("ES bulk delete has failures: {}", response.buildFailureMessage());
                throw new DsaEsException("ES bulk delete has failures");
            }
            log.debug("ES bulk delete success, index: {}, count: {}", index, ids.size());
        } catch (IOException e) {
            log.error("ES bulk delete error, index: {}", index, e);
            throw new DsaEsException("ES bulk delete failed", e);
        }
    }

    @Override
    public boolean exists(String index, String id) {
        try {
            GetRequest request = new GetRequest(index, id);
            return client.exists(request, RequestOptions.DEFAULT);
        } catch (IOException e) {
            log.error("ES exists check error, index: {}, id: {}", index, id, e);
            throw new DsaEsException("ES exists check failed", e);
        }
    }

    @Override
    public long count(DsaDsl dsl) {
        try {
            SearchRequest request = buildSearchRequest(dsl);
            request.source().size(0);
            SearchResponse response = client.search(request, RequestOptions.DEFAULT);
            return response.getHits().getTotalHits().value;
        } catch (IOException e) {
            log.error("ES count error, index: {}", dsl.getIndex(), e);
            throw new DsaEsException("ES count failed", e);
        }
    }

    @Override
    public void createIndex(String index, Map<String, Object> mapping) {
        try {
            CreateIndexRequest request = new CreateIndexRequest(index);
            if (mapping != null && !mapping.isEmpty()) {
                request.mapping(mapping);
            }
            CreateIndexResponse response = client.indices().create(request, RequestOptions.DEFAULT);
            log.info("ES create index success, index: {}, acknowledged: {}", index, response.isAcknowledged());
        } catch (IOException e) {
            log.error("ES create index error, index: {}", index, e);
            throw new DsaEsException("ES create index failed", e);
        }
    }

    @Override
    public void deleteIndex(String index) {
        try {
            DeleteIndexRequest request = new DeleteIndexRequest(index);
            AcknowledgedResponse response = client.indices().delete(request, RequestOptions.DEFAULT);
            log.info("ES delete index success, index: {}, acknowledged: {}", index, response.isAcknowledged());
        } catch (IOException e) {
            log.error("ES delete index error, index: {}", index, e);
            throw new DsaEsException("ES delete index failed", e);
        }
    }

    @Override
    public boolean indexExists(String index) {
        try {
            GetIndexRequest request = new GetIndexRequest(index);
            return client.indices().exists(request, RequestOptions.DEFAULT);
        } catch (IOException e) {
            log.error("ES index exists check error, index: {}", index, e);
            throw new DsaEsException("ES index exists check failed", e);
        }
    }

    private SearchRequest buildSearchRequest(DsaDsl dsl) {
        SearchRequest request = new SearchRequest(dsl.getIndex());
        SearchSourceBuilder sourceBuilder = new SearchSourceBuilder();

        if (dsl.getQuery() != null) {
            sourceBuilder.query(QueryBuilders.matchAllQuery());
        }

        if (dsl.getFrom() != null) {
            sourceBuilder.from(dsl.getFrom());
        }
        if (dsl.getSize() != null) {
            sourceBuilder.size(dsl.getSize());
        }

        if (dsl.getSorts() != null) {
            for (Map<String, Object> sort : dsl.getSorts()) {
                for (Map.Entry<String, Object> entry : sort.entrySet()) {
                    String field = entry.getKey();
                    @SuppressWarnings("unchecked")
                    Map<String, Object> sortConfig = (Map<String, Object>) entry.getValue();
                    String order = (String) sortConfig.get("order");
                    sourceBuilder.sort(field, "ASC".equalsIgnoreCase(order) ? SortOrder.ASC : SortOrder.DESC);
                }
            }
        }

        if (dsl.getSourceIncludes() != null) {
            sourceBuilder.fetchSource(dsl.getSourceIncludes(), dsl.getSourceExcludes());
        }

        request.source(sourceBuilder);
        return request;
    }
}
