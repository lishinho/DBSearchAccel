package io.github.dbsearchaccel.gateway;

import com.alibaba.csp.sentinel.Entry;
import com.alibaba.csp.sentinel.SphU;
import com.alibaba.csp.sentinel.slots.block.BlockException;
import com.alibaba.csp.sentinel.slots.block.RuleConstant;
import com.alibaba.csp.sentinel.slots.block.flow.FlowRule;
import com.alibaba.csp.sentinel.slots.block.flow.FlowRuleManager;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * 限流器实现.
 * <p>
 * 基于Sentinel实现接口级别的限流控制.
 * </p>
 *
 * @author DBSearchAccel Team
 * @since 1.0.0
 */
@Component
public class DsaRateLimiterImpl implements DsaRateLimiter {

    private static final Logger LOGGER = LoggerFactory.getLogger(DsaRateLimiterImpl.class);

    private final Map<String, DsaRateLimitConfig> configMap = new ConcurrentHashMap<>();

    private final Map<String, AtomicInteger> concurrentCounter = new ConcurrentHashMap<>();

    @Override
    public boolean tryAcquire(String resource) {
        DsaRateLimitConfig config = configMap.get(resource);
        if (config == null || !config.isEnabled()) {
            return true;
        }

        Entry entry = null;
        try {
            entry = SphU.entry(resource);
            return true;
        } catch (BlockException e) {
            LOGGER.warn("Rate limited for resource: {}", resource);
            return false;
        } finally {
            if (entry != null) {
                entry.exit();
            }
        }
    }

    @Override
    public boolean tryAcquire(String resource, long timeoutMs) {
        return tryAcquire(resource);
    }

    @Override
    public boolean tryAcquireConcurrent(String resource) {
        DsaRateLimitConfig config = configMap.get(resource);
        if (config == null || !config.isEnabled()) {
            return true;
        }

        AtomicInteger counter = concurrentCounter.computeIfAbsent(resource, k -> new AtomicInteger(0));
        int current = counter.get();
        int threshold = config.getConcurrentThreshold();

        if (current >= threshold) {
            LOGGER.warn("Concurrent limit reached for resource: {}, current: {}, threshold: {}",
                    resource, current, threshold);
            return false;
        }

        return counter.incrementAndGet() <= threshold || counter.decrementAndGet() >= 0;
    }

    @Override
    public void releaseConcurrent(String resource) {
        AtomicInteger counter = concurrentCounter.get(resource);
        if (counter != null && counter.get() > 0) {
            counter.decrementAndGet();
        }
    }

    @Override
    public double getCurrentQps(String resource) {
        return 0.0;
    }

    @Override
    public int getCurrentConcurrent(String resource) {
        AtomicInteger counter = concurrentCounter.get(resource);
        return counter != null ? counter.get() : 0;
    }

    @Override
    public void configure(String resource, DsaRateLimitConfig config) {
        LOGGER.info("Configuring rate limiter for resource: {}, config: {}", resource, config);

        configMap.put(resource, config);

        if (config.isEnabled() && config.getStrategy() != DsaRateLimitConfig.LimitStrategy.CONCURRENT) {
            List<FlowRule> rules = new ArrayList<>();
            FlowRule rule = new FlowRule();
            rule.setResource(resource);
            rule.setGrade(RuleConstant.FLOW_GRADE_QPS);
            rule.setCount(config.getQpsThreshold());
            rules.add(rule);

            FlowRuleManager.loadRules(rules);
            LOGGER.info("Sentinel flow rules configured for resource: {}, qps: {}", resource, config.getQpsThreshold());
        }
    }

    @Override
    public void remove(String resource) {
        LOGGER.info("Removing rate limiter for resource: {}", resource);
        configMap.remove(resource);
        concurrentCounter.remove(resource);
    }

    @Override
    public boolean isRateLimited(String resource) {
        DsaRateLimitConfig config = configMap.get(resource);
        if (config == null || !config.isEnabled()) {
            return false;
        }

        if (config.getStrategy() == DsaRateLimitConfig.LimitStrategy.CONCURRENT
                || config.getStrategy() == DsaRateLimitConfig.LimitStrategy.MIXED) {
            AtomicInteger counter = concurrentCounter.get(resource);
            if (counter != null && counter.get() >= config.getConcurrentThreshold()) {
                return true;
            }
        }

        return false;
    }
}
