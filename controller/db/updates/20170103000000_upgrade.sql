use listen2;

SET foreign_key_checks = 0;

CREATE TABLE `provisioner_template` (
  `id` bigint(20) NOT NULL AUTO_INCREMENT,
  `name` varchar(255) NOT NULL,
  `template` longtext NOT NULL,
  `version` bigint(20) DEFAULT NULL,
  PRIMARY KEY (`id`)
) ENGINE=InnoDB AUTO_INCREMENT=6 DEFAULT CHARSET=utf8;

CREATE TABLE `provisioner_template_field` (
  `id` bigint(20) NOT NULL AUTO_INCREMENT,
  `name` varchar(255) NOT NULL,
  `default_value` varchar(255) DEFAULT NULL,
  `provisioner_template_id` bigint(20) NOT NULL,
  `version` bigint(20) DEFAULT NULL,
  PRIMARY KEY (`id`),
  UNIQUE KEY `name_template_id` (`provisioner_template_id`,`name`),
  CONSTRAINT `field_provisioner_template_fk` FOREIGN KEY (`provisioner_template_id`) REFERENCES `provisioner_template` (`id`)
) ENGINE=InnoDB AUTO_INCREMENT=24 DEFAULT CHARSET=utf8;

alter table `sip_phone` add column provisioner_identifier varchar(255) default NULL;
alter table `sip_phone` add column `provisioner_template_id` bigint(20) default NULL, add CONSTRAINT `sip_phone_provisioner_template_fk` FOREIGN KEY (`provisioner_template_id`) REFERENCES `provisioner_template` (`id`);
alter table sip_phone add column provisioner_last_updated datetime NULL;

CREATE TABLE `provisioner_template_field_value` (
  `id` bigint(20) NOT NULL AUTO_INCREMENT,
  `field_value` varchar(255) DEFAULT NULL,
  `sip_phone_id` bigint(20) NOT NULL,
  `provisioner_template_field_id` bigint(20) NOT NULL,
  `version` bigint(20) DEFAULT 0,
  PRIMARY KEY (`id`),
  CONSTRAINT `field_value_provisioner_template_fk` FOREIGN KEY (`provisioner_template_field_id`) REFERENCES `provisioner_template_field` (`id`),
  CONSTRAINT `sip_phone_field_value_fk` FOREIGN KEY (`sip_phone_id`) REFERENCES `sip_phone` (`id`)
) ENGINE=InnoDB AUTO_INCREMENT=11 DEFAULT CHARSET=utf8;

SET foreign_key_checks = 1;

-- 
-- Update Organization for route
--
alter table organization add column route varchar(100) default null;

--
-- Alter call history table for IMG work.  Merging of acd and call_history
--
ALTER TABLE call_history MODIFY COLUMN `date_time` datetime NOT NULL AFTER `version`;
ALTER TABLE call_history MODIFY COLUMN `organization_id` bigint(20) NOT NULL AFTER `duration`;
ALTER TABLE call_history ADD COLUMN `session_id` varchar(255) NOT NULL AFTER `to_user_id`;
ALTER TABLE call_history ADD COLUMN `ivr` varchar(255) NOT NULL AFTER `session_id`;
ALTER TABLE call_history ADD COLUMN `last_modified` datetime DEFAULT NULL AFTER  `result`;

--
-- We need to make session_id unique prior to adding a unique key upon that column
--
UPDATE call_history AS U1, call_history AS U2 SET U1.session_id=CONCAT_WS('#',U2.id,U2.date_time) WHERE U2.id=U1.id;
ALTER TABLE call_history ADD UNIQUE KEY `session_unique` (`session_id`);

commit work;
