package io.github.dbsearchaccel.sync.increment;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.concurrent.DelayQueue;
import java.util.concurrent.Delayed;
import java.util.concurrent.TimeUnit;

/**
 * 同步重试处理器.
 * <p>
 * 提供同步失败消息的重试机制，支持延迟重试、最大重试次数限制.
 * </p>
 *
 * @author DBSearchAccel Team
 * @since 1.0.0
 */
public class DsaSyncRetryHandler {

    private static final Logger log = LoggerFactory.getLogger(DsaSyncRetryHandler.class);

    private static final int DEFAULT_MAX_RETRIES = 3;
    private static final long DEFAULT_RETRY_DELAY_MS = 5000;

    private final DelayQueue<RetryTask> retryQueue = new DelayQueue<>();
    private final int maxRetries;
    private final long retryDelayMs;

    private volatile boolean running = false;
    private Thread retryThread;

    /**
     * 默认构造方法.
     */
    public DsaSyncRetryHandler() {
        this(DEFAULT_MAX_RETRIES, DEFAULT_RETRY_DELAY_MS);
    }

    /**
     * 构造方法.
     *
     * @param maxRetries   最大重试次数
     * @param retryDelayMs 重试延迟（毫秒）
     */
    public DsaSyncRetryHandler(int maxRetries, long retryDelayMs) {
        this.maxRetries = maxRetries;
        this.retryDelayMs = retryDelayMs;
    }

    /**
     * 添加重试任务.
     *
     * @param sceneCode 场景编码
     * @param eventType 事件类型
     * @param data      数据
     * @param handler   处理器
     */
    public void addRetryTask(String sceneCode, String eventType,
                              java.util.Map<String, Object> data,
                              RetryHandler handler) {
        RetryTask task = new RetryTask(sceneCode, eventType, data, handler, 0);
        retryQueue.offer(task);
        log.debug("Added retry task, scene: [{}], event: {}", sceneCode, eventType);
    }

    /**
     * 启动重试处理器.
     */
    public void start() {
        if (running) {
            return;
        }

        running = true;
        retryThread = new Thread(this::processRetry, "dsa-sync-retry-handler");
        retryThread.setDaemon(true);
        retryThread.start();

        log.info("Sync retry handler started");
    }

    /**
     * 停止重试处理器.
     */
    public void stop() {
        running = false;
        if (retryThread != null) {
            retryThread.interrupt();
        }
        log.info("Sync retry handler stopped");
    }

    /**
     * 处理重试任务.
     */
    private void processRetry() {
        while (running) {
            try {
                RetryTask task = retryQueue.take();
                log.debug("Processing retry task, scene: [{}], attempt: {}",
                        task.sceneCode, task.attempt);

                boolean success = task.handler.handle(task.sceneCode, task.eventType, task.data);

                if (!success && task.attempt < maxRetries) {
                    task.attempt++;
                    task.executeTime = System.currentTimeMillis() + retryDelayMs;
                    retryQueue.offer(task);
                    log.warn("Retry task failed, will retry, scene: [{}], attempt: {}/{}",
                            task.sceneCode, task.attempt, maxRetries);
                } else if (!success) {
                    log.error("Retry task failed after {} attempts, scene: [{}]",
                            maxRetries, task.sceneCode);
                }

            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                break;
            } catch (Exception e) {
                log.error("Process retry task error", e);
            }
        }
    }

    /**
     * 获取待重试任务数量.
     *
     * @return 任务数量
     */
    public int getPendingCount() {
        return retryQueue.size();
    }

    /**
     * 重试处理器接口.
     */
    @FunctionalInterface
    public interface RetryHandler {
        /**
         * 处理重试任务.
         *
         * @param sceneCode 场景编码
         * @param eventType 事件类型
         * @param data      数据
         * @return true表示处理成功
         */
        boolean handle(String sceneCode, String eventType, java.util.Map<String, Object> data);
    }

    /**
     * 重试任务.
     */
    private static class RetryTask implements Delayed {

        private final String sceneCode;
        private final String eventType;
        private final java.util.Map<String, Object> data;
        private final RetryHandler handler;
        private int attempt;
        private long executeTime;

        RetryTask(String sceneCode, String eventType, java.util.Map<String, Object> data,
                   RetryHandler handler, int attempt) {
            this.sceneCode = sceneCode;
            this.eventType = eventType;
            this.data = data;
            this.handler = handler;
            this.attempt = attempt;
            this.executeTime = System.currentTimeMillis() + DEFAULT_RETRY_DELAY_MS;
        }

        @Override
        public long getDelay(TimeUnit unit) {
            return unit.convert(executeTime - System.currentTimeMillis(), TimeUnit.MILLISECONDS);
        }

        @Override
        public int compareTo(Delayed other) {
            return Long.compare(this.executeTime, ((RetryTask) other).executeTime);
        }
    }
}
