package io.github.dbsearchaccel.admin.model;

import lombok.Data;
import java.io.Serializable;

/**
 * DSA降级状态信息
 * 用于展示当前降级状态
 *
 * @author DBSearchAccel Team
 */
@Data
public class DsaDegradeStatus implements Serializable {

    private static final long serialVersionUID = 1L;

    private String sceneCode;

    private String degradeLevel;

    private Boolean isDegrading;

    private String degradeReason;

    private Long degradeTime;

    private Long recoverTime;

    private Integer degradeCount;
}
