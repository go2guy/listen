use listen2;

ALTER TABLE organization ADD COLUMN `ext_length` int(11) NOT NULL AFTER `outbound_callid_by_did`;
ALTER TABLE scheduled_conference MODIFY active_caller_addresses LONGTEXT NULL;

DROP TABLE IF EXISTS `sip_phone`;
CREATE TABLE `sip_phone` (
  `id` bigint(20) NOT NULL auto_increment,
  `version` bigint(20) NOT NULL,
  `registered` boolean default NULL,
  `extension_id` bigint(20) NOT NULL,
  `organization_id` bigint(20) NOT NULL,
  `real_name` varchar(50) DEFAULT NULL,
  `username` varchar(50) NOT NULL,
  `password` varchar(255) NOT NULL,
  `ip` varchar(50) default NULL,
  `cseq` bigint(20) default NULL,
  `date_registered` datetime default NULL,
  PRIMARY KEY  (`id`),
  KEY `FKC5294l23B3209252` (`extension_id`),
  KEY `FKC510738B56D05B56` (`organization_id`),
  KEY `unique-username` (`organization_id`,`username`),
  CONSTRAINT `sip_phone_extension_id_fk` FOREIGN KEY (`extension_id`) REFERENCES `phone_number` (`id`) ON DELETE CASCADE ON UPDATE CASCADE,
  CONSTRAINT `sip_phone_organization_id_fk` FOREIGN KEY (`organization_id`) REFERENCES `organization` (`id`)
) ENGINE=InnoDB AUTO_INCREMENT=39 DEFAULT CHARSET=utf8;

ALTER TABLE acd_user_status DROP FOREIGN KEY acd_user_status_ibfk_1;
ALTER TABLE acd_user_status ADD CONSTRAINT acd_user_status_ibfk_1 FOREIGN KEY (owner_id)  REFERENCES user (id) ON UPDATE CASCADE ON DELETE CASCADE;
ALTER TABLE action_history DROP FOREIGN KEY action_history_by_user_id_fk;
ALTER TABLE action_history DROP FOREIGN KEY action_history_on_user_id_fk;
ALTER TABLE call_history DROP FOREIGN KEY call_history_from_user__fk;
ALTER TABLE call_history DROP FOREIGN KEY call_history_to_user_id_fk;
ALTER TABLE inbox_message DROP FOREIGN KEY voicemail_forwarded__fk;
ALTER TABLE inbox_message DROP FOREIGN KEY voicemail_left_by_id_fk;
ALTER TABLE recording DROP FOREIGN KEY recording_audio_id_fk;
ALTER TABLE recording ADD CONSTRAINT recording_audio_id_fk FOREIGN KEY (audio_id)  REFERENCES audio (id) ON UPDATE CASCADE ON DELETE CASCADE;

commit work;
