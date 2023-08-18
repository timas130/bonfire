# Host: 46.254.16.245  (Version 5.5.5-10.1.23-MariaDB-9+deb9u1)
# Date: 2021-12-31 00:16:41
# Generator: MySQL-Front 6.1  (Build 1.26)


#
# Structure for table "accounts"
#

DROP TABLE IF EXISTS `accounts`;
CREATE TABLE `accounts` (
  `id` bigint(20) NOT NULL AUTO_INCREMENT,
  `google_id` varchar(30) NOT NULL DEFAULT '',
  `date_create` bigint(20) NOT NULL DEFAULT '0',
  `account_language_id` bigint(20) NOT NULL DEFAULT '0',
  `name` varchar(30) NOT NULL DEFAULT '',
  `img_id` bigint(20) NOT NULL DEFAULT '0',
  `ban_date` bigint(20) NOT NULL DEFAULT '0',
  `recruiter_id` bigint(20) DEFAULT '0',
  `lvl` bigint(20) NOT NULL DEFAULT '100',
  `karma_count` bigint(20) NOT NULL DEFAULT '0',
  `last_online_time` bigint(20) NOT NULL DEFAULT '0',
  `img_title_id` bigint(20) NOT NULL DEFAULT '0',
  `img_title_gif_id` bigint(20) NOT NULL DEFAULT '0',
  `subscribes` text NOT NULL,
  `refresh_token` text NOT NULL,
  `refresh_token_date_create` bigint(20) NOT NULL DEFAULT '0',
  `sex` bigint(20) NOT NULL DEFAULT '0',
  `reports_count` bigint(20) NOT NULL DEFAULT '0',
  `account_settings` text CHARACTER SET utf8mb4 NOT NULL,
  `karma_count_total` bigint(20) NOT NULL DEFAULT '0',
  PRIMARY KEY (`id`),
  KEY `google_id` (`google_id`),
  KEY `name` (`name`),
  KEY `last_online_time` (`last_online_time`),
  KEY `refresh_token` (`refresh_token`(10)),
  KEY `reports_count` (`reports_count`)
) ENGINE=InnoDB AUTO_INCREMENT=288846 DEFAULT CHARSET=utf8 ROW_FORMAT=COMPACT;

#
# Structure for table "accounts_achievements"
#

DROP TABLE IF EXISTS `accounts_achievements`;
CREATE TABLE `accounts_achievements` (
  `id` bigint(20) unsigned NOT NULL AUTO_INCREMENT,
  `account_id` bigint(20) NOT NULL,
  `achievement_index` bigint(20) NOT NULL DEFAULT '0',
  `achievement_lvl` bigint(20) NOT NULL DEFAULT '0',
  `karma_force` bigint(20) NOT NULL DEFAULT '0',
  PRIMARY KEY (`id`)
) ENGINE=InnoDB AUTO_INCREMENT=395380 DEFAULT CHARSET=utf8;

#
# Structure for table "accounts_effects"
#

DROP TABLE IF EXISTS `accounts_effects`;
CREATE TABLE `accounts_effects` (
  `id` bigint(20) NOT NULL AUTO_INCREMENT,
  `account_id` bigint(20) NOT NULL DEFAULT '0',
  `date_create` bigint(20) NOT NULL DEFAULT '0',
  `date_end` bigint(20) NOT NULL DEFAULT '0',
  `comment` text NOT NULL,
  `effect_index` bigint(20) NOT NULL DEFAULT '0',
  `effect_tag` bigint(20) NOT NULL DEFAULT '0',
  `from_account_name` varchar(200) NOT NULL DEFAULT '',
  `effect_comment_tag` bigint(20) NOT NULL DEFAULT '0',
  PRIMARY KEY (`id`)
) ENGINE=InnoDB AUTO_INCREMENT=4169 DEFAULT CHARSET=utf8mb4;

#
# Structure for table "accounts_emails"
#

DROP TABLE IF EXISTS `accounts_emails`;
CREATE TABLE `accounts_emails` (
  `id` bigint(20) NOT NULL AUTO_INCREMENT,
  `account_id` bigint(20) NOT NULL DEFAULT '0',
  `date_create` bigint(20) NOT NULL DEFAULT '0',
  `account_email` varchar(200) NOT NULL DEFAULT '',
  `account_password` text NOT NULL,
  PRIMARY KEY (`id`)
) ENGINE=InnoDB AUTO_INCREMENT=16801 DEFAULT CHARSET=utf8mb4;

