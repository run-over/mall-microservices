-- =====================================================================
-- 商品库：mall_product
-- =====================================================================
DROP DATABASE IF EXISTS `mall_product`;
CREATE DATABASE `mall_product` DEFAULT CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci;
USE `mall_product`;

CREATE TABLE `t_category`
(
    `id`          BIGINT      NOT NULL COMMENT '主键',
    `name`        VARCHAR(32) NOT NULL COMMENT '类目名称',
    `parent_id`   BIGINT      NOT NULL DEFAULT 0 COMMENT '父类目 ID，0 为一级类目',
    `sort`        INT         NOT NULL DEFAULT 0 COMMENT '排序值',
    `status`      TINYINT     NOT NULL DEFAULT 1 COMMENT '状态：1 启用，0 停用',
    `create_time` DATETIME             DEFAULT NULL,
    `update_time` DATETIME             DEFAULT NULL,
    `deleted`     TINYINT     NOT NULL DEFAULT 0,
    PRIMARY KEY (`id`),
    KEY `idx_parent_id` (`parent_id`)
) ENGINE = InnoDB
  DEFAULT CHARSET = utf8mb4 COMMENT ='商品类目表';

CREATE TABLE `t_product`
(
    `id`             BIGINT         NOT NULL COMMENT '主键',
    `category_id`    BIGINT         NOT NULL COMMENT '类目 ID',
    `name`           VARCHAR(128)   NOT NULL COMMENT '商品名称',
    `subtitle`       VARCHAR(255)            DEFAULT NULL COMMENT '卖点副标题',
    `brand`          VARCHAR(64)             DEFAULT NULL COMMENT '品牌',
    `price`          DECIMAL(10, 2) NOT NULL COMMENT '售价',
    `original_price` DECIMAL(10, 2)          DEFAULT NULL COMMENT '划线价',
    `main_image`     VARCHAR(255)            DEFAULT NULL COMMENT '主图地址',
    `detail`         TEXT                    DEFAULT NULL COMMENT '详情描述',
    `sales`          INT            NOT NULL DEFAULT 0 COMMENT '累计销量',
    `unit`           VARCHAR(16)             DEFAULT '件' COMMENT '计量单位',
    `status`         TINYINT        NOT NULL DEFAULT 1 COMMENT '状态：1 上架，0 下架',
    `create_time`    DATETIME                DEFAULT NULL,
    `update_time`    DATETIME                DEFAULT NULL,
    `deleted`        TINYINT        NOT NULL DEFAULT 0,
    PRIMARY KEY (`id`),
    KEY `idx_category_id` (`category_id`),
    KEY `idx_name` (`name`),
    KEY `idx_status` (`status`)
) ENGINE = InnoDB
  DEFAULT CHARSET = utf8mb4 COMMENT ='商品表';

INSERT INTO `t_category` (`id`, `name`, `parent_id`, `sort`, `status`, `create_time`, `update_time`, `deleted`)
VALUES (1, '手机数码', 0, 1, 1, NOW(), NOW(), 0),
       (2, '电脑办公', 0, 2, 1, NOW(), NOW(), 0),
       (3, '家用电器', 0, 3, 1, NOW(), NOW(), 0),
       (4, '服饰鞋包', 0, 4, 1, NOW(), NOW(), 0);

INSERT INTO `t_product` (`id`, `category_id`, `name`, `subtitle`, `brand`, `price`, `original_price`, `main_image`,
                         `detail`, `sales`, `unit`, `status`, `create_time`, `update_time`, `deleted`)
VALUES (1001, 1, '智能手机 Pro 12+256G', '骁龙旗舰芯片，一亿像素主摄', '星辰', 3999.00, 4499.00,
        'https://img.mall.com/p1001.jpg', '6.7 英寸 OLED 直屏，5000mAh 电池，支持 80W 快充。', 128, '台', 1, NOW(), NOW(), 0),
       (1002, 1, '主动降噪蓝牙耳机', '40dB 深度降噪，单次续航 8 小时', '声悦', 499.00, 599.00,
        'https://img.mall.com/p1002.jpg', '双设备连接，入耳检测，IPX5 防水。', 356, '副', 1, NOW(), NOW(), 0),
       (1003, 2, '轻薄笔记本 14 英寸', '2.8K 高色域屏，重 1.32kg', '云帆', 5299.00, 5999.00,
        'https://img.mall.com/p1003.jpg', '16G 内存 + 512G 固态，全金属机身。', 76, '台', 1, NOW(), NOW(), 0),
       (1004, 2, '机械键盘 87 键', '热插拔轴体，三模连接', '键入', 329.00, 399.00,
        'https://img.mall.com/p1004.jpg', 'PBT 键帽，Gasket 结构，支持自定义灯效。', 512, '个', 1, NOW(), NOW(), 0),
       (1005, 3, '变频空调 1.5 匹', '新一级能效，静音自清洁', '清风', 2699.00, 2999.00,
        'https://img.mall.com/p1005.jpg', '适用 12-18 平方米房间，APP 远程控制。', 43, '台', 1, NOW(), NOW(), 0),
       (1006, 4, '轻便运动跑鞋', '回弹中底，透气网面', '疾风', 359.00, 459.00,
        'https://img.mall.com/p1006.jpg', '适合日常慢跑与通勤，男款女款同价。', 289, '双', 1, NOW(), NOW(), 0);
