-- =====================================================================
-- 支付库：mall_payment
-- =====================================================================
DROP DATABASE IF EXISTS `mall_payment`;
CREATE DATABASE `mall_payment` DEFAULT CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci;
USE `mall_payment`;

CREATE TABLE `t_payment`
(
    `id`             BIGINT         NOT NULL COMMENT '主键',
    `pay_no`         VARCHAR(64)    NOT NULL COMMENT '支付单号',
    `order_no`       VARCHAR(64)    NOT NULL COMMENT '业务订单号',
    `user_id`        BIGINT         NOT NULL COMMENT '付款用户',
    `amount`         DECIMAL(12, 2) NOT NULL COMMENT '支付金额',
    `channel`        VARCHAR(20)    NOT NULL DEFAULT 'ALIPAY' COMMENT '渠道：ALIPAY / WECHAT / BALANCE',
    `status`         TINYINT        NOT NULL DEFAULT 0 COMMENT '状态：0 待支付，1 支付成功，2 已关闭',
    `trade_no`       VARCHAR(64)             DEFAULT NULL COMMENT '第三方交易号',
    `pay_time`       DATETIME                DEFAULT NULL COMMENT '支付时间',
    `notify_content` VARCHAR(512)            DEFAULT NULL COMMENT '回调原文',
    `create_time`    DATETIME                DEFAULT NULL,
    `update_time`    DATETIME                DEFAULT NULL,
    `deleted`        TINYINT        NOT NULL DEFAULT 0,
    PRIMARY KEY (`id`),
    UNIQUE KEY `uk_pay_no` (`pay_no`),
    KEY `idx_order_no` (`order_no`)
) ENGINE = InnoDB
  DEFAULT CHARSET = utf8mb4 COMMENT ='支付单表';
