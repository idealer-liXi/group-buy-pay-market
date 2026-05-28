# ************************************************************
# Merged SQL dump for group_buy_pay_market
# Combines: 01-s-pay-mall-ddd-market (pay_order) + 02-group-buy-market (all other tables)
# ************************************************************

/*!40101 SET @OLD_CHARACTER_SET_CLIENT=@@CHARACTER_SET_CLIENT */;
/*!40101 SET @OLD_CHARACTER_SET_RESULTS=@@CHARACTER_SET_RESULTS */;
/*!40101 SET @OLD_COLLATION_CONNECTION=@@COLLATION_CONNECTION */;
SET NAMES utf8mb4;
/*!40014 SET @OLD_FOREIGN_KEY_CHECKS=@@FOREIGN_KEY_CHECKS, FOREIGN_KEY_CHECKS=0 */;
/*!40101 SET @OLD_SQL_MODE='NO_AUTO_VALUE_ON_ZERO', SQL_MODE='NO_AUTO_VALUE_ON_ZERO' */;
/*!40111 SET @OLD_SQL_NOTES=@@SQL_NOTES, SQL_NOTES=0 */;

CREATE database if NOT EXISTS `group_buy_pay_market` default character set utf8mb4 collate utf8mb4_0900_ai_ci;
use `group_buy_pay_market`;

DROP TABLE IF EXISTS `pay_order`;

