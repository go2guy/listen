use listen2;

alter table acd_call_history change column call_start agent_call_start datetime;
alter table acd_call_history change column call_end agent_call_end datetime;

commit work;
