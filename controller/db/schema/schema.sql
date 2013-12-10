drop database if exists listen2;
create database if not exists listen2;
use listen2;

DROP TABLE IF EXISTS `action`;
CREATE TABLE `action` (
  `id` bigint(20) NOT NULL auto_increment,
  `version` bigint(20) NOT NULL,
  `keys_pressed` varchar(255) default NULL,
  `prompt_before` varchar(255) NOT NULL,
  `class` varchar(255) NOT NULL,
  `destination_menu_group_name` varchar(255) default NULL,
  `destination_menu_name` varchar(255) default NULL,
  `number` varchar(255) default NULL,
  `application_name` varchar(255) default NULL,
  PRIMARY KEY  (`id`)
) ENGINE=InnoDB AUTO_INCREMENT=39 DEFAULT CHARSET=latin1;


DROP TABLE IF EXISTS `organization`;
CREATE TABLE `organization` (
  `id` bigint(20) NOT NULL auto_increment,
  `version` bigint(20) NOT NULL,
  `name` varchar(100) NOT NULL,
  `context_path` varchar(50) NOT NULL,
  `enabled` bit(1) NOT NULL default b'1',
  PRIMARY KEY  (`id`),
  UNIQUE KEY `name` (`name`),
  UNIQUE KEY `name_unique_1307548966223` (`name`),
  UNIQUE KEY `context_path` (`context_path`),
  UNIQUE KEY `context_path_unique_1309965401489` (`context_path`)
) ENGINE=InnoDB AUTO_INCREMENT=2 DEFAULT CHARSET=latin1;


DROP TABLE IF EXISTS `status`;
CREATE TABLE `status` (
  `id` bigint(20) NOT NULL AUTO_INCREMENT,
  `version` bigint(20) NOT NULL,
  `name` varchar(32) NOT NULL UNIQUE,
  `description` varchar(255) NOT NULL,
  PRIMARY KEY (`id`)
) ENGINE=InnoDB AUTO_INCREMENT=14 DEFAULT CHARSET=latin1;


DROP TABLE IF EXISTS `user`;
CREATE TABLE `user` (
  `id` bigint(20) NOT NULL auto_increment,
  `version` bigint(20) NOT NULL,
  `account_expired` bit(1) NOT NULL,
  `account_locked` bit(1) NOT NULL,
  `email_address` varchar(100) NOT NULL,
  `enabled` bit(1) NOT NULL,
  `last_login` datetime default NULL,
  `organization_id` bigint(20) default NULL,
  `password` varchar(255) NOT NULL,
  `password_expired` bit(1) NOT NULL,
  `real_name` varchar(50) NOT NULL,
  `username` varchar(50) NOT NULL,
  `is_active_directory` bit(1) NOT NULL,
   status_id bigint (20) DEFAULT 1,
  PRIMARY KEY  (`id`),
  KEY `FK36EBCB56D05B56` (`organization_id`),
  KEY `unique-username` (`organization_id`,`username`),
  FOREIGN KEY (status_id) REFERENCES status (id),
  CONSTRAINT `user_organizati_fk` FOREIGN KEY (`organization_id`) REFERENCES `organization` (`id`)
) ENGINE=InnoDB AUTO_INCREMENT=7 DEFAULT CHARSET=latin1;


DROP TABLE IF EXISTS `action_history`;
CREATE TABLE `action_history` (
  `id` bigint(20) NOT NULL auto_increment,
  `version` bigint(20) NOT NULL,
  `action` varchar(255) NOT NULL,
  `by_user_id` bigint(20) default NULL,
  `channel` varchar(255) NOT NULL,
  `date_created` datetime NOT NULL,
  `description` varchar(255) NOT NULL,
  `on_user_id` bigint(20) default NULL,
  `organization_id` bigint(20) default NULL,
  PRIMARY KEY  (`id`),
  KEY `FKC510738B26425C6E` (`by_user_id`),
  KEY `FKC510738B2F0C55F6` (`on_user_id`),
  KEY `FKC510738B56D05B56` (`organization_id`),
  CONSTRAINT `action_history_by_user_id_fk` FOREIGN KEY (`by_user_id`) REFERENCES `user` (`id`),
  CONSTRAINT `action_history_on_user_id_fk` FOREIGN KEY (`on_user_id`) REFERENCES `user` (`id`),
  CONSTRAINT `action_history_organizati_fk` FOREIGN KEY (`organization_id`) REFERENCES `organization` (`id`)
) ENGINE=InnoDB AUTO_INCREMENT=39 DEFAULT CHARSET=latin1;


