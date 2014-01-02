use listen2;


LOCK TABLES `action` WRITE;
INSERT INTO `action` VALUES (34,0,NULL,'','com.interact.listen.attendant.action.ReplayMenuAction',NULL,NULL,NULL,NULL,NULL),(35,0,NULL,'','com.interact.listen.attendant.action.ReplayMenuAction',NULL,NULL,NULL,NULL,NULL),(36,0,'*','','com.interact.listen.attendant.action.LaunchApplicationAction',NULL,NULL,NULL,NULL,'Mailbox'),(37,0,'???','thankYou1Moment.wav','com.interact.listen.attendant.action.DialPressedNumberAction',NULL,NULL,NULL,NULL,NULL),(38,0,'1','sorryInvalidNumber.wav','com.interact.listen.attendant.action.ReplayMenuAction',NULL,NULL,NULL,NULL,NULL);
UNLOCK TABLES;


LOCK TABLES `organization` WRITE;
INSERT INTO `organization` VALUES (1,0,'Mimio','mimio','');
UNLOCK TABLES;


LOCK TABLES `user` WRITE;
INSERT INTO `user` (id, version, account_expired, account_locked, email_address, enabled, last_login, organization_id,
    password, password_expired, real_name, username, is_active_directory)
  VALUES (1,5,'\0','\0','iisupport@newnet.com',1,'2013-11-05 15:23:38',NULL,
    '$2a$10$026QVF/Z7eE3w.6MRN/C3.bhmjMOgUK2ozRwtP6fHkBZmov/9vdEy','\0','Listen Custodian','Custodian','\0');
INSERT INTO `user` (id, version, account_expired, account_locked, email_address, enabled, last_login, organization_id,
                    password, password_expired, real_name, username, is_active_directory)
  VALUES (2,5,'\0','\0','iisupport@newnet.com',1,'2013-11-05 15:23:38',NULL,
          '$2a$10$026QVF/Z7eE3w.6MRN/C3.bhmjMOgUK2ozRwtP6fHkBZmov/9vdEy','\0','ACD Voicemail','acdvoicemail','\0');
UNLOCK TABLES;

LOCK TABLES `acd_user_status` WRITE;
INSERT INTO `acd_user_status` (id, owner_id, version, acd_queue_status, contact_number_id, status_modified, onacall,
                               onacall_modified) VALUES (1,1,0,'Available',NULL,NULL,0,NULL);
INSERT INTO `acd_user_status` (id, owner_id, version, acd_queue_status, contact_number_id, status_modified, onacall,
                               onacall_modified) VALUES (2,2,0,'VoicemailBox',NULL,NULL,0,NULL);
UNLOCK TABLES;


LOCK TABLES `mail_configuration` WRITE;
INSERT INTO `mail_configuration` VALUES (1,0,'listen@newnet.com','pod51018.outlook.com','Summer2013!','listen@newnet.com');
UNLOCK TABLES;


LOCK TABLES `menu_group` WRITE;
INSERT INTO `menu_group` VALUES (1,16,'','Mimio In Office',1);
UNLOCK TABLES;


LOCK TABLES `menu` WRITE;
INSERT INTO `menu` VALUES (12,0,34,'',1,'In Office Attendant','newestinofficewelcome.wav',35);
UNLOCK TABLES;


LOCK TABLES `menu_action` WRITE;
INSERT INTO `menu_action` VALUES (1,12,36),(2,12,37),(3,12,38);
UNLOCK TABLES;

LOCK TABLES `role` WRITE;
INSERT INTO `role` VALUES (1,0,'ROLE_CUSTODIAN'),(2,0,'ROLE_ATTENDANT_ADMIN'),(3,0,'ROLE_ORGANIZATION_ADMIN'),(4,0,'ROLE_CONFERENCE_USER'),(5,0,'ROLE_FAX_USER'),(6,0,'ROLE_FINDME_USER'),(7,0,'ROLE_VOICEMAIL_USER'),(8,0,'ROLE_ACD_USER');
UNLOCK TABLES;


LOCK TABLES `single_organization_configuration` WRITE;
INSERT INTO `single_organization_configuration` VALUES (1,0,1);
UNLOCK TABLES;


LOCK TABLES `spot_system` WRITE;
INSERT INTO `spot_system` VALUES (1,0,'http://localhost/spot');
UNLOCK TABLES;


LOCK TABLES `user_role` WRITE;
INSERT INTO `user_role` VALUES (1,1);
UNLOCK TABLES;
