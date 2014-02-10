use listen2;

ALTER TABLE user_skill
ADD COLUMN priority INT(11) NOT NULL default 0;

DROP TABLE IF EXISTS `prompt_override`;
CREATE TABLE `prompt_override` (
  `id` bigint(20) NOT NULL auto_increment,
  `version` bigint(20) NOT NULL,
  `start_date` datetime NOT NULL,
  `end_date` datetime NOT NULL,
  `options_prompt` varchar(255) NOT NULL,
  `use_menu_id` bigint(20) NOT NULL,
  `event_type` varchar(25),
  PRIMARY KEY  (`id`),
  KEY `FK90874447DD97B406` (`use_menu_id`),
  CONSTRAINT `FK90874447DD97B406` FOREIGN KEY (`use_menu_id`) REFERENCES `menu_group` (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8;

commit work;