DROP TABLE IF EXISTS `audio`;
CREATE TABLE `audio` (
  `id` bigint(20) NOT NULL auto_increment,
  `version` bigint(20) NOT NULL,
  `date_created` datetime NOT NULL,
  `description` varchar(200) default NULL,
  `duration` varchar(255) NOT NULL,
  `last_updated` datetime NOT NULL,
  `transcription` longtext NOT NULL,
  `file` varchar(1000) default NULL,
  PRIMARY KEY  (`id`)
) ENGINE=InnoDB AUTO_INCREMENT=2 DEFAULT CHARSET=latin1;


DROP TABLE IF EXISTS `phone_number`;
CREATE TABLE `phone_number` (
  `id` bigint(20) NOT NULL auto_increment,
  `version` bigint(20) NOT NULL,
  `forwarded_to` varchar(50) default NULL,
  `greeting_id` bigint(20) default NULL,
  `is_public` bit(1) default NULL,
  `number` varchar(50) NOT NULL,
  `owner_id` bigint(20) NOT NULL,
  `class` varchar(255) NOT NULL,
  `ip` varchar(50) default NULL,
  `sms_domain` varchar(50) default NULL,
  PRIMARY KEY  (`id`),
  UNIQUE KEY `ip` (`ip`),
  KEY `FKDB80433A12722FBB` (`greeting_id`),
  KEY `FKDB80433A6D23A06E` (`owner_id`),
  CONSTRAINT `phone_number_greeting_i_fk` FOREIGN KEY (`greeting_id`) REFERENCES `audio` (`id`),
  CONSTRAINT `phone_number_owner_id_fk` FOREIGN KEY (`owner_id`) REFERENCES `user` (`id`)
) ENGINE=InnoDB AUTO_INCREMENT=6 DEFAULT CHARSET=latin1;


