package io.github.dbsearchaccel.demo.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.io.Serializable;
import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * 订单实体
 * 演示电商订单数据模型
 *
 * @author DBSearchAccel Team
 */
@Data
@TableName("t_order")
public class Order implements Serializable {

    private static final long serialVersionUID = 1L;

    @TableId(type = IdType.AUTO)
    private Long orderId;

    private String orderNo;

    private Long userId;

    private String userName;

    private String status;

    private BigDecimal totalAmount;

    private BigDecimal payAmount;

    private String paymentType;

    private String receiverName;

    private String receiverPhone;

    private String receiverAddress;

    private LocalDateTime createTime;

    private LocalDateTime payTime;

    private LocalDateTime deliveryTime;

    private LocalDateTime receiveTime;

    private String remark;
}
