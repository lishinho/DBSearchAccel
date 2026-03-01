package io.github.dbsearchaccel.demo.service;

import io.github.dbsearchaccel.demo.entity.Order;

import java.util.List;
import java.util.Map;

/**
 * 订单服务接口
 *
 * @author DBSearchAccel Team
 */
public interface OrderService {

    /**
     * 使用DSA加速查询订单
     *
     * @param params   查询参数
     * @param pageNum  页码
     * @param pageSize 每页大小
     * @return 订单列表
     */
    List<Order> queryOrdersWithDsa(Map<String, Object> params, int pageNum, int pageSize);

    /**
     * 直接查询数据库（降级场景）
     *
     * @param params   查询参数
     * @param pageNum  页码
     * @param pageSize 每页大小
     * @return 订单列表
     */
    List<Order> queryOrdersDirect(Map<String, Object> params, int pageNum, int pageSize);

    /**
     * 根据ID批量查询订单
     *
     * @param orderIds 订单ID列表
     * @return 订单列表
     */
    List<Order> queryByIds(List<Long> orderIds);

    /**
     * 创建订单
     *
     * @param order 订单信息
     * @return 创建后的订单
     */
    Order createOrder(Order order);

    /**
     * 更新订单状态
     *
     * @param orderId 订单ID
     * @param status  新状态
     * @return 是否成功
     */
    boolean updateStatus(Long orderId, String status);
}
