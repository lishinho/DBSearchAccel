package io.github.dbsearchaccel.cache;

import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.AfterReturning;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Pointcut;
import org.aspectj.lang.reflect.MethodSignature;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.lang.reflect.Method;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * 缓存切面.
 * <p>
 * 拦截标注了@DsaRealTimeCache注解的方法，自动采集主键并更新Redis缓存.
 * </p>
 *
 * @author DBSearchAccel Team
 * @since 1.0.0
 */
@Aspect
public class DsaCacheAspect {

    private static final Logger log = LoggerFactory.getLogger(DsaCacheAspect.class);

    private DsaCacheService cacheService;
    private DsaPkResolver pkResolver;
    private ExecutorService asyncExecutor;

    /**
     * 默认构造方法.
     */
    public DsaCacheAspect() {
        this.pkResolver = new DsaPkResolverImpl();
        this.asyncExecutor = Executors.newFixedThreadPool(4);
    }

    /**
     * 构造方法.
     *
     * @param cacheService 缓存服务
     */
    public DsaCacheAspect(DsaCacheService cacheService) {
        this.cacheService = cacheService;
        this.pkResolver = new DsaPkResolverImpl();
        this.asyncExecutor = Executors.newFixedThreadPool(4);
    }

    /**
     * 定义切点.
     */
    @Pointcut("@annotation(io.github.dbsearchaccel.cache.DsaRealTimeCache)")
    public void cachePointcut() {
    }

    /**
     * 环绕通知-处理缓存操作.
     *
     * @param joinPoint 连接点
     * @return 方法执行结果
     * @throws Throwable 异常
     */
    @Around("cachePointcut()")
    public Object around(ProceedingJoinPoint joinPoint) throws Throwable {
        MethodSignature signature = (MethodSignature) joinPoint.getSignature();
        Method method = signature.getMethod();
        DsaRealTimeCache annotation = method.getAnnotation(DsaRealTimeCache.class);

        Object result = joinPoint.proceed();

        if (cacheService == null) {
            log.warn("Cache service is null, skip cache operation");
            return result;
        }

        Runnable cacheTask = () -> processCacheOperation(annotation, joinPoint.getArgs());

        if (annotation.async()) {
            asyncExecutor.submit(cacheTask);
        } else {
            cacheTask.run();
        }

        return result;
    }

    /**
     * 处理缓存操作.
     *
     * @param annotation 注解
     * @param args       方法参数
     */
    private void processCacheOperation(DsaRealTimeCache annotation, Object[] args) {
        String sceneCode = annotation.sceneCode();
        String pkField = annotation.pkField();
        int paramIndex = annotation.paramIndex();
        DsaRealTimeCache.CacheOperation operation = annotation.operation();

        try {
            List<String> pkValues = pkResolver.resolve(args, pkField, paramIndex);

            if (pkValues.isEmpty()) {
                log.debug("No pk values resolved, scene: [{}]", sceneCode);
                return;
            }

            switch (operation) {
                case INSERT:
                case UPDATE:
                    int addedCount = cacheService.addPkBatch(sceneCode, pkValues);
                    log.debug("Cache operation [{}], scene: [{}], count: {}",
                            operation, sceneCode, addedCount);
                    break;

                case DELETE:
                    int removedCount = cacheService.removePkBatch(sceneCode, pkValues);
                    log.debug("Cache operation [DELETE], scene: [{}], count: {}",
                            sceneCode, removedCount);
                    break;

                default:
                    log.warn("Unknown cache operation: {}", operation);
            }

        } catch (Exception e) {
            log.error("Process cache operation failed, scene: [{}], operation: {}",
                    sceneCode, operation, e);
        }
    }

    /**
     * 设置缓存服务.
     *
     * @param cacheService 缓存服务
     */
    public void setCacheService(DsaCacheService cacheService) {
        this.cacheService = cacheService;
    }

    /**
     * 设置主键解析器.
     *
     * @param pkResolver 主键解析器
     */
    public void setPkResolver(DsaPkResolver pkResolver) {
        this.pkResolver = pkResolver;
    }

    /**
     * 关闭切面.
     */
    public void shutdown() {
        if (asyncExecutor != null) {
            asyncExecutor.shutdown();
        }
        log.info("Cache aspect shutdown");
    }
}
