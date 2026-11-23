-- =====================================================================
-- 用户库：mall_user
-- 说明：一个微服务一个独立数据库，服务之间不跨库 JOIN
-- =====================================================================
DROP DATABASE IF EXISTS `mall_user`;
CREATE DATABASE `mall_user` DEFAULT CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci;
USE `mall_user`;

-- ---------------------------------------------------------------------
-- 用户表
-- ---------------------------------------------------------------------
CREATE TABLE `t_user`
(
    `id`              BIGINT       NOT NULL COMMENT '主键（雪花 ID）',
    `username`        VARCHAR(32)  NOT NULL COMMENT '登录名',
    `password`        VARCHAR(100) NOT NULL COMMENT 'BCrypt 密码密文',
    `nickname`        VARCHAR(32)           DEFAULT NULL COMMENT '昵称',
    `phone`           VARCHAR(20)           DEFAULT NULL COMMENT '手机号',
    `email`           VARCHAR(64)           DEFAULT NULL COMMENT '邮箱',
    `gender`          TINYINT               DEFAULT 0 COMMENT '性别：1 男，2 女，0 未知',
    `role`            VARCHAR(20)  NOT NULL DEFAULT 'ROLE_USER' COMMENT '角色：ROLE_ADMIN / ROLE_USER',
    `status`          TINYINT      NOT NULL DEFAULT 1 COMMENT '状态：1 正常，0 禁用',
    `last_login_time` DATETIME              DEFAULT NULL COMMENT '最后登录时间',
    `create_time`     DATETIME              DEFAULT NULL COMMENT '创建时间',
    `update_time`     DATETIME              DEFAULT NULL COMMENT '更新时间',
    `deleted`         TINYINT      NOT NULL DEFAULT 0 COMMENT '逻辑删除：0 未删除，1 已删除',
    PRIMARY KEY (`id`),
    UNIQUE KEY `uk_username` (`username`),
    KEY `idx_phone` (`phone`)
) ENGINE = InnoDB
  DEFAULT CHARSET = utf8mb4 COMMENT ='用户表';

-- ---------------------------------------------------------------------
-- 收货地址表
-- ---------------------------------------------------------------------
CREATE TABLE `t_address`
(
    `id`          BIGINT      NOT NULL COMMENT '主键',
    `user_id`     BIGINT      NOT NULL COMMENT '所属用户',
    `receiver`    VARCHAR(32) NOT NULL COMMENT '收件人',
    `phone`       VARCHAR(20) NOT NULL COMMENT '收件电话',
    `province`    VARCHAR(32)          DEFAULT NULL COMMENT '省',
    `city`        VARCHAR(32)          DEFAULT NULL COMMENT '市',
    `district`    VARCHAR(32)          DEFAULT NULL COMMENT '区/县',
    `detail`      VARCHAR(255)         DEFAULT NULL COMMENT '详细地址',
    `is_default`  TINYINT     NOT NULL DEFAULT 0 COMMENT '是否默认：1 是，0 否',
    `create_time` DATETIME             DEFAULT NULL COMMENT '创建时间',
    `update_time` DATETIME             DEFAULT NULL COMMENT '更新时间',
    `deleted`     TINYINT     NOT NULL DEFAULT 0 COMMENT '逻辑删除标记',
    PRIMARY KEY (`id`),
    KEY `idx_user_id` (`user_id`)
) ENGINE = InnoDB
  DEFAULT CHARSET = utf8mb4 COMMENT ='收货地址表';

-- ---------------------------------------------------------------------
-- 初始化数据：管理员 admin / admin123，普通用户 test / 123456
-- 密码为 BCrypt 密文，与 PasswordUtil.encode 生成的格式一致
-- ---------------------------------------------------------------------
INSERT INTO `t_user` (`id`, `username`, `password`, `nickname`, `phone`, `email`, `gender`, `role`, `status`,
                      `create_time`, `update_time`, `deleted`)
VALUES (1, 'admin', '$2a$10$hQtIqQ4HshZWwf3wLjcPFOdfm5y2aKaDF8ZJtPS0GHvbvWjN/xH7O', '系统管理员', '13800000001',
        'admin@mall.com', 1, 'ROLE_ADMIN', 1, NOW(), NOW(), 0),
       (2, 'test', '$2a$10$/bzEsfZNibaFGxKbTwgJTuO4yaKnyXjI/NM0.S7rtOzMQPqdU8ED.', '测试用户', '13800000002',
        'test@mall.com', 1, 'ROLE_USER', 1, NOW(), NOW(), 0);

INSERT INTO `t_address` (`id`, `user_id`, `receiver`, `phone`, `province`, `city`, `district`, `detail`, `is_default`,
                         `create_time`, `update_time`, `deleted`)
VALUES (1001, 2, '张三', '13800000002', '广东省', '广州市', '增城区', '荔新公路 1 号广州华商学院', 1, NOW(), NOW(), 0),
       (1002, 2, '张三', '13800000002', '广东省', '深圳市', '南山区', '科技园南路 8 号', 0, NOW(), NOW(), 0);
