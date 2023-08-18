# Host: 46.254.16.245  (Version 5.5.5-10.1.23-MariaDB-9+deb9u1)
# Date: 2021-12-31 00:16:53
# Generator: MySQL-Front 6.1  (Build 1.26)


#
# Structure for table "resources"
#

DROP TABLE IF EXISTS `resources`;
CREATE TABLE `resources` (
  `id` bigint(20) NOT NULL AUTO_INCREMENT,
  `bytes` mediumblob NOT NULL,
  `size` bigint(20) NOT NULL DEFAULT '0',
  `tag_s_1` varchar(200) NOT NULL DEFAULT '',
  `tag_s_2` varchar(200) DEFAULT NULL,
  `publication_id` bigint(20) NOT NULL DEFAULT '0',
  PRIMARY KEY (`id`),
  KEY `tag_s_1` (`tag_s_1`(191))
) ENGINE=InnoDB AUTO_INCREMENT=1302779 DEFAULT CHARSET=utf8mb4;