#
# Structure for table "accounts_enters"
#

DROP TABLE IF EXISTS `accounts_enters`;
CREATE TABLE `accounts_enters` (
  `id` bigint(20) NOT NULL AUTO_INCREMENT,
  `account_id` bigint(20) NOT NULL DEFAULT '0',
  `date_create` bigint(20) NOT NULL DEFAULT '0',
  PRIMARY KEY (`id`)
) ENGINE=InnoDB AUTO_INCREMENT=1547361 DEFAULT CHARSET=utf8;

#
# Structure for table "accounts_notifications"
#

DROP TABLE IF EXISTS `accounts_notifications`;
CREATE TABLE `accounts_notifications` (
  `id` bigint(20) NOT NULL AUTO_INCREMENT,
  `date_create` bigint(20) NOT NULL DEFAULT '0',
  `account_id` bigint(20) NOT NULL DEFAULT '0',
  `notification_json` mediumtext CHARACTER SET utf8mb4 NOT NULL,
  `notification_type` bigint(20) NOT NULL DEFAULT '0',
  `notification_status` bigint(20) NOT NULL DEFAULT '0',
  PRIMARY KEY (`id`),
  KEY `notification_status` (`notification_status`),
  KEY `date_create` (`date_create`),
  KEY `account_id` (`account_id`)
) ENGINE=InnoDB AUTO_INCREMENT=6162468 DEFAULT CHARSET=utf8 ROW_FORMAT=COMPACT;

#
# Structure for table "activities"
#

DROP TABLE IF EXISTS `activities`;
CREATE TABLE `activities` (
  `id` bigint(11) NOT NULL AUTO_INCREMENT,
  `type` bigint(20) NOT NULL DEFAULT '0',
  `fandom_id` bigint(20) NOT NULL DEFAULT '0',
  `language_id` bigint(20) NOT NULL DEFAULT '0',
  `date_create` bigint(20) NOT NULL DEFAULT '0',
  `name` varchar(255) NOT NULL DEFAULT '',
  `image_id` bigint(20) NOT NULL DEFAULT '0',
  `background_id` bigint(20) NOT NULL DEFAULT '0',
  `creator_id` bigint(20) NOT NULL DEFAULT '0',
  `params` text NOT NULL,
  `tag_1` bigint(20) NOT NULL DEFAULT '0',
  `tag_2` bigint(20) NOT NULL DEFAULT '0',
  `tag_3` bigint(20) NOT NULL DEFAULT '0',
  `tag_s_1` varchar(255) NOT NULL DEFAULT '',
  `tag_s_2` varchar(255) NOT NULL DEFAULT '',
  `tag_s_3` varchar(255) NOT NULL DEFAULT '',
  `description` text NOT NULL,
  PRIMARY KEY (`id`),
  KEY `type` (`type`),
  KEY `fandom_id` (`fandom_id`),
  KEY `language_id` (`language_id`),
  KEY `date_create` (`date_create`),
  KEY `name` (`name`(191)),
  KEY `creator_id` (`creator_id`),
  KEY `tag_1` (`tag_1`),
  KEY `tag_2` (`tag_2`),
  KEY `tag_3` (`tag_3`),
  KEY `tag_s_1` (`tag_s_1`(191)),
  KEY `tag_s_2` (`tag_s_2`(191)),
  KEY `tag_s_3` (`tag_s_3`(191))
) ENGINE=InnoDB AUTO_INCREMENT=546 DEFAULT CHARSET=utf8mb4;

#
# Structure for table "activities_collisions"
#

