-- =====================================================================
-- 订单库：mall_order
-- 说明：本库是 Seata 全局事务的发起方（TM）所在库，同样需要 undo_log
-- =====================================================================
DROP DATABASE IF EXISTS `mall_order`;
CREATE DATABASE `mall_order` DEFAULT CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci;
USE `mall_order`;

CREATE TABLE `t_order`
(
    `id`               BIGINT         NOT NULL COMMENT '主键',
    `order_no`         VARCHAR(64)    NOT NULL COMMENT '业务订单号',
    `user_id`          BIGINT         NOT NULL COMMENT '下单用户',
    `total_amount`     DECIMAL(12, 2) NOT NULL DEFAULT 0 COMMENT '商品总额',
    `pay_amount`       DECIMAL(12, 2) NOT NULL DEFAULT 0 COMMENT '应付金额',
    `status`           TINYINT        NOT NULL DEFAULT 0 COMMENT '状态：0 待付款，1 已付款，2 已发货，3 已完成，4 已取消',
    `receiver`         VARCHAR(32)             DEFAULT NULL COMMENT '收件人',
    `receiver_phone`   VARCHAR(20)             DEFAULT NULL COMMENT '收件电话',
    `receiver_address` VARCHAR(255)            DEFAULT NULL COMMENT '收件地址',
    `remark`           VARCHAR(255)            DEFAULT NULL COMMENT '买家留言',
    `pay_time`         DATETIME                DEFAULT NULL COMMENT '支付时间',
    `cancel_time`      DATETIME                DEFAULT NULL COMMENT '取消时间',
    `cancel_reason`    VARCHAR(255)            DEFAULT NULL COMMENT '取消原因',
    `create_time`      DATETIME                DEFAULT NULL,
    `update_time`      DATETIME                DEFAULT NULL,
    `deleted`          TINYINT        NOT NULL DEFAULT 0,
    PRIMARY KEY (`id`),
    UNIQUE KEY `uk_order_no` (`order_no`),
    KEY `idx_user_status` (`user_id`, `status`),
    KEY `idx_create_time` (`create_time`)
) ENGINE = InnoDB
  DEFAULT CHARSET = utf8mb4 COMMENT ='订单主表';

CREATE TABLE `t_order_item`
(
    `id`            BIGINT         NOT NULL COMMENT '主键',
    `order_id`      BIGINT         NOT NULL COMMENT '订单 ID',
    `order_no`      VARCHAR(64)    NOT NULL COMMENT '订单号冗余',
    `product_id`    BIGINT         NOT NULL COMMENT '商品 ID',
    `product_name`  VARCHAR(128)            DEFAULT NULL COMMENT '商品名称快照',
    `product_image` VARCHAR(255)            DEFAULT NULL COMMENT '商品图片快照',
    `price`         DECIMAL(10, 2) NOT NULL COMMENT '成交单价快照',
    `quantity`      INT            NOT NULL COMMENT '购买数量',
    `total_price`   DECIMAL(12, 2) NOT NULL COMMENT '小计',
    `create_time`   DATETIME                DEFAULT NULL,
    `update_time`   DATETIME                DEFAULT NULL,
    `deleted`       TINYINT        NOT NULL DEFAULT 0,
    PRIMARY KEY (`id`),
    KEY `idx_order_no` (`order_no`),
    KEY `idx_product_id` (`product_id`)
) ENGINE = InnoDB
  DEFAULT CHARSET = utf8mb4 COMMENT ='订单明细表';

CREATE TABLE IF NOT EXISTS `undo_log`
(
    `branch_id`     BIGINT       NOT NULL COMMENT '分支事务 ID',
    `xid`           VARCHAR(128) NOT NULL COMMENT '全局事务 ID',
    `context`       VARCHAR(128) NOT NULL COMMENT 'undo_log 上下文',
    `rollback_info` LONGBLOB     NOT NULL COMMENT '回滚信息',
    `log_status`    INT          NOT NULL COMMENT '0 正常，1 防御状态',
    `log_created`   DATETIME(6)  NOT NULL,
    `log_modified`  DATETIME(6)  NOT NULL,
    UNIQUE KEY `ux_undo_log` (`xid`, `branch_id`)
) ENGINE = InnoDB
  DEFAULT CHARSET = utf8mb4 COMMENT ='AT 事务模式回滚日志';
