package io.github.dbsearchaccel.core.tiered;

import org.roaringbitmap.RoaringBitmap;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.*;
import java.nio.ByteBuffer;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;

/**
 * SSD温存储（轻量级文件存储实现）.
 * <p>
 * 基于文件系统实现L2温数据存储，支持持久化和高效检索.
 * 使用简单的文件存储替代RocksDB，避免重量级依赖.
 * </p>
 *
 * @author DBSearchAccel Team
 * @since 1.0.0
 */
public class DsaWarmStore {

    private static final Logger log = LoggerFactory.getLogger(DsaWarmStore.class);

    /**
     * 数据目录.
     */
    private final String dataDir;

    /**
     * 索引文件.
     */
    private final Properties indexFile;

    /**
     * 索引文件路径.
     */
    private final Path indexPath;

    /**
     * 是否已初始化.
     */
    private boolean initialized = false;

    /**
     * 构造方法.
     *
     * @param dataDir 数据存储目录
     */
    public DsaWarmStore(String dataDir) {
        this.dataDir = dataDir;
        this.indexFile = new Properties();
        this.indexPath = Paths.get(dataDir, "index.properties");
        init();
    }

    /**
     * 初始化.
     */
    private void init() {
        try {
            Path dirPath = Paths.get(dataDir);
            if (!Files.exists(dirPath)) {
                Files.createDirectories(dirPath);
            }

            if (Files.exists(indexPath)) {
                try (InputStream is = Files.newInputStream(indexPath)) {
                    indexFile.load(is);
                }
            }

            initialized = true;
            log.info("DsaWarmStore initialized, dataDir: {}", dataDir);

        } catch (IOException e) {
            log.error("Failed to initialize DsaWarmStore: {}", dataDir, e);
            throw new RuntimeException("Failed to initialize DsaWarmStore", e);
        }
    }

    /**
     * 获取位图.
     *
     * @param sceneCode 场景编码
     * @param indexKey  索引键
     * @return 位图，不存在返回null
     */
    public RoaringBitmap get(String sceneCode, String indexKey) {
        if (!initialized) {
            return null;
        }

        try {
            String key = buildKey(sceneCode, indexKey);
            Path filePath = getFilePath(key);

            if (!Files.exists(filePath)) {
                return null;
            }

            byte[] bytes = Files.readAllBytes(filePath);
            RoaringBitmap bitmap = deserializeBitmap(bytes);

            log.debug("Warm store get: {}:{}, cardinality: {}", sceneCode, indexKey, bitmap.getLongCardinality());
            return bitmap;

        } catch (IOException e) {
            log.error("Failed to get from warm store: {}:{}", sceneCode, indexKey, e);
            return null;
        }
    }

    /**
     * 存入位图.
     *
     * @param sceneCode 场景编码
     * @param indexKey  索引键
     * @param bitmap    位图
     */
    public void put(String sceneCode, String indexKey, RoaringBitmap bitmap) {
        if (!initialized || bitmap == null || bitmap.isEmpty()) {
            return;
        }

        try {
            String key = buildKey(sceneCode, indexKey);
            Path filePath = getFilePath(key);

            byte[] bytes = serializeBitmap(bitmap);
            Files.write(filePath, bytes);

            log.debug("Warm store put: {}:{}, cardinality: {}", sceneCode, indexKey, bitmap.getLongCardinality());

        } catch (IOException e) {
            log.error("Failed to put to warm store: {}:{}", sceneCode, indexKey, e);
        }
    }

    /**
     * 移除位图.
     *
     * @param sceneCode 场景编码
     * @param indexKey  索引键
     */
    public void remove(String sceneCode, String indexKey) {
        if (!initialized) {
            return;
        }

        try {
            String key = buildKey(sceneCode, indexKey);
            Path filePath = getFilePath(key);

            if (Files.exists(filePath)) {
                Files.delete(filePath);
            }

            log.debug("Warm store remove: {}:{}", sceneCode, indexKey);

        } catch (IOException e) {
            log.error("Failed to delete from warm store: {}:{}", sceneCode, indexKey, e);
        }
    }

    /**
     * 清空场景数据.
     *
     * @param sceneCode 场景编码
     */
    public void clear(String sceneCode) {
        if (!initialized) {
            return;
        }

        try {
            String prefix = sceneCode + "_";
            Path dirPath = Paths.get(dataDir);

            Files.walk(dirPath)
                    .filter(path -> path.getFileName().toString().startsWith(prefix))
                    .forEach(path -> {
                        try {
                            Files.deleteIfExists(path);
                        } catch (IOException e) {
                            log.warn("Failed to delete file: {}", path, e);
                        }
                    });

            log.info("Warm store cleared for scene: {}", sceneCode);

        } catch (IOException e) {
            log.error("Failed to clear warm store for scene: {}", sceneCode, e);
        }
    }