DROP TABLE IF EXISTS `activities_collisions`;
CREATE TABLE `activities_collisions` (
  `id` bigint(20) NOT NULL AUTO_INCREMENT,
  `type` bigint(20) NOT NULL DEFAULT '0',
  `account_id` bigint(20) NOT NULL DEFAULT '0',
  `activity_id` bigint(20) NOT NULL DEFAULT '0',
  `date_create` bigint(20) NOT NULL DEFAULT '0',
  `tag_1` bigint(20) NOT NULL DEFAULT '0',
  `tag_2` bigint(20) NOT NULL DEFAULT '0',
  `tag_3` bigint(20) NOT NULL DEFAULT '0',
  `tag_s_1` varchar(255) NOT NULL DEFAULT '',
  `tag_s_2` varchar(255) NOT NULL DEFAULT '',
  `tag_s_3` varchar(255) NOT NULL DEFAULT '',
  PRIMARY KEY (`id`),
  KEY `type` (`type`),
  KEY `account_id` (`account_id`),
  KEY `activity_id` (`activity_id`),
  KEY `date_create` (`date_create`),
  KEY `tag_1` (`tag_1`),
  KEY `tag_2` (`tag_2`),
  KEY `tag_3` (`tag_3`)
) ENGINE=InnoDB AUTO_INCREMENT=98203 DEFAULT CHARSET=utf8mb4;

#
# Structure for table "chats"
#

DROP TABLE IF EXISTS `chats`;
CREATE TABLE `chats` (
  `id` bigint(20) NOT NULL AUTO_INCREMENT,
  `type` bigint(20) NOT NULL DEFAULT '0',
  `fandom_id` bigint(20) NOT NULL DEFAULT '0',
  `language_id` bigint(20) NOT NULL DEFAULT '0',
  `date_create` bigint(20) NOT NULL DEFAULT '0',
  `name` varchar(500) NOT NULL DEFAULT '',
  `image_id` bigint(20) NOT NULL DEFAULT '0',
  `creator_id` bigint(20) NOT NULL DEFAULT '0',
  `chat_params` varchar(1000) NOT NULL DEFAULT '',
  `background_id` bigint(20) NOT NULL DEFAULT '0',
  PRIMARY KEY (`id`),
  KEY `type` (`type`),
  KEY `date_create` (`date_create`),
  KEY `name` (`name`(191)),
  KEY `image_id` (`image_id`),
  KEY `creator_id` (`creator_id`),
  KEY `fandom_id` (`fandom_id`),
  KEY `language_id` (`language_id`)
) ENGINE=InnoDB AUTO_INCREMENT=2119 DEFAULT CHARSET=utf8mb4;

#
# Structure for table "chats_subscriptions"
#

DROP TABLE IF EXISTS `chats_subscriptions`;
CREATE TABLE `chats_subscriptions` (
  `id` bigint(20) NOT NULL AUTO_INCREMENT,
  `account_id` bigint(20) NOT NULL DEFAULT '0',
  `target_id` bigint(20) NOT NULL DEFAULT '0',
  `target_sub_id` bigint(20) DEFAULT '0',
  `chat_type` bigint(20) NOT NULL DEFAULT '0',
  `subscribed` bigint(20) NOT NULL DEFAULT '0',
  `read_date` bigint(20) NOT NULL DEFAULT '0',
  `last_message_id` bigint(20) NOT NULL DEFAULT '0',
  `last_message_date` bigint(20) NOT NULL DEFAULT '0',
  `enter_date` bigint(20) NOT NULL DEFAULT '0',
  `exit_date` bigint(20) NOT NULL DEFAULT '0',
  `member_status` bigint(20) NOT NULL DEFAULT '0',
  `member_level` bigint(20) NOT NULL DEFAULT '0',
  `member_owner` bigint(20) NOT NULL DEFAULT '0',
  `new_messages` bigint(20) NOT NULL DEFAULT '0',
  PRIMARY KEY (`id`),
  KEY `account_id` (`account_id`),
  KEY `target_id` (`target_id`),
  KEY `target_sub_id` (`target_sub_id`),
  KEY `chat_type` (`chat_type`),
  KEY `subscribed` (`subscribed`),
  KEY `read_date` (`read_date`),
  KEY `last_message_id` (`last_message_id`),
  KEY `last_message_date` (`last_message_date`),
  KEY `enter_date` (`enter_date`),
  KEY `member_status` (`member_status`),
  KEY `exit_date` (`exit_date`),
  KEY `member_level` (`member_level`),
  KEY `member_owner` (`member_owner`)
) ENGINE=InnoDB AUTO_INCREMENT=117968 DEFAULT CHARSET=utf8mb4;

#
# Structure for table "collisions"
#

