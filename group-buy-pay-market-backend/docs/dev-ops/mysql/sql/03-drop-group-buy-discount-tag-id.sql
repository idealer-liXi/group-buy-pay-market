-- Discount no longer owns crowd tag restrictions; activity owns tag binding and scope.
ALTER TABLE `group_buy_discount`
  DROP COLUMN `tag_id`;