DROP TABLE IF EXISTS `after_hours_configuration`;
CREATE TABLE `after_hours_configuration` (
  `id` bigint(20) NOT NULL auto_increment,
  `version` bigint(20) NOT NULL,
  `alternate_number` varchar(255) NOT NULL,
  `organization_id` bigint(20) NOT NULL,
  `realize_alert_name` varchar(255) NOT NULL,
  `realize_url` varchar(255) NOT NULL,
  `mobile_phone_id` bigint(20) default NULL,
  PRIMARY KEY  (`id`),
  KEY `FK7C7614E356D05B56` (`organization_id`),
  KEY `FK7C7614E359C40479` (`mobile_phone_id`),
  CONSTRAINT `after_hours_conf_organiza_fk` FOREIGN KEY (`organization_id`) REFERENCES `organization` (`id`),
  CONSTRAINT `FK7C7614E359C40479` FOREIGN KEY (`mobile_phone_id`) REFERENCES `phone_number` (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=latin1;



DROP TABLE IF EXISTS `call_data`;
CREATE TABLE `call_data` (
  `id` bigint(20) NOT NULL auto_increment,
  `version` bigint(20) NOT NULL,
  `ani` varchar(255) NOT NULL,
  `dnis` varchar(255) NOT NULL,
  `ended` datetime default NULL,
  `session_id` varchar(255) NOT NULL,
  `started` datetime NOT NULL,
  PRIMARY KEY  (`id`),
  UNIQUE KEY `session_id` (`session_id`),
  UNIQUE KEY `session_id_unique_1311604373502` (`session_id`)
) ENGINE=InnoDB AUTO_INCREMENT=17 DEFAULT CHARSET=latin1;


DROP TABLE IF EXISTS `call_history`;
CREATE TABLE `call_history` (
  `id` bigint(20) NOT NULL auto_increment,
  `version` bigint(20) NOT NULL,
  `ani` varchar(255) NOT NULL,
  `date_time` datetime NOT NULL,
  `dnis` varchar(255) NOT NULL,
  `duration` varchar(255) NOT NULL,
  `from_user_id` bigint(20) default NULL,
  `organization_id` bigint(20) NOT NULL,
  `to_user_id` bigint(20) default NULL,
  `result` varchar(255) NOT NULL,
  PRIMARY KEY  (`id`),
  KEY `FKC404C7B356D05B56` (`organization_id`),
  KEY `FKC404C7B388BFFA92` (`to_user_id`),
  KEY `FKC404C7B3F96764C1` (`from_user_id`),
  CONSTRAINT `call_history_from_user__fk` FOREIGN KEY (`from_user_id`) REFERENCES `user` (`id`),
  CONSTRAINT `call_history_organizati_fk` FOREIGN KEY (`organization_id`) REFERENCES `organization` (`id`),
  CONSTRAINT `call_history_to_user_id_fk` FOREIGN KEY (`to_user_id`) REFERENCES `user` (`id`)
) ENGINE=InnoDB AUTO_INCREMENT=7 DEFAULT CHARSET=latin1;


DROP TABLE IF EXISTS `conference`;
CREATE TABLE `conference` (
  `id` bigint(20) NOT NULL auto_increment,
  `version` bigint(20) NOT NULL,
  `arcade_id` varchar(255) default NULL,
  `description` varchar(100) NOT NULL,
  `is_recording` bit(1) NOT NULL,
  `is_started` bit(1) NOT NULL,
  `owner_id` bigint(20) NOT NULL,
  `recording_session_id` varchar(255) default NULL,
  `start_time` datetime default NULL,
  PRIMARY KEY  (`id`),
  KEY `FK2B5F451C6D23A06E` (`owner_id`),
  CONSTRAINT `conference_owner_id_fk` FOREIGN KEY (`owner_id`) REFERENCES `user` (`id`)
) ENGINE=InnoDB AUTO_INCREMENT=6 DEFAULT CHARSET=latin1;


DROP TABLE IF EXISTS `conferencing_configuration`;
CREATE TABLE `conferencing_configuration` (
  `id` bigint(20) NOT NULL auto_increment,
  `version` bigint(20) NOT NULL,
  `organization_id` bigint(20) NOT NULL,
  `pin_length` int(11) NOT NULL,
  PRIMARY KEY  (`id`),
  KEY `FKC3E7621056D05B56` (`organization_id`),
  CONSTRAINT `FKC3E7621056D05B56` FOREIGN KEY (`organization_id`) REFERENCES `organization` (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=latin1;


DROP TABLE IF EXISTS `device_registration`;
CREATE TABLE `device_registration` (
  `id` bigint(20) NOT NULL auto_increment,
  `version` bigint(20) NOT NULL,
  `device_id` varchar(255) NOT NULL,
  `device_type` varchar(255) NOT NULL,
  `registration_token` varchar(255) NOT NULL,
  `user_id` bigint(20) NOT NULL,
  PRIMARY KEY  (`id`),
  KEY `FKA5BAB00213CF056` (`user_id`),
  CONSTRAINT `FKA5BAB00213CF056` FOREIGN KEY (`user_id`) REFERENCES `user` (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=latin1;


DROP TABLE IF EXISTS `device_registration_registered_types`;
CREATE TABLE `device_registration_registered_types` (
  id bigint(20) not null primary key auto_increment,
  `device_registration_id` bigint(20) NOT NULL,
  `registered_type` varchar(255) default NULL,
  KEY `FK4606E719490FB402` (`device_registration_id`),
  CONSTRAINT `FK4606E719490FB402` FOREIGN KEY (`device_registration_id`) REFERENCES `device_registration` (`id`),
  index (registered_type)
) ENGINE=InnoDB DEFAULT CHARSET=latin1;


DROP TABLE IF EXISTS `find_me_number`;
CREATE TABLE `find_me_number` (
  `id` bigint(20) NOT NULL auto_increment,
  `version` bigint(20) NOT NULL,
  `dial_duration` int(11) NOT NULL,
  `is_enabled` bit(1) NOT NULL,
  `number` varchar(50) NOT NULL,
  `priority` int(11) NOT NULL,
  `user_id` bigint(20) NOT NULL,
  PRIMARY KEY  (`id`),
  KEY `FK71CAACCA13CF056` (`user_id`),
  CONSTRAINT `find_me_number_user_id_fk` FOREIGN KEY (`user_id`) REFERENCES `user` (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=latin1;


DROP TABLE IF EXISTS `find_me_preferences`;
CREATE TABLE `find_me_preferences` (
  `id` bigint(20) NOT NULL auto_increment,
  `version` bigint(20) NOT NULL,
  `expires` datetime default NULL,
  `reminder_number` varchar(255) default NULL,
  `send_reminder` bit(1) NOT NULL,
  `user_id` bigint(20) NOT NULL,
  PRIMARY KEY  (`id`),
  KEY `FK46C4D61713CF056` (`user_id`),
  CONSTRAINT `find_me_preferenc_user_id_fk` FOREIGN KEY (`user_id`) REFERENCES `user` (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=latin1;


DROP TABLE IF EXISTS `global_outdial_restriction`;
CREATE TABLE `global_outdial_restriction` (
  `id` bigint(20) NOT NULL auto_increment,
  `version` bigint(20) NOT NULL,
  `pattern` varchar(50) NOT NULL,
  PRIMARY KEY  (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=latin1;


DROP TABLE IF EXISTS `google_auth_configuration`;
CREATE TABLE `google_auth_configuration` (
  `id` bigint(20) NOT NULL auto_increment,
  `version` bigint(20) NOT NULL,
  `auth_pass` varchar(255) NOT NULL,
  `auth_token` longtext NOT NULL,
  `auth_user` varchar(255) NOT NULL,
  `is_enabled` bit(1) NOT NULL,
  `last_error` varchar(255) default NULL,
  `next_retry` datetime default NULL,
  `retry_timeout` bigint(20) NOT NULL,
  PRIMARY KEY  (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=latin1;


DROP TABLE IF EXISTS `inbox_message`;
CREATE TABLE `inbox_message` (
  `id` bigint(20) NOT NULL auto_increment,
  `version` bigint(20) NOT NULL,
  `ani` varchar(255) default NULL,
  `audio_id` bigint(20) default NULL,
  `date_created` datetime NOT NULL,
  `forwarded_by_id` bigint(20) default NULL,
  `is_new` bit(1) NOT NULL,
  `left_by_id` bigint(20) default NULL,
  `owner_id` bigint(20) NOT NULL,
  `class` varchar(255) NOT NULL,
  `file` varchar(1000) default NULL,
  `pages` int(11) default NULL,
  PRIMARY KEY  (`id`),
  KEY `FKC34DFDE96D23A06E` (`owner_id`),
  KEY `FKC34DFDE96F5092FE` (`audio_id`),
  KEY `FKC34DFDE99BC21612` (`left_by_id`),
  KEY `FKC34DFDE9FC1CAE6F` (`forwarded_by_id`),
  CONSTRAINT `voicemail_audio_id_fk` FOREIGN KEY (`audio_id`) REFERENCES `audio` (`id`),
  CONSTRAINT `voicemail_forwarded__fk` FOREIGN KEY (`forwarded_by_id`) REFERENCES `user` (`id`),
  CONSTRAINT `voicemail_left_by_id_fk` FOREIGN KEY (`left_by_id`) REFERENCES `user` (`id`),
  CONSTRAINT `voicemail_owner_id_fk` FOREIGN KEY (`owner_id`) REFERENCES `user` (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=latin1;


DROP TABLE IF EXISTS `mail_configuration`;
CREATE TABLE `mail_configuration` (
  `id` bigint(20) NOT NULL auto_increment,
  `version` bigint(20) NOT NULL,
  `default_from` varchar(255) default NULL,
  `host` varchar(255) NOT NULL,
  `password` varchar(255) default NULL,
  `username` varchar(255) default NULL,
  PRIMARY KEY  (`id`)
) ENGINE=InnoDB AUTO_INCREMENT=2 DEFAULT CHARSET=latin1;


DROP TABLE IF EXISTS `menu_group`;
CREATE TABLE `menu_group` (
  `id` bigint(20) NOT NULL auto_increment,
  `version` bigint(20) NOT NULL,
  `is_default` bit(1) NOT NULL,
  `name` varchar(30) NOT NULL,
  `organization_id` bigint(20) NOT NULL,
  PRIMARY KEY  (`id`),
  KEY `FKFA3CAB9F56D05B56` (`organization_id`),
  CONSTRAINT `menu_group_organization_fk` FOREIGN KEY (`organization_id`) REFERENCES `organization` (`id`)
) ENGINE=InnoDB AUTO_INCREMENT=3 DEFAULT CHARSET=latin1;


DROP TABLE IF EXISTS `menu`;
CREATE TABLE `menu` (
  `id` bigint(20) NOT NULL auto_increment,
  `version` bigint(20) NOT NULL,
  `default_action_id` bigint(20) NOT NULL,
  `is_entry` bit(1) NOT NULL,
  `menu_group_id` bigint(20) NOT NULL,
  `name` varchar(50) NOT NULL,
  `options_prompt` varchar(255) NOT NULL,
  `timeout_action_id` bigint(20) NOT NULL,
  PRIMARY KEY  (`id`),
  KEY `FK33155F2A28807E` (`menu_group_id`),
  KEY `FK33155F414E9077` (`timeout_action_id`),
  KEY `FK33155F5C58157` (`default_action_id`),
  KEY `unique-name` (`menu_group_id`,`name`),
  CONSTRAINT `menu_default_ac_fk` FOREIGN KEY (`default_action_id`) REFERENCES `action` (`id`),
  CONSTRAINT `menu_menu_group_fk` FOREIGN KEY (`menu_group_id`) REFERENCES `menu_group` (`id`),
  CONSTRAINT `menu_timeout_ac_fk` FOREIGN KEY (`timeout_action_id`) REFERENCES `action` (`id`)
) ENGINE=InnoDB AUTO_INCREMENT=13 DEFAULT CHARSET=latin1;


DROP TABLE IF EXISTS `menu_action`;
CREATE TABLE `menu_action` (
  id bigint(20) not null primary key auto_increment,
  `menu_keypress_actions_id` bigint(20) default NULL,
  `action_id` bigint(20) default NULL,
  KEY `FK424A6D162E667B4` (`menu_keypress_actions_id`),
  KEY `FK424A6D16C2B2CDD5` (`action_id`),
  CONSTRAINT `menu_action_action_id_fk` FOREIGN KEY (`action_id`) REFERENCES `action` (`id`),
  CONSTRAINT `menu_action_menu_keypress_fk` FOREIGN KEY (`menu_keypress_actions_id`) REFERENCES `menu` (`id`),
  index (action_id)
) ENGINE=InnoDB DEFAULT CHARSET=latin1;


DROP TABLE IF EXISTS `time_restriction`;
CREATE TABLE `time_restriction` (
  `id` bigint(20) NOT NULL auto_increment,
  `version` bigint(20) NOT NULL,
  `end_time` varchar(255) NOT NULL,
  `friday` bit(1) NOT NULL,
  `monday` bit(1) NOT NULL,
  `saturday` bit(1) NOT NULL,
  `start_time` varchar(255) NOT NULL,
  `sunday` bit(1) NOT NULL,
  `thursday` bit(1) NOT NULL,
  `tuesday` bit(1) NOT NULL,
  `wednesday` bit(1) NOT NULL,
  PRIMARY KEY  (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=latin1;


DROP TABLE IF EXISTS `menu_group_time_restriction`;
CREATE TABLE `menu_group_time_restriction` (
  id bigint(20) not null primary key auto_increment,
  `menu_group_restrictions_id` bigint(20) default NULL,
  `time_restriction_id` bigint(20) default NULL,
  KEY `FK910B81FA382B3C56` (`menu_group_restrictions_id`),
  KEY `FK910B81FA6B8BA9DE` (`time_restriction_id`),
  CONSTRAINT `menu_group_time_menu_grou_fk` FOREIGN KEY (`menu_group_restrictions_id`) REFERENCES `menu_group` (`id`),
  CONSTRAINT `menu_group_time_time_rest_fk` FOREIGN KEY (`time_restriction_id`) REFERENCES `time_restriction` (`id`),
  index (time_restriction_id)
) ENGINE=InnoDB DEFAULT CHARSET=latin1;


DROP TABLE IF EXISTS `number_route`;
CREATE TABLE `number_route` (
  `id` bigint(20) NOT NULL auto_increment,
  `version` bigint(20) NOT NULL,
  `destination` varchar(100) NOT NULL,
  `label` varchar(50) default NULL,
  `organization_id` bigint(20) NOT NULL,
  `pattern` varchar(50) NOT NULL,
  `type` varchar(255) NOT NULL,
  PRIMARY KEY  (`id`),
  KEY `FK534695D356D05B56` (`organization_id`),
  KEY `unique-label` (`type`,`organization_id`,`label`),
  KEY `unique-pattern` (`type`,`organization_id`,`pattern`),
  CONSTRAINT `number_route_organizati_fk` FOREIGN KEY (`organization_id`) REFERENCES `organization` (`id`)
) ENGINE=InnoDB AUTO_INCREMENT=2 DEFAULT CHARSET=latin1;


DROP TABLE IF EXISTS `organization_enabled_features`;
CREATE TABLE `organization_enabled_features` (
  id bigint(20) not null primary key auto_increment,
  `organization_id` bigint(20) NOT NULL,
  `listen_feature` varchar(255) default NULL,
  KEY `FK8CA0C50756D05B56` (`organization_id`),
  CONSTRAINT `organization_en_organizat_fk` FOREIGN KEY (`organization_id`) REFERENCES `organization` (`id`),
  index (listen_feature)
) ENGINE=InnoDB DEFAULT CHARSET=latin1;


DROP TABLE IF EXISTS `outdial_restriction`;
CREATE TABLE `outdial_restriction` (
  `id` bigint(20) NOT NULL auto_increment,
  `version` bigint(20) NOT NULL,
  `organization_id` bigint(20) NOT NULL,
  `pattern` varchar(50) NOT NULL,
  `target_id` bigint(20) default NULL,
  PRIMARY KEY  (`id`),
  KEY `FK3AAF28CB56D05B56` (`organization_id`),
  KEY `FK3AAF28CBD9654CD0` (`target_id`),
  CONSTRAINT `outdial_restrict_organizati_fk` FOREIGN KEY (`organization_id`) REFERENCES `organization` (`id`),
  CONSTRAINT `outdial_restrict_target_id_fk` FOREIGN KEY (`target_id`) REFERENCES `user` (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=latin1;


DROP TABLE IF EXISTS `outdial_restriction_exception`;
CREATE TABLE `outdial_restriction_exception` (
  `id` bigint(20) NOT NULL auto_increment,
  `version` bigint(20) NOT NULL,
  `restriction_id` bigint(20) NOT NULL,
  `target_id` bigint(20) NOT NULL,
  PRIMARY KEY  (`id`),
  KEY `FKB8C7B1DBAF1F7D18` (`restriction_id`),
  KEY `FKB8C7B1DBD9654CD0` (`target_id`),
  CONSTRAINT `outdial_restric2_restrictio_fk` FOREIGN KEY (`restriction_id`) REFERENCES `outdial_restriction` (`id`),
  CONSTRAINT `outdial_restric2_target_id_fk` FOREIGN KEY (`target_id`) REFERENCES `user` (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=latin1;


DROP TABLE IF EXISTS `outgoing_fax`;
CREATE TABLE `outgoing_fax` (
  `id` bigint(20) NOT NULL auto_increment,
  `version` bigint(20) NOT NULL,
  `date_created` datetime NOT NULL,
  `date_sent` datetime default NULL,
  `dnis` varchar(255) NOT NULL,
  `pages` int(11) NOT NULL,
  `sender_id` bigint(20) NOT NULL,
  `attempts` int(11) NOT NULL,
  `status` varchar(255) NOT NULL,
  PRIMARY KEY  (`id`),
  KEY `FK39FA57CABED31AC` (`sender_id`),
  CONSTRAINT `FK39FA57CABED31AC` FOREIGN KEY (`sender_id`) REFERENCES `user` (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=latin1;


DROP TABLE IF EXISTS `user_file`;
CREATE TABLE `user_file` (
  `id` bigint(20) NOT NULL auto_increment,
  `version` bigint(20) NOT NULL,
  `detected_type` varchar(255) NOT NULL,
  `file` varchar(1000) default NULL,
  `owner_id` bigint(20) NOT NULL,
  PRIMARY KEY  (`id`),
  KEY `FK143669706D23A06E` (`owner_id`),
  CONSTRAINT `FK143669706D23A06E` FOREIGN KEY (`owner_id`) REFERENCES `user` (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=latin1;


DROP TABLE IF EXISTS `outgoing_fax_user_file`;
CREATE TABLE `outgoing_fax_user_file` (
  id bigint(20) not null primary key auto_increment,
  `outgoing_fax_source_files_id` bigint(20) default NULL,
  `user_file_id` bigint(20) default NULL,
  `source_files_idx` int(11) default NULL,
  KEY `FK7A26073B813442AD` (`user_file_id`),
  KEY `FK7A26073B74452195` (`outgoing_fax_source_files_id`),
  CONSTRAINT `FK7A26073B813442AD` FOREIGN KEY (`user_file_id`) REFERENCES `user_file` (`id`),
  CONSTRAINT `outgo_fax_user_outgo_fax_sour_id_fk` FOREIGN KEY (`outgoing_fax_source_files_id`) REFERENCES `outgoing_fax` (`id`),
  index (user_file_id)
) ENGINE=InnoDB DEFAULT CHARSET=latin1;


DROP TABLE IF EXISTS `participant`;
CREATE TABLE `participant` (
  `id` bigint(20) NOT NULL auto_increment,
  `version` bigint(20) NOT NULL,
  `ani` varchar(50) NOT NULL,
  `conference_id` bigint(20) NOT NULL,
  `date_created` datetime NOT NULL,
  `is_admin` bit(1) NOT NULL,
  `is_admin_muted` bit(1) NOT NULL,
  `is_muted` bit(1) NOT NULL,
  `is_passive` bit(1) NOT NULL,
  `recorded_name_id` bigint(20) NOT NULL,
  `session_id` varchar(255) NOT NULL,
  `user_id` bigint(20) default NULL,
  PRIMARY KEY  (`id`),
  KEY `FK2DBDEF3313CF056` (`user_id`),
  KEY `FK2DBDEF337B2692B3` (`conference_id`),
  KEY `FK2DBDEF33B498DB9A` (`recorded_name_id`),
  KEY `participant_ani_uk` (`conference_id`,`ani`),
  CONSTRAINT `participant_conference_fk` FOREIGN KEY (`conference_id`) REFERENCES `conference` (`id`),
  CONSTRAINT `participant_recorded_name_fk` FOREIGN KEY (`recorded_name_id`) REFERENCES `audio` (`id`),
  CONSTRAINT `participant_user_id_fk` FOREIGN KEY (`user_id`) REFERENCES `user` (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=latin1;


DROP TABLE IF EXISTS `pin`;
CREATE TABLE `pin` (
  `id` bigint(20) NOT NULL auto_increment,
  `version` bigint(20) NOT NULL,
  `conference_id` bigint(20) NOT NULL,
  `number` varchar(20) NOT NULL,
  `pin_type` varchar(255) NOT NULL,
  PRIMARY KEY  (`id`),
  KEY `FK1B1957B2692B3` (`conference_id`),
  CONSTRAINT `pin_conference_fk` FOREIGN KEY (`conference_id`) REFERENCES `conference` (`id`)
) ENGINE=InnoDB AUTO_INCREMENT=16 DEFAULT CHARSET=latin1;


DROP TABLE IF EXISTS `prompt_override`;
CREATE TABLE `prompt_override` (
  `id` bigint(20) NOT NULL auto_increment,
  `version` bigint(20) NOT NULL,
  `date` date NOT NULL,
  `options_prompt` varchar(255) NOT NULL,
  `overrides_menu_id` bigint(20) default NULL,
  `use_menu_id` bigint(20) NOT NULL,
  PRIMARY KEY  (`id`),
  KEY `FK908744472A28807E` (`overrides_menu_id`),
  KEY `unique-date` (`overrides_menu_id`,`date`),
  KEY `FK90874447DD97B406` (`use_menu_id`),
  CONSTRAINT `FK90874447138FACA6` FOREIGN KEY (`overrides_menu_id`) REFERENCES `menu_group` (`id`),
  CONSTRAINT `FK90874447DD97B406` FOREIGN KEY (`use_menu_id`) REFERENCES `menu_group` (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=latin1;


DROP TABLE IF EXISTS `recording`;
CREATE TABLE `recording` (
  `id` bigint(20) NOT NULL auto_increment,
  `version` bigint(20) NOT NULL,
  `audio_id` bigint(20) NOT NULL,
  `conference_id` bigint(20) NOT NULL,
  PRIMARY KEY  (`id`),
  KEY `FK3B387DF16F5092FE` (`audio_id`),
  KEY `FK3B387DF17B2692B3` (`conference_id`),
  CONSTRAINT `recording_audio_id_fk` FOREIGN KEY (`audio_id`) REFERENCES `audio` (`id`),
  CONSTRAINT `recording_conference_id_fk` FOREIGN KEY (`conference_id`) REFERENCES `conference` (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=latin1;


DROP TABLE IF EXISTS `role`;
CREATE TABLE `role` (
  `id` bigint(20) NOT NULL auto_increment,
  `version` bigint(20) NOT NULL,
  `authority` varchar(255) NOT NULL,
  PRIMARY KEY  (`id`),
  UNIQUE KEY `authority` (`authority`),
  UNIQUE KEY `authority_unique_1307548966258` (`authority`)
) ENGINE=InnoDB AUTO_INCREMENT=8 DEFAULT CHARSET=latin1;


DROP TABLE IF EXISTS `scheduled_conference`;
CREATE TABLE `scheduled_conference` (
  `id` bigint(20) NOT NULL auto_increment,
  `version` bigint(20) NOT NULL,
  `active_caller_addresses` longtext NOT NULL,
  `date` date NOT NULL,
  `date_created` datetime NOT NULL,
  `email_body` longtext NOT NULL,
  `email_subject` varchar(255) NOT NULL,
  `ends` varchar(255) NOT NULL,
  `for_conference_id` bigint(20) NOT NULL,
  `passive_caller_addresses` longtext NOT NULL,
  `scheduled_by_id` bigint(20) NOT NULL,
  `starts` varchar(255) NOT NULL,
  `uid` varchar(255) default NULL,
  `sequence` int(11) NOT NULL,
  PRIMARY KEY  (`id`),
  KEY `FK44069BEE42175DBD` (`for_conference_id`),
  KEY `FK44069BEE57EDA238` (`scheduled_by_id`),
  CONSTRAINT `scheduled_conf_for_confer_fk` FOREIGN KEY (`for_conference_id`) REFERENCES `conference` (`id`),
  CONSTRAINT `scheduled_conf_scheduled_b_fk` FOREIGN KEY (`scheduled_by_id`) REFERENCES `user` (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=latin1;


DROP TABLE IF EXISTS `single_organization_configuration`;
CREATE TABLE `single_organization_configuration` (
  `id` bigint(20) NOT NULL auto_increment,
  `version` bigint(20) NOT NULL,
  `organization_id` bigint(20) NOT NULL,
  PRIMARY KEY  (`id`),
  KEY `FKA64DE54156D05B56` (`organization_id`),
  CONSTRAINT `FKA64DE54156D05B56` FOREIGN KEY (`organization_id`) REFERENCES `organization` (`id`)
) ENGINE=InnoDB AUTO_INCREMENT=2 DEFAULT CHARSET=latin1;


DROP TABLE IF EXISTS `spot_system`;
CREATE TABLE `spot_system` (
  `id` bigint(20) NOT NULL auto_increment,
  `version` bigint(20) NOT NULL,
  `name` varchar(255) NOT NULL,
  PRIMARY KEY  (`id`)
) ENGINE=InnoDB AUTO_INCREMENT=2 DEFAULT CHARSET=latin1;



DROP TABLE IF EXISTS `transcription_configuration`;
CREATE TABLE `transcription_configuration` (
  `id` bigint(20) NOT NULL auto_increment,
  `version` bigint(20) NOT NULL,
  `is_enabled` bit(1) NOT NULL,
  `organization_id` bigint(20) NOT NULL,
  `phone_number` varchar(255) NOT NULL,
  PRIMARY KEY  (`id`),
  UNIQUE KEY `organization_id` (`organization_id`),
  UNIQUE KEY `organization_id_unique_1307548966277` (`organization_id`),
  KEY `FKF351DC0956D05B56` (`organization_id`),
  CONSTRAINT `transcription_c_organizat_fk` FOREIGN KEY (`organization_id`) REFERENCES `organization` (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=latin1;




DROP TABLE IF EXISTS `user_role`;
CREATE TABLE `user_role` (
  `role_id` bigint(20) NOT NULL,
  `user_id` bigint(20) NOT NULL,
  PRIMARY KEY  (`role_id`,`user_id`),
  KEY `FK143BF46A13CF056` (`user_id`),
  KEY `FK143BF46A5C122C76` (`role_id`),
  CONSTRAINT `user_role_role_id_fk` FOREIGN KEY (`role_id`) REFERENCES `role` (`id`),
  CONSTRAINT `user_role_user_id_fk` FOREIGN KEY (`user_id`) REFERENCES `user` (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=latin1;


DROP TABLE IF EXISTS `voicemail_preferences`;
CREATE TABLE `voicemail_preferences` (
  `id` bigint(20) NOT NULL auto_increment,
  `version` bigint(20) NOT NULL,
  `email_notification_address` varchar(255) default NULL,
  `is_email_notification_enabled` bit(1) NOT NULL,
  `is_sms_notification_enabled` bit(1) NOT NULL,
  `passcode` varchar(20) NOT NULL,
  `playback_order` varchar(255) NOT NULL,
  `recurring_notifications_enabled` bit(1) NOT NULL,
  `sms_notification_address` varchar(255) default NULL,
  `transcribe` bit(1) NOT NULL,
  `user_id` bigint(20) NOT NULL,
  PRIMARY KEY  (`id`),
  KEY `FK1C02A5A213CF056` (`user_id`),
  CONSTRAINT `voicemail_prefer_user_id_fk` FOREIGN KEY (`user_id`) REFERENCES `user` (`id`)
) ENGINE=InnoDB AUTO_INCREMENT=6 DEFAULT CHARSET=latin1;


DROP TABLE IF EXISTS `voicemail_preferences_time_restriction`;
CREATE TABLE `voicemail_preferences_time_restriction` (
  id bigint(20) not null primary key auto_increment,
  `voicemail_preferences_email_time_restrictions_id` bigint(20) default NULL,
  `time_restriction_id` bigint(20) default NULL,
  `voicemail_preferences_sms_time_restrictions_id` bigint(20) default NULL,
  KEY `FK566E6E576B285DF1` (`voicemail_preferences_email_time_restrictions_id`),
  KEY `FK566E6E576B8BA9DE` (`time_restriction_id`),
  KEY `FK566E6E5776CE5F94` (`voicemail_preferences_sms_time_restrictions_id`),
  CONSTRAINT `voicemail_prefe2_email_time_fk` FOREIGN KEY (`voicemail_preferences_email_time_restrictions_id`) REFERENCES `voicemail_preferences` (`id`),
  CONSTRAINT `voicemail_prefe2_sms_time_r_fk` FOREIGN KEY (`voicemail_preferences_sms_time_restrictions_id`) REFERENCES `voicemail_preferences` (`id`),
  CONSTRAINT `voicemail_prefe2_time_restr_fk` FOREIGN KEY (`time_restriction_id`) REFERENCES `time_restriction` (`id`),
  index (voicemail_preferences_sms_time_restrictions_id)
) ENGINE=InnoDB DEFAULT CHARSET=latin1;

DROP TABLE IF EXISTS `skill`;
CREATE TABLE `skill` (
  `id` bigint(20) NOT NULL AUTO_INCREMENT,
  `version` bigint(20) NOT NULL,
  `skillname` varchar(32) NOT NULL,
  `organization_id` bigint(20) NOT NULL,
  `description` varchar(255) NOT NULL,
  PRIMARY KEY (`id`),
  UNIQUE KEY `skillname` (`skillname`,`organization_id`),
  KEY `FK7C7614E356D05B56` (`organization_id`),
  CONSTRAINT `skill_organiza_fk` FOREIGN KEY (`organization_id`) REFERENCES `organization` (`id`)
) ENGINE=InnoDB AUTO_INCREMENT=14 DEFAULT CHARSET=latin1;

DROP TABLE IF EXISTS `user_skill`;
CREATE TABLE `user_skill` (
  `id` bigint(20) NOT NULL AUTO_INCREMENT,
  `version` bigint(20) NOT NULL,
  `user_id` bigint(20) NOT NULL,
  `skill_id` bigint(20) NOT NULL,
  PRIMARY KEY (`id`),
  UNIQUE KEY `user_id` (`user_id`,`skill_id`),
  KEY `FK7C7614E356D05D43` (`user_id`),
  KEY `FK7C7614E356D05D53` (`skill_id`),
  CONSTRAINT `user_skill_skill_fk` FOREIGN KEY (`skill_id`) REFERENCES `skill` (`id`),
  CONSTRAINT `user_skill_user_fk` FOREIGN KEY (`user_id`) REFERENCES `user` (`id`)
)