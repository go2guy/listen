use listen2;

create index `call_history_session_indx` on call_history (session_id);

commit work;
