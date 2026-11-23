-- =====================================================================
-- 库存库：mall_stock
-- 说明：本库是 Seata AT 模式的 RM（资源管理器），必须存在 undo_log 表
-- =====================================================================
DROP DATABASE IF EXISTS `mall_stock`;
CREATE DATABASE `mall_stock` DEFAULT CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci;
USE `mall_stock`;

CREATE TABLE `t_stock`
(
    `id`           BIGINT       NOT NULL COMMENT '主键',
    `product_id`   BIGINT       NOT NULL COMMENT '商品 ID',
    `product_name` VARCHAR(128)          DEFAULT NULL COMMENT '商品名称冗余',
    `total`        INT          NOT NULL DEFAULT 0 COMMENT '总库存',
    `available`    INT          NOT NULL DEFAULT 0 COMMENT '可售库存',
    `locked`       INT          NOT NULL DEFAULT 0 COMMENT '锁定库存',
    `version`      INT          NOT NULL DEFAULT 0 COMMENT '乐观锁版本号',
    `create_time`  DATETIME              DEFAULT NULL,
    `update_time`  DATETIME              DEFAULT NULL,
    `deleted`      TINYINT      NOT NULL DEFAULT 0,
    PRIMARY KEY (`id`),
    UNIQUE KEY `uk_product_id` (`product_id`)
) ENGINE = InnoDB
  DEFAULT CHARSET = utf8mb4 COMMENT ='库存表';

CREATE TABLE `t_stock_log`
(
    `id`              BIGINT      NOT NULL COMMENT '主键',
    `product_id`      BIGINT      NOT NULL COMMENT '商品 ID',
    `order_no`        VARCHAR(64)          DEFAULT NULL COMMENT '关联订单号',
    `quantity`        INT         NOT NULL COMMENT '变更数量',
    `type`            TINYINT     NOT NULL COMMENT '类型：1 扣减，2 回补',
    `after_available` INT                  DEFAULT NULL COMMENT '变更后可用库存',
    `create_time`     DATETIME             DEFAULT NULL,
    `update_time`     DATETIME             DEFAULT NULL,
    `deleted`         TINYINT     NOT NULL DEFAULT 0,
    PRIMARY KEY (`id`),
    -- 幂等的最后一道防线：同一订单同一商品同一类型只允许一条流水
    UNIQUE KEY `uk_order_product_type` (`order_no`, `product_id`, `type`),
    KEY `idx_product_id` (`product_id`)
) ENGINE = InnoDB
  DEFAULT CHARSET = utf8mb4 COMMENT ='库存变更流水表';

-- ---------------------------------------------------------------------
-- Seata AT 模式回滚日志表（由 seata 自动写入，不要手工修改）
-- ---------------------------------------------------------------------
CREATE TABLE IF NOT EXISTS `undo_log`
(
    `branch_id`     BIGINT       NOT NULL COMMENT '分支事务 ID',
    `xid`           VARCHAR(128) NOT NULL COMMENT '全局事务 ID',
    `context`       VARCHAR(128) NOT NULL COMMENT 'undo_log 上下文，如序列化方式',
    `rollback_info` LONGBLOB     NOT NULL COMMENT '回滚信息（前镜像/后镜像）',
    `log_status`    INT          NOT NULL COMMENT '0 正常，1 防御状态',
    `log_created`   DATETIME(6)  NOT NULL COMMENT '创建时间',
    `log_modified`  DATETIME(6)  NOT NULL COMMENT '修改时间',
    UNIQUE KEY `ux_undo_log` (`xid`, `branch_id`)
) ENGINE = InnoDB
  DEFAULT CHARSET = utf8mb4 COMMENT ='AT 事务模式回滚日志';

INSERT INTO `t_stock` (`id`, `product_id`, `product_name`, `total`, `available`, `locked`, `version`, `create_time`,
                       `update_time`, `deleted`)
VALUES (5001, 1001, '智能手机 Pro 12+256G', 500, 500, 0, 0, NOW(), NOW(), 0),
       (5002, 1002, '主动降噪蓝牙耳机', 800, 800, 0, 0, NOW(), NOW(), 0),
       (5003, 1003, '轻薄笔记本 14 英寸', 200, 200, 0, 0, NOW(), NOW(), 0),
       (5004, 1004, '机械键盘 87 键', 600, 600, 0, 0, NOW(), NOW(), 0),
       (5005, 1005, '变频空调 1.5 匹', 120, 120, 0, 0, NOW(), NOW(), 0),
       (5006, 1006, '轻便运动跑鞋', 300, 300, 0, 0, NOW(), NOW(), 0);
