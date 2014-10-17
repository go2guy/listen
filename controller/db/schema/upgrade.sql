use listen2;

ALTER TABLE organization ADD COLUMN `ext_length` int(11) NOT NULL AFTER `outbound_callid_by_did`;
ALTER TABLE scheduled_conference MODIFY active_caller_addresses LONGTEXT NULL;


commit work;
