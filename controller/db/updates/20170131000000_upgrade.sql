use listen2;

create index `call_history_session_indx` on call_history (session_id);

alter table call_history add column outbound_ani varchar(255) default null after ani;
alter table call_history add column inbound_dnis varchar(255) default null after dnis;
alter table call_history add column common_call_id varchar(36) default null after session_id;
alter table acd_call add column common_call_id varchar(36) default null after session_id;
alter table acd_call_history add column common_call_id varchar(36) default null after session_id;
alter table acd_call add column init_time datetime default null after common_call_id;

commit work;
