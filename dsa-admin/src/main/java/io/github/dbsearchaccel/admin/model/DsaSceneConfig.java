package io.github.dbsearchaccel.admin.model;

import lombok.Data;
import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * DSA场景配置实体
 * 用于存储场景的配置信息
 *
 * @author DBSearchAccel Team
 */
@Data
public class DsaSceneConfig implements Serializable {

    private static final long serialVersionUID = 1L;

    private Long id;

    private String sceneCode;

    private String sceneName;

    private String description;

    private String esIndex;

    private String dbTable;

    private String pkField;

    private String status;

    private Integer priority;

    private String dslTemplate;

    private String fieldMapping;

    private LocalDateTime createTime;

    private LocalDateTime updateTime;
}
