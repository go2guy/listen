use listen2;

ALTER TABLE organization ADD COLUMN `ext_length` int(11) NOT NULL AFTER `outbound_callid_by_did`;

commit work;
