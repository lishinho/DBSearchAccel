package io.github.dbsearchaccel.admin.model;

import lombok.Data;
import java.io.Serializable;
import java.util.Map;

/**
 * DSA监控大盘数据
 * 用于展示核心监控指标
 *
 * @author DBSearchAccel Team
 */
@Data
public class DsaDashboardData implements Serializable {

    private static final long serialVersionUID = 1L;

    private Long totalQueries;

    private Long avgLatency;

    private Double hitRate;

    private Long degradeCount;

    private Long errorCount;

    private Map<String, Long> sceneQueryCount;

    private Map<String, Double> sceneLatency;

    private Map<String, Long> esQueryCount;

    private Map<String, Long> dbQueryCount;
}
