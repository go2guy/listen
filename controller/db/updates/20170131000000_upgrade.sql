use listen2;

create index `call_history_session_indx` on call_history (session_id);

alter table call_history add column outbound_ani varchar(255) default null after ani;
alter table call_history add column inbound_dnis varchar(255) default null after dnis;
alter table call_history add column common_call_id varchar(36) default null after session_id;
alter table acd_call add column common_call_id varchar(36) default null after session_id;
alter table acd_call_history add column common_call_id varchar(36) default null after session_id;
alter table acd_call add column init_time datetime default null after common_call_id;

CREATE TABLE `pbx_conference` (
  `id` bigint(20) NOT NULL auto_increment,
  `version` bigint(20) NOT NULL,
  `name` varchar(255) NOT NULL,
  `ani` varchar(255) NOT NULL,
  `dnis` varchar(255) NOT NULL,
  `monitoringSession` varchar(255),
  `monitoredExtension` varchar(255),
  PRIMARY KEY  (`id`)
) ENGINE=InnoDB AUTO_INCREMENT=6 DEFAULT CHARSET=utf8;

ALTER TABLE acd_call MODIFY COLUMN enqueue_time DATETIME NULL;
ALTER TABLE acd_call_history MODIFY COLUMN enqueue_time DATETIME NULL;
ALTER TABLE acd_call_history MODIFY COLUMN dequeue_time DATETIME NULL;

commit work;
