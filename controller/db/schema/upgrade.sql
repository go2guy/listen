use listen2;

ALTER TABLE sip_phone ADD COLUMN `user_agent` varchar(50) DEFAULT NULL AFTER `cseq`;

commit work;
