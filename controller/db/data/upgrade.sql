use listen2;

LOCK TABLES `prompt_override` WRITE;
INSERT INTO prompt_override (version,options_prompt,use_menu_id,start_date,end_date,event_type)
  values (0,'closedCompanyEvent.wav',1,now() - interval 2 day, now() - interval 1 day, 'UNSCHEDULED_EVENT');
UNLOCK TABLES;

commit work;