DROP TABLE IF EXISTS `collisions`;
CREATE TABLE `collisions` (
  `id` bigint(20) NOT NULL AUTO_INCREMENT,
  `owner_id` bigint(20) NOT NULL DEFAULT '0',
  `collision_type` bigint(20) NOT NULL DEFAULT '0',
  `collision_id` bigint(20) NOT NULL DEFAULT '0',
  `collision_date_create` bigint(20) NOT NULL DEFAULT '0',
  `collision_sub_id` bigint(20) NOT NULL DEFAULT '0',
  `collision_key` varchar(255) NOT NULL DEFAULT '',
  `value_1` bigint(20) NOT NULL DEFAULT '0',
  `value_2` text CHARACTER SET utf8mb4,
  `value_3` bigint(20) NOT NULL DEFAULT '0',
  `value_4` bigint(20) NOT NULL DEFAULT '0',
  `value_5` bigint(20) NOT NULL DEFAULT '0',
  PRIMARY KEY (`id`),
  KEY `owner_id` (`owner_id`),
  KEY `collision_sub_id` (`collision_sub_id`),
  KEY `collision_id` (`collision_id`),
  KEY `collision_type` (`collision_type`),
  KEY `value_1` (`value_1`),
  KEY `value_2` (`value_2`(10)),
  KEY `collision_key` (`collision_key`),
  KEY `owner + collision` (`owner_id`,`collision_type`)
) ENGINE=InnoDB AUTO_INCREMENT=5807617 DEFAULT CHARSET=utf8;

#
# Structure for table "donate"
#

DROP TABLE IF EXISTS `donate`;
CREATE TABLE `donate` (
  `id` bigint(20) NOT NULL AUTO_INCREMENT,
  `account_id` bigint(20) NOT NULL DEFAULT '0',
  `sum` bigint(20) NOT NULL DEFAULT '0',
  `data` text NOT NULL,
  `donate_key` varchar(300) NOT NULL DEFAULT '',
  PRIMARY KEY (`id`),
  KEY `account_id` (`account_id`),
  KEY `key` (`donate_key`(191))
) ENGINE=InnoDB AUTO_INCREMENT=18 DEFAULT CHARSET=utf8mb4;

#
# Structure for table "fandoms"
#

DROP TABLE IF EXISTS `fandoms`;
CREATE TABLE `fandoms` (
  `id` bigint(11) NOT NULL AUTO_INCREMENT,
  `name` varchar(255) NOT NULL DEFAULT '',
  `creator_id` bigint(20) NOT NULL DEFAULT '0',
  `image_id` bigint(20) NOT NULL DEFAULT '0',
  `image_title_id` bigint(20) NOT NULL DEFAULT '0',
  `date_create` bigint(20) NOT NULL DEFAULT '0',
  `status` bigint(20) NOT NULL DEFAULT '0',
  `subscribers_count` bigint(20) NOT NULL DEFAULT '0',
  `fandom_category` bigint(20) NOT NULL DEFAULT '0',
  `fandom_closed` bigint(20) NOT NULL DEFAULT '0',
  `karma_cof` bigint(20) NOT NULL DEFAULT '100',
  PRIMARY KEY (`id`),
  KEY `name` (`name`),
  KEY `creator_id` (`creator_id`),
  KEY `image_id` (`image_id`),
  KEY `date_create` (`date_create`),
  KEY `status` (`status`),
  KEY `subscribers_count` (`subscribers_count`),
  KEY `fandom_category` (`fandom_category`),
  KEY `fandom_closed` (`fandom_closed`),
  KEY `karma_cof` (`karma_cof`)
) ENGINE=InnoDB AUTO_INCREMENT=6491 DEFAULT CHARSET=utf8;

#
# Structure for table "rubrics"
#

