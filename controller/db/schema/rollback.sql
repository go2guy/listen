use listen2;

drop table provisioner_template_field_value;
drop table provisioner_template_field;
alter table sip_phone drop foreign key sip_phone_provisioner_template_fk;
alter table sip_phone drop column provisioner_template_id;
alter table sip_phone drop column provisioner_identifier;
alter table sip_phone drop column provisioner_last_updated;
drop table provisioner_template;

alter table organization drop column route;

alter table organization drop column post_cdr;
alter table organization drop column cdr_url;

alter table call_history drop column cdr_post_result;
alter table call_history drop column cdr_post_count;

commit;
