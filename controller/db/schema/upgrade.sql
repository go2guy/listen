use listen2;

SET foreign_key_checks = 0;

alter table organization add column ad_server varchar(100);
alter table organization add column ad_domain varchar(100);
alter table organization add column ldap_basedn varchar(100);
alter table organization add column ldap_port varchar(100);
alter table organization add column ldap_dc varchar(100);

SET foreign_key_checks = 1;
commit work;