DROP TABLE IF EXISTS `rubrics`;
CREATE TABLE `rubrics` (
  `id` bigint(20) NOT NULL AUTO_INCREMENT,
  `name` varchar(500) NOT NULL DEFAULT '',
  `creator_id` bigint(20) NOT NULL DEFAULT '0',
  `karma_cof` bigint(20) NOT NULL DEFAULT '0',
  `fandom_id` bigint(20) NOT NULL DEFAULT '0',
  `language_id` bigint(20) NOT NULL DEFAULT '0',
  `date_create` bigint(20) NOT NULL DEFAULT '0',
  `owner_id` bigint(20) NOT NULL DEFAULT '0',
  `status` bigint(20) NOT NULL DEFAULT '0',
  `status_change_date` bigint(20) NOT NULL DEFAULT '0',
  PRIMARY KEY (`id`),
  KEY `creator_id` (`creator_id`),
  KEY `fandom_id` (`fandom_id`),
  KEY `language_id` (`language_id`),
  KEY `fandom_id_2` (`fandom_id`,`language_id`),
  KEY `owner_id` (`owner_id`),
  KEY `status` (`status`),
  KEY `status_change_date` (`status_change_date`)
) ENGINE=InnoDB AUTO_INCREMENT=461 DEFAULT CHARSET=utf8mb4;

#
# Structure for table "support"
#

DROP TABLE IF EXISTS `support`;
CREATE TABLE `support` (
  `id` bigint(20) NOT NULL AUTO_INCREMENT,
  `date` bigint(20) NOT NULL DEFAULT '0',
  `count` bigint(20) NOT NULL DEFAULT '0',
  `user_id` bigint(20) NOT NULL DEFAULT '0',
  `date_create` bigint(20) NOT NULL DEFAULT '0',
  `status` bigint(20) NOT NULL DEFAULT '0',
  `comment` varchar(500) NOT NULL DEFAULT '',
  `donate_info` text NOT NULL,
  PRIMARY KEY (`id`),
  KEY `date` (`date`),
  KEY `count` (`count`),
  KEY `user_id` (`user_id`),
  KEY `date_create` (`date_create`),
  KEY `status` (`status`)
) ENGINE=InnoDB AUTO_INCREMENT=992 DEFAULT CHARSET=utf8mb4;

#
# Structure for table "translates"
#

DROP TABLE IF EXISTS `translates`;
CREATE TABLE `translates` (
  `id` bigint(20) NOT NULL AUTO_INCREMENT,
  `languageId` bigint(20) NOT NULL DEFAULT '0',
  `translate_key` varchar(200) NOT NULL DEFAULT '',
  `text` text,
  `hint` varchar(500) NOT NULL DEFAULT '',
  `appKey` varchar(50) NOT NULL DEFAULT '',
  PRIMARY KEY (`id`),
  KEY `key` (`translate_key`(191)),
  KEY `appKey` (`appKey`),
  KEY `languageId` (`languageId`,`translate_key`(191))
) ENGINE=InnoDB AUTO_INCREMENT=5322 DEFAULT CHARSET=utf8mb4;

#
# Structure for table "translates_history"
#

DROP TABLE IF EXISTS `translates_history`;
CREATE TABLE `translates_history` (
  `id` bigint(20) NOT NULL AUTO_INCREMENT,
  `language_id` bigint(20) NOT NULL DEFAULT '0',
  `language_id_from` bigint(20) NOT NULL DEFAULT '0',
  `translate_key` varchar(200) NOT NULL DEFAULT '',
  `old_text` text,
  `new_text` text,
  `history_type` bigint(20) NOT NULL DEFAULT '0',
  `history_creator_id` bigint(20) NOT NULL DEFAULT '0',
  `date_history_created` bigint(20) NOT NULL DEFAULT '0',
  `project_key` varchar(200) NOT NULL DEFAULT '',
  `history_comment` text NOT NULL,
  `confirm_account_1` bigint(20) NOT NULL DEFAULT '0',
  `confirm_account_2` bigint(20) NOT NULL DEFAULT '0',
  `confirm_account_3` bigint(20) NOT NULL DEFAULT '0',
  PRIMARY KEY (`id`)
) ENGINE=InnoDB AUTO_INCREMENT=2341 DEFAULT CHARSET=utf8mb4;

#
# Structure for table "units"
#

