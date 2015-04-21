use listen2;

-- OLD upgrade.sql
-----------------------------------------------------------------------------------------
-- ALTER TABLE sip_phone ADD COLUMN `user_agent` varchar(50) DEFAULT NULL AFTER `cseq`;
-----------------------------------------------------------------------------------------


-- NEW upgrade.sql - v6 to v7
-----------------------------------------------------------------------------------------
/*
	Adding on Cascades must drop foreign key to begin with
*/
ALTER TABLE acd_user_status DROP FOREIGN KEY owner_id;
ALTER TABLE acd_user_status
	ADD CONSTRAINT `acd_user_status_ibfk_1`
	FOREIGN KEY (`owner_id`)
	REFERENCES `user` (`id`) ON DELETE CASCADE ON UPDATE CASCADE;

ALTER TABLE acd_user_status DROP FOREIGN KEY contact_number_id;
ALTER TABLE acd_user_status
	ADD CONSTRAINT `acd_user_status_ibfk_2`
	FOREIGN KEY (`contact_number_id`)
	REFERENCES `phone_number` (`id`) ON DELETE CASCADE;

	
/*
	Removing Constraints for action_history
*/	
ALTER TABLE action_history DROP FOREIGN KEY by_user_id;
ALTER TABLE action_history DROP FOREIGN KEY on_user_id;

/*
	Removing Constraints for call_history
*/	
ALTER TABLE call_history DROP FOREIGN KEY from_user_id;
ALTER TABLE call_history DROP FOREIGN KEY to_user_id;

/*
	Removing Constraints for inbox_message
*/	
ALTER TABLE inbox_message DROP FOREIGN KEY forwarded_by_id;
ALTER TABLE inbox_message DROP FOREIGN KEY left_by_id;

/*
	Add ext_length to organization
*/
ALTER TABLE organization ADD COLUMN ext_length int(11) NOT NULL;

/*
	Removing Column ip in phone_number
*/	
ALTER TABLE phone_number DROP COLUMN ip;

/*
	Adding on Cascades must drop foreign key to begin with
*/
ALTER TABLE prompt_override DROP FOREIGN KEY use_menu_id;
ALTER TABLE prompt_override
	ADD CONSTRAINT `FK90874447DD97B406`
	FOREIGN KEY (`use_menu_id`)
	REFERENCES `menu_group` (`id`) ON DELETE CASCADE ON UPDATE CASCADE;

/*
	Adding on Cascades must drop foreign key to begin with
*/
ALTER TABLE recording DROP FOREIGN KEY audio_id;
ALTER TABLE recording
	ADD CONSTRAINT `recording_audio_id_fk`
	FOREIGN KEY (`audio_id`)
	REFERENCES `audio` (`id`) ON DELETE CASCADE ON UPDATE CASCADE;

/*
	Modify Columns of scheduled_conference
*/
ALTER TABLE scheduled_conference MODIFY COLUMN active_caller_addresses longtext;
ALTER TABLE scheduled_conference MODIFY COLUMN passive_caller_addresses longtext;

DROP TABLE IF EXISTS `sip_phone`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `sip_phone` (
  `id` bigint(20) NOT NULL AUTO_INCREMENT,
  `version` bigint(20) NOT NULL,
  `registered` tinyint(1) DEFAULT NULL,
  `extension_id` bigint(20) NOT NULL,
  `organization_id` bigint(20) NOT NULL,
  `real_name` varchar(50) DEFAULT NULL,
  `username` varchar(50) NOT NULL,
  `password` varchar(255) NOT NULL,
  `ip` varchar(50) DEFAULT NULL,
  `cseq` bigint(20) DEFAULT NULL,
  `user_agent` varchar(50) DEFAULT NULL,
  `date_registered` datetime DEFAULT NULL,
  `date_expires` datetime DEFAULT NULL,
  PRIMARY KEY (`id`),
  KEY `FKC5294l23B3209252` (`extension_id`),
  KEY `FKC510738B56D05B56` (`organization_id`),
  KEY `unique-username` (`organization_id`,`username`),
  CONSTRAINT `sip_phone_extension_id_fk` FOREIGN KEY (`extension_id`) REFERENCES `phone_number` (`id`) ON DELETE CASCADE ON UPDATE CASCADE,
  CONSTRAINT `sip_phone_organization_id_fk` FOREIGN KEY (`organization_id`) REFERENCES `organization` (`id`)
) ENGINE=InnoDB AUTO_INCREMENT=50 DEFAULT CHARSET=utf8;

commit work;