CREATE TABLE `pay_order` (
  `id` int(10) unsigned NOT NULL AUTO_INCREMENT COMMENT 'Auto ID',
  `user_id` varchar(32) NOT NULL COMMENT 'User ID',
  `product_id` varchar(16) NOT NULL COMMENT 'Product ID',
  `product_name` varchar(64) NOT NULL COMMENT 'Product Name',
  `order_id` varchar(16) NOT NULL COMMENT 'Order ID',
  `order_time` datetime NOT NULL COMMENT 'Order Time',
  `total_amount` decimal(8,2) unsigned DEFAULT NULL COMMENT 'Order Amount',
  `status` varchar(32) NOT NULL COMMENT 'Order Status',
  `pay_url` varchar(2014) DEFAULT NULL COMMENT 'Pay Info',
  `pay_time` datetime DEFAULT NULL COMMENT 'Pay Time',
  `market_type` tinyint(1) DEFAULT NULL COMMENT 'Market Type',
  `market_deduction_amount` decimal(8,2) DEFAULT NULL COMMENT 'Market Discount',
  `pay_amount` decimal(8,2) NOT NULL COMMENT 'Pay Amount',
  `create_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT 'Create Time',
  `update_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT 'Update Time',
  PRIMARY KEY (`id`),
  UNIQUE KEY `uq_order_id` (`order_id`),
  KEY `idx_user_id_product_id` (`user_id`,`product_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;

LOCK TABLES `pay_order` WRITE;
/*!40000 ALTER TABLE `pay_order` DISABLE KEYS */;
/*!40000 ALTER TABLE `pay_order` ENABLE KEYS */;
UNLOCK TABLES;

DROP TABLE IF EXISTS `market_user`;

CREATE TABLE `market_user` (
  `id` bigint unsigned NOT NULL AUTO_INCREMENT COMMENT 'Auto ID',
  `user_id` varchar(64) NOT NULL COMMENT 'User ID',
  `login_type` varchar(16) NOT NULL DEFAULT 'FINGERPRINT' COMMENT 'Login Type',
  `display_name` varchar(64) NOT NULL COMMENT 'Display Name',
  `status` tinyint(1) NOT NULL DEFAULT '0' COMMENT 'Status',
  `first_login_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT 'First Login Time',
  `last_login_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT 'Last Login Time',
  `create_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT 'Create Time',
  `update_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT 'Update Time',
  PRIMARY KEY (`id`),
  UNIQUE KEY `uq_user_id` (`user_id`),
  KEY `idx_last_login_time` (`last_login_time`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='Market User';

DROP TABLE IF EXISTS `crowd_tags`;

CREATE TABLE `crowd_tags` (
  `id` int unsigned NOT NULL AUTO_INCREMENT COMMENT 'Auto ID',
  `tag_id` varchar(32) NOT NULL COMMENT 'Tag ID',
  `tag_name` varchar(64) NOT NULL COMMENT 'Tag Name',
  `tag_desc` varchar(256) NOT NULL COMMENT 'Tag Desc',
  `statistics` int NOT NULL COMMENT 'Statistics',
  `create_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT 'Create Time',
  `update_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT 'Update Time',
  PRIMARY KEY (`id`),
  UNIQUE KEY `uq_tag_id` (`tag_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='Crowd tags';

LOCK TABLES `crowd_tags` WRITE;
/*!40000 ALTER TABLE `crowd_tags` DISABLE KEYS */;
/*!40000 ALTER TABLE `crowd_tags` ENABLE KEYS */;
UNLOCK TABLES;

DROP TABLE IF EXISTS `crowd_tags_detail`;

CREATE TABLE `crowd_tags_detail` (
  `id` int unsigned NOT NULL AUTO_INCREMENT COMMENT 'Auto ID',
  `tag_id` varchar(32) NOT NULL COMMENT 'Tag ID',
  `user_id` varchar(64) NOT NULL COMMENT 'User ID',
  `create_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT 'Create Time',
  `update_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT 'Update Time',
  PRIMARY KEY (`id`),
  UNIQUE KEY `uq_tag_user` (`tag_id`,`user_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='Crowd tag details';

LOCK TABLES `crowd_tags_detail` WRITE;
/*!40000 ALTER TABLE `crowd_tags_detail` DISABLE KEYS */;
/*!40000 ALTER TABLE `crowd_tags_detail` ENABLE KEYS */;
UNLOCK TABLES;

DROP TABLE IF EXISTS `crowd_tags_job`;

CREATE TABLE `crowd_tags_job` (
  `id` int unsigned NOT NULL AUTO_INCREMENT COMMENT 'Auto ID',
  `tag_id` varchar(32) NOT NULL COMMENT 'Tag ID',
  `batch_id` varchar(8) NOT NULL COMMENT 'Batch ID',
  `tag_type` tinyint(1) NOT NULL DEFAULT '1' COMMENT 'Tag Type',
  `tag_rule` varchar(8) NOT NULL COMMENT 'Tag Rule',
  `stat_start_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT 'Stat Start Time',
  `stat_end_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT 'Stat End Time',
  `status` tinyint(1) NOT NULL DEFAULT '0' COMMENT 'Status',
  `create_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT 'Create Time',
  `update_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT 'Update Time',
  PRIMARY KEY (`id`),
  UNIQUE KEY `uq_batch_id` (`batch_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='Crowd tag job';

LOCK TABLES `crowd_tags_job` WRITE;
/*!40000 ALTER TABLE `crowd_tags_job` DISABLE KEYS */;
/*!40000 ALTER TABLE `crowd_tags_job` ENABLE KEYS */;
UNLOCK TABLES;

DROP TABLE IF EXISTS `group_buy_activity`;

CREATE TABLE `group_buy_activity` (
  `id` bigint unsigned NOT NULL AUTO_INCREMENT COMMENT 'Auto ID',
  `activity_id` bigint NOT NULL COMMENT 'Activity ID',
  `activity_name` varchar(128) NOT NULL COMMENT 'Activity Name',
  `discount_id` varchar(8) NOT NULL COMMENT 'Discount ID',
  `group_type` tinyint(1) NOT NULL DEFAULT '0' COMMENT 'Group Type',
  `take_limit_count` int NOT NULL DEFAULT '1' COMMENT 'Take Limit Count',
  `target` int NOT NULL DEFAULT '1' COMMENT 'Target Count',
  `valid_time` int NOT NULL DEFAULT '15' COMMENT 'Valid Time Minutes',
  `status` tinyint(1) NOT NULL DEFAULT '0' COMMENT 'Activity Status',
  `start_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT 'Start Time',
  `end_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT 'End Time',
  `tag_id` varchar(8) DEFAULT NULL COMMENT 'Tag ID',
  `tag_scope` varchar(4) DEFAULT NULL COMMENT 'Tag Scope',
  `create_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT 'Create Time',
  `update_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT 'Update Time',
  PRIMARY KEY (`id`),
  UNIQUE KEY `uq_activity_id` (`activity_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='Group buy activity';

LOCK TABLES `group_buy_activity` WRITE;
/*!40000 ALTER TABLE `group_buy_activity` DISABLE KEYS */;

INSERT INTO `group_buy_activity` (`activity_id`, `activity_name`, `discount_id`, `group_type`, `take_limit_count`, `target`, `valid_time`, `status`, `start_time`, `end_time`) VALUES
(9890001, '拼团读书节-直减优惠', '1', 1, 1, 3, 15, 1, '2025-01-01 00:00:00', '2026-12-31 23:59:59'),
(9890002, '拼团特惠-满减活动', '2', 1, 1, 5, 30, 1, '2025-01-01 00:00:00', '2026-12-31 23:59:59'),
(9890003, '新人专享-N元购', '3', 1, 1, 2, 10, 1, '2025-01-01 00:00:00', '2026-12-31 23:59:59');

/*!40000 ALTER TABLE `group_buy_activity` ENABLE KEYS */;
UNLOCK TABLES;

DROP TABLE IF EXISTS `group_buy_discount`;

CREATE TABLE `group_buy_discount` (
  `id` bigint unsigned NOT NULL AUTO_INCREMENT COMMENT 'Auto ID',
  `discount_id` varchar(8) NOT NULL COMMENT 'Discount ID',
  `discount_name` varchar(64) NOT NULL COMMENT 'Discount Name',
  `discount_desc` varchar(256) NOT NULL COMMENT 'Discount Desc',
  `discount_type` tinyint(1) NOT NULL DEFAULT '0' COMMENT 'Discount Type',
  `market_plan` varchar(4) NOT NULL DEFAULT 'ZJ' COMMENT 'Market Plan',
  `market_expr` varchar(32) NOT NULL COMMENT 'Market Expr',
  `tag_id` varchar(8) DEFAULT NULL COMMENT 'Tag ID',
  `status` tinyint(1) NOT NULL DEFAULT '0' COMMENT 'Discount Status',
  `create_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT 'Create Time',
  `update_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT 'Update Time',
  PRIMARY KEY (`id`),
  UNIQUE KEY `uq_discount_id` (`discount_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;

LOCK TABLES `group_buy_discount` WRITE;
/*!40000 ALTER TABLE `group_buy_discount` DISABLE KEYS */;

INSERT INTO `group_buy_discount` (`discount_id`, `discount_name`, `discount_desc`, `discount_type`, `market_plan`, `market_expr`, `status`) VALUES
('1', '直减10元', '拼团直减10元', 1, 'ZJ', '10', 0),
('2', '满100减20', '满100元减20元', 1, 'MJ', '100,20', 0),
('3', '9.9元购', 'N元购专享价9.9元', 1, 'N', '9.9', 0);

/*!40000 ALTER TABLE `group_buy_discount` ENABLE KEYS */;
UNLOCK TABLES;

DROP TABLE IF EXISTS `group_buy_order`;

CREATE TABLE `group_buy_order` (
  `id` int unsigned NOT NULL AUTO_INCREMENT COMMENT 'Auto ID',
  `team_id` varchar(8) NOT NULL COMMENT 'Team ID',
  `activity_id` bigint NOT NULL COMMENT 'Activity ID',
  `source` varchar(8) NOT NULL COMMENT 'Source',
  `channel` varchar(8) NOT NULL COMMENT 'Channel',
  `original_price` decimal(8,2) NOT NULL COMMENT 'Original Price',
  `deduction_price` decimal(8,2) NOT NULL COMMENT 'Deduction Price',
  `pay_price` decimal(8,2) NOT NULL COMMENT 'Pay Price',
  `target_count` int NOT NULL COMMENT 'Target Count',
  `complete_count` int NOT NULL COMMENT 'Complete Count',
  `lock_count` int NOT NULL COMMENT 'Lock Count',
  `status` tinyint(1) NOT NULL DEFAULT '0' COMMENT 'Status',
  `valid_start_time` datetime NOT NULL COMMENT 'Valid Start Time',
  `valid_end_time` datetime NOT NULL COMMENT 'Valid End Time',
  `notify_type` varchar(8) NOT NULL DEFAULT 'HTTP' COMMENT 'Notify Type',
  `notify_url` varchar(512) DEFAULT NULL COMMENT 'Notify URL',
  `create_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT 'Create Time',
  `update_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT 'Update Time',
  PRIMARY KEY (`id`),
  UNIQUE KEY `uq_team_id` (`team_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;

LOCK TABLES `group_buy_order` WRITE;
/*!40000 ALTER TABLE `group_buy_order` DISABLE KEYS */;
/*!40000 ALTER TABLE `group_buy_order` ENABLE KEYS */;
UNLOCK TABLES;

DROP TABLE IF EXISTS `group_buy_order_list`;

CREATE TABLE `group_buy_order_list` (
  `id` int unsigned NOT NULL AUTO_INCREMENT COMMENT 'Auto ID',
  `user_id` varchar(64) NOT NULL COMMENT 'User ID',
  `team_id` varchar(8) NOT NULL COMMENT 'Team ID',
  `order_id` varchar(12) NOT NULL COMMENT 'Order ID',
  `activity_id` bigint NOT NULL COMMENT 'Activity ID',
  `start_time` datetime NOT NULL COMMENT 'Start Time',
  `end_time` datetime NOT NULL COMMENT 'End Time',
  `goods_id` varchar(16) NOT NULL COMMENT 'Goods ID',
  `source` varchar(8) NOT NULL COMMENT 'Source',
  `channel` varchar(8) NOT NULL COMMENT 'Channel',
  `original_price` decimal(8,2) NOT NULL COMMENT 'Original Price',
  `deduction_price` decimal(8,2) NOT NULL COMMENT 'Deduction Price',
  `pay_price` decimal(8,2) NOT NULL COMMENT 'Pay Price',
  `status` tinyint(1) NOT NULL DEFAULT '0' COMMENT 'Status',
  `out_trade_no` varchar(12) NOT NULL COMMENT 'Out Trade No',
  `out_trade_time` datetime DEFAULT NULL COMMENT 'Out Trade Time',
  `biz_id` varchar(64) NOT NULL COMMENT 'Biz ID',
  `create_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT 'Create Time',
  `update_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT 'Update Time',
  PRIMARY KEY (`id`),
  UNIQUE KEY `uq_order_id` (`order_id`),
  KEY `idx_user_id_activity_id` (`user_id`,`activity_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;

LOCK TABLES `group_buy_order_list` WRITE;
/*!40000 ALTER TABLE `group_buy_order_list` DISABLE KEYS */;
/*!40000 ALTER TABLE `group_buy_order_list` ENABLE KEYS */;
UNLOCK TABLES;

DROP TABLE IF EXISTS `notify_task`;

CREATE TABLE `notify_task` (
  `id` int unsigned NOT NULL AUTO_INCREMENT COMMENT 'Auto ID',
  `activity_id` bigint NOT NULL COMMENT 'Activity ID',
  `team_id` varchar(8) NOT NULL COMMENT 'Team ID',
  `notify_category` varchar(64) DEFAULT NULL COMMENT 'Notify Category',
  `notify_type` varchar(8) NOT NULL DEFAULT 'HTTP' COMMENT 'Notify Type',
  `notify_mq` varchar(32) DEFAULT NULL COMMENT 'Notify MQ',
  `notify_url` varchar(128) DEFAULT NULL COMMENT 'Notify URL',
  `notify_count` int NOT NULL COMMENT 'Notify Count',
  `notify_status` tinyint(1) NOT NULL COMMENT 'Notify Status',
  `parameter_json` varchar(256) NOT NULL COMMENT 'Parameter Json',
  `uuid` varchar(128) NOT NULL COMMENT 'UUID',
  `create_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT 'Create Time',
  `update_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT 'Update Time',
  PRIMARY KEY (`id`),
  KEY `uq_uuid` (`uuid`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;

LOCK TABLES `notify_task` WRITE;
/*!40000 ALTER TABLE `notify_task` DISABLE KEYS */;
/*!40000 ALTER TABLE `notify_task` ENABLE KEYS */;
UNLOCK TABLES;

DROP TABLE IF EXISTS `sc_sku_activity`;

CREATE TABLE `sc_sku_activity` (
  `id` int unsigned NOT NULL AUTO_INCREMENT COMMENT 'Auto ID',
  `source` varchar(8) NOT NULL COMMENT 'Source',
  `channel` varchar(8) NOT NULL COMMENT 'Channel',
  `activity_id` bigint NOT NULL COMMENT 'Activity ID',
  `goods_id` varchar(16) NOT NULL COMMENT 'Goods ID',
  `create_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT 'Create Time',
  `update_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT 'Update Time',
  PRIMARY KEY (`id`),
  UNIQUE KEY `uq_sc_goodsid` (`source`,`channel`,`goods_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='SC sku activity';

LOCK TABLES `sc_sku_activity` WRITE;
/*!40000 ALTER TABLE `sc_sku_activity` DISABLE KEYS */;

INSERT INTO `sc_sku_activity` (`source`, `channel`, `activity_id`, `goods_id`) VALUES
('s01', 'c01', 9890001, '9890001'),
('s01', 'c01', 9890002, '9890002'),
('s01', 'c01', 9890002, '9890003'),
('s01', 'c01', 9890003, '9890004');

/*!40000 ALTER TABLE `sc_sku_activity` ENABLE KEYS */;
UNLOCK TABLES;

DROP TABLE IF EXISTS `sku_image`;

CREATE TABLE `sku_image` (
  `id` bigint unsigned NOT NULL AUTO_INCREMENT COMMENT 'Auto ID',
  `goods_id` varchar(16) NOT NULL COMMENT 'Goods ID',
  `image_url` varchar(512) NOT NULL COMMENT 'Public Image URL',
  `oss_object_key` varchar(512) NOT NULL COMMENT 'OSS Object Key',
  `sort_order` int NOT NULL COMMENT 'Display Order',
  `create_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT 'Create Time',
  `update_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT 'Update Time',
  PRIMARY KEY (`id`),
  KEY `idx_goods_sort` (`goods_id`,`sort_order`,`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='Sku Images';

LOCK TABLES `sku_image` WRITE;
/*!40000 ALTER TABLE `sku_image` DISABLE KEYS */;
/*!40000 ALTER TABLE `sku_image` ENABLE KEYS */;
UNLOCK TABLES;

DROP TABLE IF EXISTS `sku`;

CREATE TABLE `sku` (
  `id` int unsigned NOT NULL AUTO_INCREMENT COMMENT 'Auto ID',
  `source` varchar(8) NOT NULL COMMENT 'Source',
  `channel` varchar(8) NOT NULL COMMENT 'Channel',
  `goods_id` varchar(16) NOT NULL COMMENT 'Goods ID',
  `goods_name` varchar(128) NOT NULL COMMENT 'Goods Name',
  `original_price` decimal(10,2) NOT NULL COMMENT 'Original Price',
  `status` tinyint(1) NOT NULL DEFAULT '0' COMMENT 'Sku Status',
  `create_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT 'Create Time',
  `update_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT 'Update Time',
  PRIMARY KEY (`id`),
  UNIQUE KEY `uq_goods_id` (`goods_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='Sku';

LOCK TABLES `sku` WRITE;
/*!40000 ALTER TABLE `sku` DISABLE KEYS */;

INSERT INTO `sku` (`source`, `channel`, `goods_id`, `goods_name`, `original_price`, `status`) VALUES
('s01', 'c01', '9890001', '手写MyBatis：渐进式源码实践（全彩）', 89.90, 0),
('s01', 'c01', '9890002', '重学Java设计模式（精装版）', 129.00, 0),
('s01', 'c01', '9890003', 'DDD领域驱动设计实战', 109.00, 0),
('s01', 'c01', '9890004', '大模型应用开发极简入门', 59.90, 0);

/*!40000 ALTER TABLE `sku` ENABLE KEYS */;
UNLOCK TABLES;

/*!40111 SET SQL_NOTES=@OLD_SQL_NOTES */;
/*!40101 SET SQL_MODE=@OLD_SQL_MODE */;
/*!40014 SET FOREIGN_KEY_CHECKS=@OLD_FOREIGN_KEY_CHECKS */;
/*!40101 SET CHARACTER_SET_CLIENT=@OLD_CHARACTER_SET_CLIENT */;
/*!40101 SET CHARACTER_SET_RESULTS=@OLD_CHARACTER_SET_RESULTS */;
/*!40101 SET COLLATION_CONNECTION=@OLD_COLLATION_CONNECTION */;
