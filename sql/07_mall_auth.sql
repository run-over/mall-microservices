-- =====================================================================
-- 认证库：mall_auth
-- 说明：存放 OAuth2 客户端信息与令牌，令牌持久化到数据库后重启不丢失，
--       也便于网关以外的服务直接查询令牌状态（例如强制下线）
-- =====================================================================
DROP DATABASE IF EXISTS `mall_auth`;
CREATE DATABASE `mall_auth` DEFAULT CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci;
USE `mall_auth`;

-- ---------------------------------------------------------------------
-- 客户端信息：mall-client 具备 password 与 refresh_token 两种授权模式
-- client_secret 为 "mall-secret" 的 BCrypt 密文
-- ---------------------------------------------------------------------
CREATE TABLE `oauth_client_details`
(
    `client_id`               VARCHAR(256) NOT NULL COMMENT '客户端 ID',
    `resource_ids`            VARCHAR(256)          DEFAULT NULL COMMENT '可访问的资源 ID',
    `client_secret`           VARCHAR(256)          DEFAULT NULL COMMENT '客户端密钥（BCrypt 密文）',
    `scope`                   VARCHAR(256)          DEFAULT NULL COMMENT '授权范围',
    `authorized_grant_types`  VARCHAR(256)          DEFAULT NULL COMMENT '授权模式',
    `web_server_redirect_uri` VARCHAR(256)          DEFAULT NULL COMMENT '回调地址',
    `authorities`             VARCHAR(256)          DEFAULT NULL COMMENT '客户端权限',
    `access_token_validity`   INT                   DEFAULT NULL COMMENT '访问令牌有效期（秒）',
    `refresh_token_validity`  INT                   DEFAULT NULL COMMENT '刷新令牌有效期（秒）',
    `additional_information`  VARCHAR(4096)         DEFAULT NULL COMMENT '扩展信息',
    `autoapprove`             VARCHAR(256)          DEFAULT NULL COMMENT '自动授权范围',
    PRIMARY KEY (`client_id`)
) ENGINE = InnoDB
  DEFAULT CHARSET = utf8mb4 COMMENT ='OAuth2 客户端信息';

-- ---------------------------------------------------------------------
-- 访问令牌：token 与 authentication 采用 JVM 序列化二进制存储
-- 使用 MEDIUMBLOB 容纳权限较多时的令牌内容
-- ---------------------------------------------------------------------
CREATE TABLE `oauth_access_token`
(
    `id`                BIGINT AUTO_INCREMENT COMMENT '自增主键',
    `token_id`          VARCHAR(256) DEFAULT NULL COMMENT '令牌值的 MD5',
    `token`             MEDIUMBLOB   DEFAULT NULL COMMENT '令牌二进制',
    `authentication_id` VARCHAR(256) DEFAULT NULL COMMENT '认证信息标识',
    `user_name`         VARCHAR(256) DEFAULT NULL COMMENT '用户名',
    `client_id`         VARCHAR(256) DEFAULT NULL COMMENT '客户端 ID',
    `authentication`    MEDIUMBLOB   DEFAULT NULL COMMENT '认证信息二进制',
    `refresh_token`     VARCHAR(256) DEFAULT NULL COMMENT '关联的刷新令牌',
    PRIMARY KEY (`id`),
    KEY `idx_token_id` (`token_id`),
    KEY `idx_auth_id` (`authentication_id`),
    KEY `idx_refresh_token` (`refresh_token`)
) ENGINE = InnoDB
  DEFAULT CHARSET = utf8mb4 COMMENT ='OAuth2 访问令牌';

CREATE TABLE `oauth_refresh_token`
(
    `id`             BIGINT AUTO_INCREMENT COMMENT '自增主键',
    `token_id`       VARCHAR(256) DEFAULT NULL,
    `token`          MEDIUMBLOB   DEFAULT NULL,
    `authentication` MEDIUMBLOB   DEFAULT NULL,
    PRIMARY KEY (`id`),
    KEY `idx_token_id` (`token_id`)
) ENGINE = InnoDB
  DEFAULT CHARSET = utf8mb4 COMMENT ='OAuth2 刷新令牌';

INSERT INTO `oauth_client_details` (`client_id`, `resource_ids`, `client_secret`, `scope`, `authorized_grant_types`,
                                    `web_server_redirect_uri`, `authorities`, `access_token_validity`,
                                    `refresh_token_validity`, `additional_information`, `autoapprove`)
VALUES ('mall-client', 'mall-resource', '$2a$10$9MLpBzJ9dA.DKI1IfiTgsuOQBGH/Bw3WCdoS5/AAg3f3Z3NHYmjwC', 'all',
        'password,refresh_token', NULL, 'ROLE_ADMIN,ROLE_USER', 7200, 604800, NULL, 'true');
