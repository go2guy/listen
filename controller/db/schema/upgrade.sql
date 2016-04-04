use listen2;

SET foreign_key_checks = 0;

alter table organization add column ad_server varchar(100);
alter table organization add column ad_domain varchar(100);
alter table organization add column ldap_basedn varchar(100);
alter table organization add column ldap_port varchar(100);
alter table organization add column ldap_dc varchar(100);

alter table sip_phone add column `phone_user_id` varchar(50) not null;
alter table sip_phone add key `unique-phone-user-id` (`phone_user_id`);

SET foreign_key_checks = 1;
commit work;