DROP TABLE IF EXISTS `units`;
CREATE TABLE `units` (
  `id` bigint(20) NOT NULL AUTO_INCREMENT,
  `fandom_id` bigint(20) NOT NULL DEFAULT '0',
  `language_id` bigint(20) NOT NULL DEFAULT '0',
  `unit_type` bigint(20) NOT NULL DEFAULT '0',
  `unit_category` bigint(20) NOT NULL DEFAULT '0',
  `date_create` bigint(20) NOT NULL DEFAULT '0',
  `creator_id` bigint(20) NOT NULL DEFAULT '0',
  `unit_json` mediumtext CHARACTER SET utf8mb4 NOT NULL,
  `parent_unit_id` bigint(20) NOT NULL DEFAULT '0',
  `status` bigint(20) NOT NULL DEFAULT '0',
  `subunits_count` bigint(20) NOT NULL DEFAULT '0',
  `karma_count` bigint(20) NOT NULL DEFAULT '0',
  `important` bigint(20) NOT NULL DEFAULT '0',
  `parent_fandom_closed` int(11) NOT NULL DEFAULT '0',
  `closed` int(11) NOT NULL DEFAULT '0',
  `tag_1` bigint(20) NOT NULL DEFAULT '0',
  `tag_2` bigint(20) NOT NULL DEFAULT '0',
  `tag_3` bigint(20) NOT NULL DEFAULT '0',
  `tag_4` bigint(20) NOT NULL DEFAULT '0',
  `tag_5` bigint(20) NOT NULL DEFAULT '0',
  `tag_6` bigint(20) NOT NULL DEFAULT '0',
  `tag_7` bigint(20) NOT NULL DEFAULT '0',
  `tag_s_1` varchar(255) NOT NULL DEFAULT '',
  `tag_s_2` varchar(255) NOT NULL DEFAULT '',
  `unit_reports_count` bigint(20) NOT NULL DEFAULT '0',
  `fandom_key` varchar(200) NOT NULL DEFAULT '',
  PRIMARY KEY (`id`),
  KEY `date_create` (`date_create`),
  KEY `tag_1` (`tag_1`),
  KEY `tag_2` (`tag_2`),
  KEY `tag_3` (`tag_3`),
  KEY `tag_s_1` (`tag_s_1`),
  KEY `unit_type` (`unit_type`),
  KEY `status` (`status`),
  KEY `fandom_id` (`fandom_id`),
  KEY `language_id` (`language_id`),
  KEY `parent_unit_id` (`parent_unit_id`),
  KEY `tag_s_2` (`tag_s_2`),
  KEY `unit_type_2` (`unit_type`,`date_create`),
  KEY `tag_4` (`tag_4`),
  KEY `unit_reports_count` (`unit_reports_count`),
  KEY `parent_fandom_closed` (`parent_fandom_closed`),
  KEY `important` (`important`),
  KEY `karma_count` (`karma_count`),
  KEY `creator_id` (`creator_id`),
  KEY `closed` (`closed`),
  KEY `tag_6` (`tag_6`),
  KEY `tag_7` (`tag_7`),
  KEY `tag_1_2` (`tag_1`,`tag_2`,`unit_type`),
  KEY `fandom_key` (`fandom_key`)
) ENGINE=InnoDB AUTO_INCREMENT=4420672 DEFAULT CHARSET=utf8;

#
# Structure for table "units_history"
#

DROP TABLE IF EXISTS `units_history`;
CREATE TABLE `units_history` (
  `id` bigint(20) NOT NULL AUTO_INCREMENT,
  `unit_id` bigint(20) NOT NULL DEFAULT '0',
  `history_type` bigint(20) NOT NULL DEFAULT '0',
  `data` text NOT NULL,
  `date` bigint(20) NOT NULL DEFAULT '0',
  PRIMARY KEY (`id`),
  KEY `date` (`date`),
  KEY `unit_id` (`unit_id`),
  KEY `history_type` (`history_type`)
) ENGINE=InnoDB AUTO_INCREMENT=3809721 DEFAULT CHARSET=utf8mb4;

#
# Structure for table "units_karma_transactions"
#

