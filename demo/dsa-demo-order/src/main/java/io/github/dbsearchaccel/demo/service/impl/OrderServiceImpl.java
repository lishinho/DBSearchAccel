package io.github.dbsearchaccel.demo.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import io.github.dbsearchaccel.common.model.request.DsaRequest;
import io.github.dbsearchaccel.common.model.response.DsaResponse;
import io.github.dbsearchaccel.demo.entity.Order;
import io.github.dbsearchaccel.demo.mapper.OrderMapper;
import io.github.dbsearchaccel.demo.service.OrderService;
import io.github.dbsearchaccel.sdk.DsaClient;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * 订单服务实现
 * 演示如何使用DSA SDK进行加速查询
 *
 * @author DBSearchAccel Team
 */
@Service
public class OrderServiceImpl implements OrderService {

    private static final Logger log = LoggerFactory.getLogger(OrderServiceImpl.class);

    private static final String SCENE_CODE = "order_query";

    private final DsaClient dsaClient;

    private final OrderMapper orderMapper;

    public OrderServiceImpl(DsaClient dsaClient, OrderMapper orderMapper) {
        this.dsaClient = dsaClient;
        this.orderMapper = orderMapper;
    }

    @Override
    @SuppressWarnings("unchecked")
    public List<Order> queryOrdersWithDsa(Map<String, Object> params, int pageNum, int pageSize) {
        log.info("使用DSA加速查询订单, params={}, pageNum={}, pageSize={}", params, pageNum, pageSize);

        DsaRequest request = new DsaRequest();
        request.setSceneCode(SCENE_CODE);
        request.setQueryParams(params);
        request.setPageNum(pageNum);
        request.setPageSize(pageSize);
        request.setSortField("create_time");
        request.setSortOrder("DESC");

        try {
            DsaResponse<Object> response = dsaClient.query(request);
            log.info("DSA查询完成, total={}", response.getPageInfo() != null ? response.getPageInfo().getTotal() : 0);
            if (response.isSuccess() && response.getData() instanceof List) {
                return (List<Order>) response.getData();
            }
            return new ArrayList<>();
        } catch (Exception e) {
            log.error("DSA查询失败，降级到直接查询数据库", e);
            return queryOrdersDirect(params, pageNum, pageSize);
        }
    }

    @Override
    public List<Order> queryOrdersDirect(Map<String, Object> params, int pageNum, int pageSize) {
        log.info("直接查询数据库, params={}", params);

        LambdaQueryWrapper<Order> wrapper = new LambdaQueryWrapper<>();

        if (params.get("userId") != null) {
            wrapper.eq(Order::getUserId, params.get("userId"));
        }
        if (params.get("status") != null) {
            wrapper.eq(Order::getStatus, params.get("status"));
        }
        if (params.get("orderNo") != null) {
            wrapper.like(Order::getOrderNo, params.get("orderNo"));
        }
        if (params.get("startTime") != null) {
            LocalDateTime startTime = parseDateTime(params.get("startTime").toString());
            if (startTime != null) {
                wrapper.ge(Order::getCreateTime, startTime);
            }
        }
        if (params.get("endTime") != null) {
            LocalDateTime endTime = parseDateTime(params.get("endTime").toString());
            if (endTime != null) {
                wrapper.le(Order::getCreateTime, endTime);
            }
        }
        if (params.get("minAmount") != null) {
            wrapper.ge(Order::getTotalAmount, params.get("minAmount"));
        }
        if (params.get("maxAmount") != null) {
            wrapper.le(Order::getTotalAmount, params.get("maxAmount"));
        }

        wrapper.orderByDesc(Order::getCreateTime);

        return orderMapper.selectList(wrapper);
    }

    @Override
    public List<Order> queryByIds(List<Long> orderIds) {
        if (orderIds == null || orderIds.isEmpty()) {
            return List.of();
        }
        return orderMapper.selectBatchIds(orderIds);
    }

    @Override
    public Order createOrder(Order order) {
        order.setCreateTime(LocalDateTime.now());
        order.setStatus("CREATED");
        orderMapper.insert(order);
        log.info("创建订单成功, orderId={}", order.getOrderId());
        return order;
    }

    @Override
    public boolean updateStatus(Long orderId, String status) {
        LambdaUpdateWrapper<Order> wrapper = new LambdaUpdateWrapper<>();
        wrapper.eq(Order::getOrderId, orderId)
                .set(Order::getStatus, status);

        if ("PAID".equals(status)) {
            wrapper.set(Order::getPayTime, LocalDateTime.now());
        } else if ("DELIVERED".equals(status)) {
            wrapper.set(Order::getDeliveryTime, LocalDateTime.now());
        } else if ("RECEIVED".equals(status)) {
            wrapper.set(Order::getReceiveTime, LocalDateTime.now());
        }

        int rows = orderMapper.update(null, wrapper);
        log.info("更新订单状态, orderId={}, status={}, rows={}", orderId, status, rows);
        return rows > 0;
    }

    private LocalDateTime parseDateTime(String dateStr) {
        try {
            if (dateStr.length() == 10) {
                return LocalDateTime.parse(dateStr + "T00:00:00", DateTimeFormatter.ISO_LOCAL_DATE_TIME);
            }
            return LocalDateTime.parse(dateStr, DateTimeFormatter.ISO_LOCAL_DATE_TIME);
        } catch (Exception e) {
            log.warn("解析日期失败: {}", dateStr);
            return null;
        }
    }
}
