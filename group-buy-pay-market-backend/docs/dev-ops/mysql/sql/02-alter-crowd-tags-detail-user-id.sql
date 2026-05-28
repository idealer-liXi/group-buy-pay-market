use `group_buy_pay_market`;

ALTER TABLE `crowd_tags_detail`
  MODIFY COLUMN `user_id` varchar(64) NOT NULL COMMENT 'User ID';
