-- ========================================
-- 电商智能运营平台 - 数据库初始化脚本
-- 创建时间: 2026-07-09
-- 说明: 基于需求规格说明书 v1.0 设计
-- ========================================

CREATE DATABASE IF NOT EXISTS `ecommerce_platform` DEFAULT CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci;
USE `ecommerce_platform`;

-- ----------------------------------------
-- 1. 用户表 (user)
-- 对应需求: 个人中心页面（会员等级管理模块）
-- ----------------------------------------
DROP TABLE IF EXISTS `user`;
CREATE TABLE `user` (
    `id`          BIGINT       NOT NULL AUTO_INCREMENT COMMENT '用户ID',
    `username`    VARCHAR(50)  NOT NULL COMMENT '用户名',
    `password`    VARCHAR(128) NOT NULL COMMENT '密码',
    `nickname`    VARCHAR(50)  DEFAULT NULL COMMENT '昵称',
    `role`        VARCHAR(20)  NOT NULL DEFAULT 'seller' COMMENT '角色',
    `deleted`     TINYINT      NOT NULL DEFAULT 0 COMMENT '逻辑删除: 0-未删除 1-已删除',
    `create_time` DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `update_time` DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    PRIMARY KEY (`id`),
    UNIQUE KEY `uk_username` (`username`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='用户表';

-- ----------------------------------------
-- 2. 商品分类表 (category)
-- 对应需求: 用户偏好分类依据、商品分类管理
-- ----------------------------------------
DROP TABLE IF EXISTS `category`;
CREATE TABLE `category` (
    `id`          BIGINT       NOT NULL AUTO_INCREMENT COMMENT '分类ID',
    `name`        VARCHAR(50)  NOT NULL COMMENT '分类名称',
    `parent_id`   BIGINT       DEFAULT NULL COMMENT '父分类ID(顶级分类为NULL)',
    `sort_order`  INT          NOT NULL DEFAULT 0 COMMENT '排序序号',
    `deleted`     TINYINT      NOT NULL DEFAULT 0 COMMENT '逻辑删除: 0-未删除 1-已删除',
    `create_time` DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `update_time` DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    PRIMARY KEY (`id`),
    KEY `idx_parent_id` (`parent_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='商品分类表';

-- ----------------------------------------
-- 3. 商品表 (product)
-- 对应需求: 商品管理页面（商品管理模块+库存管理模块）
-- ----------------------------------------
DROP TABLE IF EXISTS `product`;
CREATE TABLE `product` (
    `id`          BIGINT       NOT NULL AUTO_INCREMENT COMMENT '商品ID',
    `seller_id`   BIGINT       NOT NULL COMMENT '卖家用户ID',
    `category_id` BIGINT       DEFAULT NULL COMMENT '商品分类ID',
    `name`        VARCHAR(50)  NOT NULL COMMENT '商品名称(最多7字)',
    `description` VARCHAR(200) DEFAULT NULL COMMENT '商品描述(最多50字)',
    `image`       VARCHAR(255) DEFAULT NULL COMMENT '商品图片路径',
    `price`       DECIMAL(10,2) NOT NULL COMMENT '商品原价(0~10000之间，不含0和10000)',
    `stock`       INT          NOT NULL DEFAULT 0 COMMENT '商品库存',
    `on_sale`     TINYINT      NOT NULL DEFAULT 0 COMMENT '是否上架: 0-下架 1-上架',
    `rating`      DECIMAL(2,1)  NOT NULL DEFAULT 0.0 COMMENT '平均评分(1~5,小数截断)',
    `deleted`     TINYINT      NOT NULL DEFAULT 0 COMMENT '逻辑删除: 0-未删除 1-已删除',
    `create_time` DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `update_time` DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    PRIMARY KEY (`id`),
    KEY `idx_seller_id` (`seller_id`),
    KEY `idx_category_id` (`category_id`),
    KEY `idx_on_sale` (`on_sale`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='商品表';

-- ----------------------------------------
-- 4. 临时库存锁定表 (stock_lock)
-- 对应需求: 商品购买时临时减少库存，等待用户付款
-- ----------------------------------------
DROP TABLE IF EXISTS `stock_lock`;
CREATE TABLE `stock_lock` (
    `id`          BIGINT       NOT NULL AUTO_INCREMENT COMMENT '锁定ID',
    `product_id`  BIGINT       NOT NULL COMMENT '商品ID',
    `user_id`     BIGINT       NOT NULL COMMENT '锁定用户ID(买家)',
    `quantity`    INT          NOT NULL DEFAULT 1 COMMENT '锁定数量',
    `status`      TINYINT      NOT NULL DEFAULT 0 COMMENT '锁定状态: 0-已锁定 1-已付款 2-已取消',
    `create_time` DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '锁定时间',
    `update_time` DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    PRIMARY KEY (`id`),
    KEY `idx_product_id` (`product_id`),
    KEY `idx_user_id` (`user_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='临时库存锁定表';

-- ----------------------------------------
-- 5. 商品评论表 (product_review)
-- 对应需求: 商品详情页面（客户评论情感分析模块）
-- ----------------------------------------
DROP TABLE IF EXISTS `product_review`;
CREATE TABLE `product_review` (
    `id`          BIGINT       NOT NULL AUTO_INCREMENT COMMENT '评论ID',
    `product_id`  BIGINT       NOT NULL COMMENT '商品ID',
    `user_id`     BIGINT       NOT NULL COMMENT '评论用户ID(买家)',
    `order_id`    BIGINT       NOT NULL COMMENT '关联订单ID',
    `content`     VARCHAR(500) NOT NULL COMMENT '评论内容',
    `rating`      TINYINT      NOT NULL COMMENT '评分(1~5星)',
    `sentiment`   TINYINT      DEFAULT NULL COMMENT '情感分析结果: 1-正面 0-中性 -1-负面',
    `deleted`     TINYINT      NOT NULL DEFAULT 0 COMMENT '逻辑删除: 0-未删除 1-已删除',
    `create_time` DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '评论时间',
    `update_time` DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    PRIMARY KEY (`id`),
    KEY `idx_product_id` (`product_id`),
    KEY `idx_user_id` (`user_id`),
    KEY `idx_order_id` (`order_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='商品评论表';

-- ----------------------------------------
-- 6. 订单表 (orders)
-- 对应需求: 订单管理页面（订单管理模块+售后工单流转模块）
-- ----------------------------------------
DROP TABLE IF EXISTS `orders`;
CREATE TABLE `orders` (
    `id`              BIGINT       NOT NULL AUTO_INCREMENT COMMENT '订单ID(自增主键)',
    `order_no`        INT          NOT NULL COMMENT '订单编号(1000~9999循环)',
    `product_id`      BIGINT       NOT NULL COMMENT '商品ID',
    `product_name`    VARCHAR(50)  NOT NULL COMMENT '商品名称(下单时快照)',
    `product_image`   VARCHAR(255) DEFAULT NULL COMMENT '商品图片(下单时快照)',
    `quantity`        INT          NOT NULL DEFAULT 1 COMMENT '购买数量',
    `unit_price`      DECIMAL(10,2) NOT NULL COMMENT '下单时单价(已优惠后)',
    `total_amount`    DECIMAL(10,2) NOT NULL COMMENT '订单总金额',
    `vip_discount`    DECIMAL(10,2) NOT NULL DEFAULT 0.00 COMMENT 'VIP优惠金额',
    `activity_discount_id` BIGINT DEFAULT NULL COMMENT '活动折扣关联ID(若无活动则为NULL)',
    `discount_rate`   DECIMAL(2,1)  DEFAULT NULL COMMENT '折扣率(如0.8表示8折)',
    `buyer_id`        BIGINT       NOT NULL COMMENT '买家用户ID',
    `seller_id`       BIGINT       NOT NULL COMMENT '卖家用户ID',
    `status`          TINYINT      NOT NULL DEFAULT 0 COMMENT '订单状态: 0-待付款 1-已付款待发货 2-已发货 3-已完成 4-已取消',
    `is_rated`        TINYINT      NOT NULL DEFAULT 0 COMMENT '是否已评分: 0-否 1-是',
    `rate_expire_time` DATETIME    DEFAULT NULL COMMENT '评分过期时间(下单后一个工作日)',
    `refund_status`   TINYINT      NOT NULL DEFAULT 0 COMMENT '退款状态: 0-无退款 1-退款中 2-退款成功 3-退款拒绝',
    `deleted`         TINYINT      NOT NULL DEFAULT 0 COMMENT '逻辑删除: 0-未删除 1-已删除',
    `create_time`     DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `update_time`     DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    PRIMARY KEY (`id`),
    UNIQUE KEY `idx_order_no` (`order_no`),
    KEY `idx_buyer_id` (`buyer_id`),
    KEY `idx_seller_id` (`seller_id`),
    KEY `idx_product_id` (`product_id`),
    KEY `idx_status` (`status`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='订单表';

-- ----------------------------------------
-- 7. 售后退款/工单表 (refund)
-- 对应需求: 订单管理页面（售后工单流转模块）
-- ----------------------------------------
DROP TABLE IF EXISTS `refund`;
CREATE TABLE `refund` (
    `id`          BIGINT       NOT NULL AUTO_INCREMENT COMMENT '退款/工单ID',
    `order_id`    BIGINT       NOT NULL COMMENT '关联订单ID',
    `buyer_id`    BIGINT       NOT NULL COMMENT '买家用户ID',
    `seller_id`   BIGINT       NOT NULL COMMENT '卖家用户ID',
    `reason`      VARCHAR(500) NOT NULL COMMENT '退款原因',
    `status`      TINYINT      NOT NULL DEFAULT 0 COMMENT '工单状态: 0-待处理 1-同意退款 2-拒绝退款',
    `reply`       VARCHAR(500) DEFAULT NULL COMMENT '卖家回复',
    `deleted`     TINYINT      NOT NULL DEFAULT 0 COMMENT '逻辑删除: 0-未删除 1-已删除',
    `create_time` DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '申请时间',
    `update_time` DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '处理时间',
    PRIMARY KEY (`id`),
    KEY `idx_order_id` (`order_id`),
    KEY `idx_buyer_id` (`buyer_id`),
    KEY `idx_seller_id` (`seller_id`),
    KEY `idx_status` (`status`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='售后退款/工单表';

-- ----------------------------------------
-- 8. 营销活动表 (activity)
-- 对应需求: 活动设置页面（营销活动设置模块）
-- ----------------------------------------
DROP TABLE IF EXISTS `activity`;
CREATE TABLE `activity` (
    `id`          BIGINT       NOT NULL AUTO_INCREMENT COMMENT '活动ID',
    `name`        VARCHAR(100) NOT NULL COMMENT '活动名称(最多25字)',
    `description` VARCHAR(300) DEFAULT NULL COMMENT '活动描述(最多75字)',
    `status`      TINYINT      NOT NULL DEFAULT 0 COMMENT '活动状态: 0-草稿 1-已发布 2-已结束',
    `creator_id`  BIGINT       NOT NULL COMMENT '创建者(卖家)用户ID',
    `start_time`  DATETIME     DEFAULT NULL COMMENT '活动开始时间',
    `end_time`    DATETIME     DEFAULT NULL COMMENT '活动结束时间',
    `deleted`     TINYINT      NOT NULL DEFAULT 0 COMMENT '逻辑删除: 0-未删除 1-已删除',
    `create_time` DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `update_time` DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    PRIMARY KEY (`id`),
    KEY `idx_creator_id` (`creator_id`),
    KEY `idx_status` (`status`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='营销活动表';

-- ----------------------------------------
-- 9. 活动商品折扣关联表 (activity_product)
-- 对应需求: 活动设置页面（折扣商品管理）
-- ----------------------------------------
DROP TABLE IF EXISTS `activity_product`;
CREATE TABLE `activity_product` (
    `id`            BIGINT        NOT NULL AUTO_INCREMENT COMMENT 'ID',
    `activity_id`   BIGINT        NOT NULL COMMENT '活动ID',
    `product_id`    BIGINT        NOT NULL COMMENT '商品ID',
    `discount`      DECIMAL(2,1)  NOT NULL DEFAULT 0.9 COMMENT '折扣度(如0.9表示9折,最低0.9)',
    `discount_price` DECIMAL(10,2) NOT NULL COMMENT '折后价(原价*折扣度)',
    `original_price` DECIMAL(10,2) NOT NULL COMMENT '商品原价(快照)',
    `deleted`       TINYINT       NOT NULL DEFAULT 0 COMMENT '逻辑删除: 0-未删除 1-已删除',
    `create_time`   DATETIME      NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `update_time`   DATETIME      NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    PRIMARY KEY (`id`),
    KEY `idx_activity_id` (`activity_id`),
    KEY `idx_product_id` (`product_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='活动商品折扣关联表';

-- ----------------------------------------
-- 10. 用户消息表 (user_message)
-- 对应需求: 个人中心页面（近期消息）
-- ----------------------------------------
DROP TABLE IF EXISTS `user_message`;
CREATE TABLE `user_message` (
    `id`          BIGINT       NOT NULL AUTO_INCREMENT COMMENT '消息ID',
    `user_id`     BIGINT       NOT NULL COMMENT '接收用户ID',
    `sender_id`   BIGINT       DEFAULT NULL COMMENT '发送者ID(系统消息为NULL)',
    `type`        TINYINT      NOT NULL COMMENT '消息类型: 1-退货申请 2-退货答复 3-折扣活动 4-订单通知 5-系统消息',
    `title`       VARCHAR(100) NOT NULL COMMENT '消息标题',
    `content`     VARCHAR(500) NOT NULL COMMENT '消息内容',
    `is_read`     TINYINT      NOT NULL DEFAULT 0 COMMENT '是否已读: 0-未读 1-已读',
    `related_id`  BIGINT       DEFAULT NULL COMMENT '关联业务ID(订单ID/活动ID等)',
    `deleted`     TINYINT      NOT NULL DEFAULT 0 COMMENT '逻辑删除: 0-未删除 1-已删除',
    `create_time` DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `update_time` DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    PRIMARY KEY (`id`),
    KEY `idx_user_id` (`user_id`),
    KEY `idx_type` (`type`),
    KEY `idx_is_read` (`is_read`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='用户消息表';

-- ----------------------------------------
-- 11. VIP等级配置表 (vip_config)
-- 对应需求: 个人中心页面（会员等级管理模块）
-- ----------------------------------------
DROP TABLE IF EXISTS `vip_config`;
CREATE TABLE `vip_config` (
    `id`               BIGINT       NOT NULL AUTO_INCREMENT COMMENT 'ID',
    `level`            TINYINT      NOT NULL COMMENT 'VIP等级(1~5)',
    `name`             VARCHAR(20)  NOT NULL COMMENT '等级名称',
    `threshold`        INT          NOT NULL COMMENT '满减阈值(元)',
    `discount_amount`  DECIMAL(10,2) NOT NULL COMMENT '满减金额(元)',
    `description`      VARCHAR(100) DEFAULT NULL COMMENT '等级描述',
    `deleted`          TINYINT      NOT NULL DEFAULT 0 COMMENT '逻辑删除: 0-未删除 1-已删除',
    `create_time`      DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `update_time`      DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    PRIMARY KEY (`id`),
    UNIQUE KEY `uk_level` (`level`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='VIP等级配置表';

-- ----------------------------------------
-- 12. 智能客服对话记录表 (chat_history)
-- 对应需求: 商品详情页面（智能客服模块）
-- ----------------------------------------
DROP TABLE IF EXISTS `chat_history`;
CREATE TABLE `chat_history` (
    `id`          BIGINT       NOT NULL AUTO_INCREMENT COMMENT 'ID',
    `user_id`     BIGINT       NOT NULL COMMENT '用户ID',
    `product_id`  BIGINT       DEFAULT NULL COMMENT '关联商品ID(可为空)',
    `question`    VARCHAR(500) NOT NULL COMMENT '用户问题',
    `answer`      VARCHAR(2000) NOT NULL COMMENT 'AI回答',
    `deleted`     TINYINT      NOT NULL DEFAULT 0 COMMENT '逻辑删除: 0-未删除 1-已删除',
    `create_time` DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `update_time` DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    PRIMARY KEY (`id`),
    KEY `idx_user_id` (`user_id`),
    KEY `idx_product_id` (`product_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='智能客服对话记录表';

-- ----------------------------------------
-- 13. AI营销文案生成记录表 (ai_marketing)
-- 对应需求: 活动设置页面（自动生成营销文案和活动策划建议模块）
-- ----------------------------------------
DROP TABLE IF EXISTS `ai_marketing`;
CREATE TABLE `ai_marketing` (
    `id`              BIGINT       NOT NULL AUTO_INCREMENT COMMENT 'ID',
    `user_id`         BIGINT       NOT NULL COMMENT '用户ID(卖家)',
    `user_requirement` VARCHAR(1000) NOT NULL COMMENT '用户输入的活动需求描述',
    `generated_name`  VARCHAR(100) DEFAULT NULL COMMENT 'AI生成的活动名称',
    `generated_desc`  VARCHAR(300) DEFAULT NULL COMMENT 'AI生成的活动描述',
    `suggestion`      VARCHAR(1000) DEFAULT NULL COMMENT 'AI活动策划建议',
    `deleted`         TINYINT      NOT NULL DEFAULT 0 COMMENT '逻辑删除: 0-未删除 1-已删除',
    `create_time`     DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `update_time`     DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    PRIMARY KEY (`id`),
    KEY `idx_user_id` (`user_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='AI营销文案生成记录表';

-- ----------------------------------------
-- 14. 评论情感分析总结表 (sentiment_summary)
-- 对应需求: 商品详情页面（客户评论情感分析模块 - 智能总结）
-- ----------------------------------------
DROP TABLE IF EXISTS `sentiment_summary`;
CREATE TABLE `sentiment_summary` (
    `id`           BIGINT       NOT NULL AUTO_INCREMENT COMMENT 'ID',
    `product_id`   BIGINT       NOT NULL COMMENT '商品ID',
    `summary_text` VARCHAR(2000) NOT NULL COMMENT 'AI情感分析总结内容',
    `positive_count`  INT      NOT NULL DEFAULT 0 COMMENT '正面评论数',
    `neutral_count`   INT      NOT NULL DEFAULT 0 COMMENT '中性评论数',
    `negative_count`  INT      NOT NULL DEFAULT 0 COMMENT '负面评论数',
    `recommend`    TINYINT      NOT NULL DEFAULT 0 COMMENT '是否推荐购买: 0-不推荐 1-推荐',
    `deleted`      TINYINT      NOT NULL DEFAULT 0 COMMENT '逻辑删除: 0-未删除 1-已删除',
    `create_time`  DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '生成时间',
    `update_time`  DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    PRIMARY KEY (`id`),
    KEY `idx_product_id` (`product_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='评论情感分析总结表';

-- ========================================
-- 初始化数据
-- ========================================

-- VIP等级配置初始数据
INSERT INTO `vip_config` (`level`, `name`, `threshold`, `discount_amount`, `description`) VALUES
(1, 'VIP I',   50,  2.00, '每满50元减2元'),
(2, 'VIP II',  50,  4.00, '每满50元减4元'),
(3, 'VIP III', 50,  6.00, '每满50元减6元'),
(4, 'VIP IV',  50,  8.00, '每满50元减8元'),
(5, 'VIP V',   50, 10.00, '每满50元减10元');

-- 商品分类初始数据
INSERT INTO `category` (`name`, `parent_id`, `sort_order`) VALUES
('数码电子', NULL, 1),
('服装鞋帽', NULL, 2),
('食品饮料', NULL, 3),
('家居生活', NULL, 4),
('图书文具', NULL, 5),
('运动户外', NULL, 6);

-- 测试用户
INSERT INTO `user` (`username`, `password`, `nickname`, `role`, `deleted`) VALUES
('admin', '123456', '管理员', 'seller', 0);

-- 测试商品数据
INSERT INTO `product` (`seller_id`, `category_id`, `name`, `description`, `image`, `price`, `stock`, `on_sale`, `rating`, `deleted`) VALUES
(1, 1, '无线蓝牙耳机', '高音质降噪蓝牙耳机，续航30小时', NULL, 299.00, 150, 1, 4.5, 0),
(1, 2, '纯棉短袖T恤', '夏季透气舒适纯棉面料，多色可选', NULL, 89.00, 500, 1, 4.2, 0);

-- 测试订单数据
INSERT INTO `orders` (`order_no`, `product_id`, `product_name`, `product_image`, `quantity`, `unit_price`, `total_amount`, `vip_discount`, `discount_rate`, `buyer_id`, `seller_id`, `status`, `is_rated`, `deleted`) VALUES
(1001, 1, '无线蓝牙耳机', NULL, 2, 299.00, 598.00, 0.00, NULL, 2, 1, 3, 1, 0),
(1002, 2, '纯棉短袖T恤', NULL, 3, 89.00, 267.00, 0.00, NULL, 2, 1, 1, 0, 0);
