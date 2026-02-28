package io.github.dbsearchaccel.infra.es;

import org.apache.http.HttpHost;
import org.elasticsearch.client.RestClient;
import org.elasticsearch.client.RestClientBuilder;
import org.elasticsearch.client.RestHighLevelClient;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * Elasticsearch配置类.
 * <p>
 * 配置Elasticsearch客户端连接信息.
 * </p>
 *
 * @author DBSearchAccel Team
 * @since 1.0.0
 */
@Configuration
public class DsaEsConfig {

    /**
     * ES主机地址.
     * <p>
     * 格式：host:port，多个用逗号分隔
     * </p>
     */
    @Value("${dsa.es.hosts:127.0.0.1:9200}")
    private String esHosts;

    /**
     * ES用户名.
     */
    @Value("${dsa.es.username:}")
    private String username;

    /**
     * ES密码.
     */
    @Value("${dsa.es.password:}")
    private String password;

    /**
     * 连接超时时间（毫秒）.
     */
    @Value("${dsa.es.connection-timeout:5000}")
    private int connectionTimeout;

    /**
     * Socket超时时间（毫秒）.
     */
    @Value("${dsa.es.socket-timeout:30000}")
    private int socketTimeout;

    /**
     * 创建ES高级客户端Bean.
     *
     * @return RestHighLevelClient实例
     */
    @Bean
    public RestHighLevelClient restHighLevelClient() {
        String[] hosts = esHosts.split(",");
        HttpHost[] httpHosts = new HttpHost[hosts.length];
        for (int i = 0; i < hosts.length; i++) {
            String[] hostPort = hosts[i].trim().split(":");
            String host = hostPort[0];
            int port = hostPort.length > 1 ? Integer.parseInt(hostPort[1]) : 9200;
            httpHosts[i] = new HttpHost(host, port, "http");
        }

        RestClientBuilder builder = RestClient.builder(httpHosts)
                .setRequestConfigCallback(requestConfigBuilder ->
                        requestConfigBuilder
                                .setConnectTimeout(connectionTimeout)
                                .setSocketTimeout(socketTimeout));

        return new RestHighLevelClient(builder);
    }

    /**
     * 创建DSA ES客户端Bean.
     *
     * @param restHighLevelClient ES高级客户端
     * @return DsaEsClient实例
     */
    @Bean
    public DsaEsClient dsaEsClient(RestHighLevelClient restHighLevelClient) {
        return new DsaEsClientImpl(restHighLevelClient);
    }
}
