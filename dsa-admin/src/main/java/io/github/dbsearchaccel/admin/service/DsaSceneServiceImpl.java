package io.github.dbsearchaccel.admin.service;

import io.github.dbsearchaccel.admin.model.DsaSceneConfig;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicLong;
import java.util.stream.Collectors;

/**
 * DSA场景管理服务实现
 * 使用内存存储，生产环境可替换为数据库存储
 *
 * @author DBSearchAccel Team
 */
@Service
public class DsaSceneServiceImpl implements DsaSceneService {

    private final AtomicLong idGenerator = new AtomicLong(1);

    private final Map<Long, DsaSceneConfig> configStore = new ConcurrentHashMap<>();

    private final Map<String, DsaSceneConfig> codeIndex = new ConcurrentHashMap<>();

    @Override
    public DsaSceneConfig create(DsaSceneConfig config) {
        if (config.getSceneCode() == null || config.getSceneCode().isEmpty()) {
            throw new IllegalArgumentException("sceneCode cannot be empty");
        }
        if (codeIndex.containsKey(config.getSceneCode())) {
            throw new IllegalArgumentException("sceneCode already exists: " + config.getSceneCode());
        }
        Long id = idGenerator.getAndIncrement();
        config.setId(id);
        config.setCreateTime(LocalDateTime.now());
        config.setUpdateTime(LocalDateTime.now());
        if (config.getStatus() == null) {
            config.setStatus("ENABLED");
        }
        configStore.put(id, config);
        codeIndex.put(config.getSceneCode(), config);
        return config;
    }

    @Override
    public DsaSceneConfig update(DsaSceneConfig config) {
        if (config.getId() == null) {
            throw new IllegalArgumentException("id cannot be null");
        }
        DsaSceneConfig existing = configStore.get(config.getId());
        if (existing == null) {
            throw new IllegalArgumentException("config not found: " + config.getId());
        }
        config.setUpdateTime(LocalDateTime.now());
        config.setCreateTime(existing.getCreateTime());
        configStore.put(config.getId(), config);
        if (config.getSceneCode() != null) {
            codeIndex.put(config.getSceneCode(), config);
        }
        return config;
    }

    @Override
    public boolean delete(Long id) {
        DsaSceneConfig config = configStore.remove(id);
        if (config != null && config.getSceneCode() != null) {
            codeIndex.remove(config.getSceneCode());
            return true;
        }
        return false;
    }

    @Override
    public DsaSceneConfig getById(Long id) {
        return configStore.get(id);
    }

    @Override
    public DsaSceneConfig getBySceneCode(String sceneCode) {
        return codeIndex.get(sceneCode);
    }

    @Override
    public List<DsaSceneConfig> listAll() {
        return configStore.values().stream()
                .sorted((a, b) -> {
                    int priorityCompare = Integer.compare(
                            b.getPriority() != null ? b.getPriority() : 0,
                            a.getPriority() != null ? a.getPriority() : 0);
                    if (priorityCompare != 0) {
                        return priorityCompare;
                    }
                    return a.getSceneCode().compareTo(b.getSceneCode());
                })
                .collect(Collectors.toList());
    }

    @Override
    public List<DsaSceneConfig> listByStatus(String status) {
        return configStore.values().stream()
                .filter(c -> status.equals(c.getStatus()))
                .collect(Collectors.toList());
    }

    @Override
    public boolean enable(Long id) {
        DsaSceneConfig config = configStore.get(id);
        if (config == null) {
            return false;
        }
        config.setStatus("ENABLED");
        config.setUpdateTime(LocalDateTime.now());
        return true;
    }

    @Override
    public boolean disable(Long id) {
        DsaSceneConfig config = configStore.get(id);
        if (config == null) {
            return false;
        }
        config.setStatus("DISABLED");
        config.setUpdateTime(LocalDateTime.now());
        return true;
    }
}