    /**
     * 清空所有数据.
     */
    public void clearAll() {
        if (!initialized) {
            return;
        }

        try {
            Path dirPath = Paths.get(dataDir);

            Files.walk(dirPath)
                    .filter(path -> path.toString().endsWith(".bitmap"))
                    .forEach(path -> {
                        try {
                            Files.deleteIfExists(path);
                        } catch (IOException e) {
                            log.warn("Failed to delete file: {}", path, e);
                        }
                    });

            log.info("Warm store cleared all");

        } catch (IOException e) {
            log.error("Failed to clear all warm store", e);
        }
    }

    /**
     * 获取存储条目数.
     *
     * @param sceneCode 场景编码
     * @return 条目数
     */
    public long count(String sceneCode) {
        if (!initialized) {
            return 0;
        }

        try {
            String prefix = sceneCode + "_";
            Path dirPath = Paths.get(dataDir);

            return Files.walk(dirPath)
                    .filter(path -> path.getFileName().toString().startsWith(prefix))
                    .count();

        } catch (IOException e) {
            log.error("Failed to count warm store for scene: {}", sceneCode, e);
            return 0;
        }
    }

    /**
     * 估算存储大小.
     *
     * @param sceneCode 场景编码
     * @return 存储大小（字节）
     */
    public long estimateSize(String sceneCode) {
        if (!initialized) {
            return 0;
        }

        try {
            String prefix = sceneCode + "_";
            Path dirPath = Paths.get(dataDir);

            return Files.walk(dirPath)
                    .filter(path -> path.getFileName().toString().startsWith(prefix))
                    .mapToLong(path -> {
                        try {
                            return Files.size(path);
                        } catch (IOException e) {
                            return 0L;
                        }
                    })
                    .sum();

        } catch (IOException e) {
            log.error("Failed to estimate size for scene: {}", sceneCode, e);
            return 0;
        }
    }

    /**
     * 获取所有键.
     *
     * @param sceneCode 场景编码
     * @return 键到位图映射
     */
    public Map<String, RoaringBitmap> getAll(String sceneCode) {
        Map<String, RoaringBitmap> result = new HashMap<>();
        if (!initialized) {
            return result;
        }

        try {
            String prefix = sceneCode + "_";
            Path dirPath = Paths.get(dataDir);

            Files.walk(dirPath)
                    .filter(path -> path.getFileName().toString().startsWith(prefix))
                    .forEach(path -> {
                        try {
                            String fileName = path.getFileName().toString();
                            String indexKey = fileName.substring(prefix.length(), fileName.length() - 7);
                            byte[] bytes = Files.readAllBytes(path);
                            RoaringBitmap bitmap = deserializeBitmap(bytes);
                            result.put(indexKey, bitmap);
                        } catch (IOException e) {
                            log.warn("Failed to read file: {}", path, e);
                        }
                    });

        } catch (IOException e) {
            log.error("Failed to get all from warm store for scene: {}", sceneCode, e);
        }

        return result;
    }

    /**
     * 关闭存储.
     */
    public void close() {
        if (initialized) {
            try {
                try (OutputStream os = Files.newOutputStream(indexPath)) {
                    indexFile.store(os, "DsaWarmStore Index");
                }
            } catch (IOException e) {
                log.warn("Failed to save index file", e);
            }
            initialized = false;
            log.info("DsaWarmStore closed");
        }
    }

    /**
     * 构建存储键.
     */
    private String buildKey(String sceneCode, String indexKey) {
        String safeKey = indexKey.replace(":", "_").replace("/", "_");
        return sceneCode + "_" + safeKey;
    }

    /**
     * 获取文件路径.
     */
    private Path getFilePath(String key) {
        return Paths.get(dataDir, key + ".bitmap");
    }

    /**
     * 序列化位图.
     */
    private byte[] serializeBitmap(RoaringBitmap bitmap) {
        byte[] bytes = new byte[bitmap.serializedSizeInBytes()];
        bitmap.serialize(ByteBuffer.wrap(bytes));
        return bytes;
    }

    /**
     * 反序列化位图.
     */
    private RoaringBitmap deserializeBitmap(byte[] bytes) {
        RoaringBitmap bitmap = new RoaringBitmap();
        try {
            bitmap.deserialize(ByteBuffer.wrap(bytes));
        } catch (IOException e) {
            log.error("Failed to deserialize bitmap", e);
            return new RoaringBitmap();
        }
        return bitmap;
    }

    public boolean isInitialized() {
        return initialized;
    }

    public String getDataDir() {
        return dataDir;
    }
}
