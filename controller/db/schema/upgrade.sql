use listen2;

ALTER TABLE organization ADD COLUMN `outbound_callid` VARCHAR(50) NOT NULL AFTER `enabled`;
ALTER TABLE organization ADD COLUMN `outbound_callid_by_did` BIT(1) NOT NULL AFTER `outbound_callId`;

commit work;
