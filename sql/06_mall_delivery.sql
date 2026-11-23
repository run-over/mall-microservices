-- =====================================================================
-- 配送库：mall_delivery
-- =====================================================================
DROP DATABASE IF EXISTS `mall_delivery`;
CREATE DATABASE `mall_delivery` DEFAULT CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci;
USE `mall_delivery`;

CREATE TABLE `t_delivery`
(
    `id`          BIGINT      NOT NULL COMMENT '主键',
    `delivery_no` VARCHAR(64) NOT NULL COMMENT '配送单号',
    `order_no`    VARCHAR(64) NOT NULL COMMENT '业务订单号',
    `company`     VARCHAR(64)          DEFAULT NULL COMMENT '快递公司',
    `tracking_no` VARCHAR(64)          DEFAULT NULL COMMENT '快递单号',
    `receiver`    VARCHAR(32)          DEFAULT NULL COMMENT '收件人',
    `phone`       VARCHAR(20)          DEFAULT NULL COMMENT '收件电话',
    `address`     VARCHAR(255)         DEFAULT NULL COMMENT '收件地址',
    `status`      TINYINT     NOT NULL DEFAULT 0 COMMENT '状态：0 待揽收，1 运输中，2 已签收',
    `sign_time`   DATETIME             DEFAULT NULL COMMENT '签收时间',
    `create_time` DATETIME             DEFAULT NULL,
    `update_time` DATETIME             DEFAULT NULL,
    `deleted`     TINYINT     NOT NULL DEFAULT 0,
    PRIMARY KEY (`id`),
    UNIQUE KEY `uk_delivery_no` (`delivery_no`),
    -- 一单一配送，用唯一索引兜住重复发货
    UNIQUE KEY `uk_order_no` (`order_no`)
) ENGINE = InnoDB
  DEFAULT CHARSET = utf8mb4 COMMENT ='配送单表';
