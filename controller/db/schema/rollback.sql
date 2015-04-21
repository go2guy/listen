use listen2;

/*
	Removing Constraints for acd_user_status, mine as well leave it the same
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
	Removing Constraints for action_history, mine as well leave it the same
*/	
ALTER TABLE action_history DROP FOREIGN KEY by_user_id;
ALTER TABLE action_history DROP FOREIGN KEY on_user_id;

/*
	Removing Constraints for call_history, mine as well leave it the same
*/	
ALTER TABLE call_history DROP FOREIGN KEY from_user_id;
ALTER TABLE call_history DROP FOREIGN KEY to_user_id;

/*
	Remove Constraints for inbox_message, mine as well leave it the same
*/	
ALTER TABLE inbox_message DROP FOREIGN KEY forwarded_by_id;
ALTER TABLE inbox_message DROP FOREIGN KEY left_by_id;

/*
	Remove ext_length to organization
*/
ALTER TABLE organization DROP COLUMN ext_length;

/*
	Removing Column ip in phone_number
*/	
ALTER TABLE phone_number DROP COLUMN ip;
ALTER TABLE phone_number ADD COLUMN ip varchar(50) DEFAULT NULL;
ALTER TABLE phone_number ADD UNIQUE (ip);

/*
	Adding on Cascades must drop foreign key to begin with, mine as well keep it the same
*/
ALTER TABLE prompt_override DROP FOREIGN KEY use_menu_id;
ALTER TABLE prompt_override
	ADD CONSTRAINT `FK90874447DD97B406`
	FOREIGN KEY (`use_menu_id`)
	REFERENCES `menu_group` (`id`);

/*
	Adding on Cascades must drop foreign key to begin with, mine as well keep it the same
*/
ALTER TABLE recording DROP FOREIGN KEY audio_id;
ALTER TABLE recording
	ADD CONSTRAINT `recording_audio_id_fk`
	FOREIGN KEY (`audio_id`)
	REFERENCES `audio` (`id`);

/*
	Modify Columns of scheduled_conference, mine as well keep it the same
*/
ALTER TABLE scheduled_conference MODIFY COLUMN active_caller_addresses longtext NOT NULL;
ALTER TABLE scheduled_conference MODIFY COLUMN passive_caller_addresses longtext NOT NULL;

DROP TABLE IF EXISTS `sip_phone`;

commit work;
