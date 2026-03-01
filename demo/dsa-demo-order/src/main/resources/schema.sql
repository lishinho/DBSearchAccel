-- 创建示例数据库
CREATE DATABASE IF NOT EXISTS dsa_demo DEFAULT CHARACTER SET utf8mb4;

USE dsa_demo;

-- 订单表
CREATE TABLE IF NOT EXISTS t_order (
    order_id BIGINT PRIMARY KEY AUTO_INCREMENT COMMENT '订单ID',
    order_no VARCHAR(64) NOT NULL COMMENT '订单号',
    user_id BIGINT NOT NULL COMMENT '用户ID',
    user_name VARCHAR(64) COMMENT '用户名',
    status VARCHAR(32) NOT NULL DEFAULT 'CREATED' COMMENT '订单状态: CREATED/PAID/DELIVERED/RECEIVED/CANCELLED',
    total_amount DECIMAL(12,2) NOT NULL COMMENT '订单总金额',
    pay_amount DECIMAL(12,2) COMMENT '实付金额',
    payment_type VARCHAR(32) COMMENT '支付方式: ALIPAY/WECHAT/BANK',
    receiver_name VARCHAR(64) COMMENT '收货人姓名',
    receiver_phone VARCHAR(32) COMMENT '收货人电话',
    receiver_address VARCHAR(256) COMMENT '收货地址',
    create_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    pay_time DATETIME COMMENT '支付时间',
    delivery_time DATETIME COMMENT '发货时间',
    receive_time DATETIME COMMENT '收货时间',
    remark VARCHAR(512) COMMENT '备注',
    INDEX idx_user_id (user_id),
    INDEX idx_status (status),
    INDEX idx_create_time (create_time),
    INDEX idx_order_no (order_no)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='订单表';