DROP TABLE IF EXISTS `units_karma_transactions`;
CREATE TABLE `units_karma_transactions` (
  `id` bigint(20) unsigned NOT NULL AUTO_INCREMENT,
  `from_account_id` bigint(20) NOT NULL DEFAULT '0',
  `target_account_id` bigint(20) NOT NULL DEFAULT '0',
  `date_create` bigint(20) NOT NULL DEFAULT '0',
  `unit_id` bigint(20) NOT NULL DEFAULT '0',
  `karma_count` bigint(20) NOT NULL DEFAULT '0',
  `change_account_karma` bit(1) NOT NULL DEFAULT b'0',
  `fandom_id` bigint(20) NOT NULL DEFAULT '0',
  `language_id` bigint(20) NOT NULL DEFAULT '0',
  `karma_cof` bigint(20) NOT NULL DEFAULT '0',
  `anonymous` bigint(20) NOT NULL DEFAULT '0',
  PRIMARY KEY (`id`),
  KEY `date_create` (`date_create`),
  KEY `from_account_id` (`from_account_id`),
  KEY `target_account_id` (`target_account_id`),
  KEY `unit_id` (`unit_id`),
  KEY `change_account_karma` (`change_account_karma`),
  KEY `fandom_id` (`fandom_id`),
  KEY `language_id` (`language_id`),
  KEY `anonymous` (`anonymous`)
) ENGINE=InnoDB AUTO_INCREMENT=2639237 DEFAULT CHARSET=utf8;

#
# Structure for table "wiki_items"
#

DROP TABLE IF EXISTS `wiki_items`;
CREATE TABLE `wiki_items` (
  `id` bigint(20) NOT NULL AUTO_INCREMENT,
  `parent_item_id` bigint(20) NOT NULL DEFAULT '0',
  `fandom_id` bigint(20) NOT NULL DEFAULT '0',
  `language_id` bigint(20) NOT NULL DEFAULT '0',
  `date_create` bigint(20) NOT NULL DEFAULT '0',
  `creator_id` bigint(20) NOT NULL DEFAULT '0',
  `type` bigint(20) NOT NULL DEFAULT '0',
  `status` bigint(20) NOT NULL DEFAULT '0',
  PRIMARY KEY (`id`),
  KEY `fandom_id` (`fandom_id`),
  KEY `language_id` (`language_id`),
  KEY `date_create` (`date_create`),
  KEY `parent_item_id` (`parent_item_id`),
  KEY `type` (`type`)
) ENGINE=InnoDB AUTO_INCREMENT=2617 DEFAULT CHARSET=utf8mb4;

#
# Structure for table "wiki_pages"
#

DROP TABLE IF EXISTS `wiki_pages`;
CREATE TABLE `wiki_pages` (
  `id` bigint(20) NOT NULL AUTO_INCREMENT,
  `item_id` bigint(20) NOT NULL DEFAULT '0',
  `item_data` text NOT NULL,
  `date_create` bigint(20) NOT NULL DEFAULT '0',
  `creator_id` bigint(20) NOT NULL DEFAULT '0',
  `language_id` bigint(20) NOT NULL DEFAULT '0',
  `event_type` bigint(20) NOT NULL DEFAULT '0',
  `wiki_status` bigint(20) NOT NULL DEFAULT '0',
  PRIMARY KEY (`id`),
  KEY `item_id` (`item_id`),
  KEY `date_create` (`date_create`),
  KEY `creator_id` (`creator_id`),
  KEY `language_id` (`language_id`),
  KEY `status` (`wiki_status`)
) ENGINE=InnoDB AUTO_INCREMENT=8608 DEFAULT CHARSET=utf8mb4;

#
# Structure for table "wiki_titles"
#

DROP TABLE IF EXISTS `wiki_titles`;
CREATE TABLE `wiki_titles` (
  `id` bigint(20) NOT NULL AUTO_INCREMENT,
  `item_id` bigint(20) NOT NULL DEFAULT '0',
  `parent_item_id` bigint(20) NOT NULL DEFAULT '0',
  `fandom_id` bigint(20) NOT NULL DEFAULT '0',
  `item_data` text NOT NULL,
  `date_create` bigint(20) NOT NULL DEFAULT '0',
  `type` bigint(20) NOT NULL DEFAULT '0',
  `creator_id` bigint(20) NOT NULL DEFAULT '0',
  `wiki_status` bigint(20) NOT NULL DEFAULT '0',
  PRIMARY KEY (`id`),
  KEY `item_id` (`item_id`),
  KEY `date_create` (`date_create`),
  KEY `parent_item_id` (`parent_item_id`),
  KEY `status` (`wiki_status`)
) ENGINE=InnoDB AUTO_INCREMENT=2996 DEFAULT CHARSET=utf8mb4;

alter table wiki_titles add priority int not null default 0;
