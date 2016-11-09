use listen2;

alter table call_history modify column date_time datetime NULL;
update call_history set date_time=NULL where date_time='0000-00-00 00:00:00';

commit;