use listen2;

ALTER TABLE sip_phone ADD COLUMN `user_agent` varchar(50) DEFAULT NULL AFTER `cseq`;

alter table organization add column ad_server varchar(100);
alter table organization add column ad_domain varchar(100);
alter table organization add column ldap_basedn varchar(100);
alter table organization add column ldap_port varchar(100);
alter table organization add column ldap_dc varchar(100);

commit work;
