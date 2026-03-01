package io.github.dbsearchaccel.demo.controller;

import io.github.dbsearchaccel.demo.entity.Order;
import io.github.dbsearchaccel.demo.service.OrderService;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 订单查询控制器
 * 演示DSA加速查询的使用方式
 *
 * @author DBSearchAccel Team
 */
@RestController
@RequestMapping("/api/order")
public class OrderController {

    private final OrderService orderService;

    public OrderController(OrderService orderService) {
        this.orderService = orderService;
    }

    /**
     * 使用DSA加速查询订单
     * 支持多条件组合查询
     *
     * @param userId     用户ID
     * @param status     订单状态
     * @param orderNo    订单号（模糊匹配）
     * @param startTime  开始时间
     * @param endTime    结束时间
     * @param minAmount  最小金额
     * @param maxAmount  最大金额
     * @param pageNum    页码
     * @param pageSize   每页大小
     * @return 订单列表
     */
    @GetMapping("/query")
    public List<Order> queryOrders(
            @RequestParam(required = false) Long userId,
            @RequestParam(required = false) String status,
            @RequestParam(required = false) String orderNo,
            @RequestParam(required = false) String startTime,
            @RequestParam(required = false) String endTime,
            @RequestParam(required = false) Double minAmount,
            @RequestParam(required = false) Double maxAmount,
            @RequestParam(defaultValue = "1") int pageNum,
            @RequestParam(defaultValue = "20") int pageSize) {

        Map<String, Object> params = new HashMap<>();
        if (userId != null) params.put("userId", userId);
        if (status != null) params.put("status", status);
        if (orderNo != null) params.put("orderNo", orderNo);
        if (startTime != null) params.put("startTime", startTime);
        if (endTime != null) params.put("endTime", endTime);
        if (minAmount != null) params.put("minAmount", minAmount);
        if (maxAmount != null) params.put("maxAmount", maxAmount);

        return orderService.queryOrdersWithDsa(params, pageNum, pageSize);
    }

    /**
     * 直接查询数据库（降级场景演示）
     */
    @GetMapping("/query-direct")
    public List<Order> queryOrdersDirect(
            @RequestParam(required = false) Long userId,
            @RequestParam(required = false) String status,
            @RequestParam(defaultValue = "1") int pageNum,
            @RequestParam(defaultValue = "20") int pageSize) {

        Map<String, Object> params = new HashMap<>();
        if (userId != null) params.put("userId", userId);
        if (status != null) params.put("status", status);

        return orderService.queryOrdersDirect(params, pageNum, pageSize);
    }

    /**
     * 根据ID批量查询订单
     */
    @PostMapping("/query-by-ids")
    public List<Order> queryByIds(@RequestBody List<Long> orderIds) {
        return orderService.queryByIds(orderIds);
    }

    /**
     * 创建订单
     */
    @PostMapping
    public Order createOrder(@RequestBody Order order) {
        return orderService.createOrder(order);
    }

    /**
     * 更新订单状态
     */
    @PutMapping("/{orderId}/status")
    public boolean updateStatus(@PathVariable Long orderId, @RequestParam String status) {
        return orderService.updateStatus(orderId, status);
    }
}
