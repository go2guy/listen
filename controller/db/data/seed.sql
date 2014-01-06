use listen2;

/* This appears to reference an audio file that is not deployed by default */
LOCK TABLES `audio` WRITE;
INSERT INTO `audio` VALUES (1,0,'2013-11-05 15:54:59',NULL,'PT0S','2013-11-05 15:54:59','','file:/interact/listen/artifacts/2/voicemail/greeting/357-greeting.wav');
UNLOCK TABLES;


LOCK TABLES `user` WRITE;
INSERT INTO `user` (id, version, account_expired, account_locked, email_address, enabled, last_login, organization_id,
                    password, password_expired, real_name, username, is_active_directory)
      VALUES
        (2,4,'\0','\0','operator@newnet.com',1,'2013-11-05 16:08:21',1,
          '$2a$10$026QVF/Z7eE3w.6MRN/C3.bhmjMOgUK2ozRwtP6fHkBZmov/9vdEy','\0','Operator','operator','\0'),
        (3,3,'\0','\0','scott@newnet.com',1,'2013-11-05 16:10:10',1,
          '$2a$10$026QVF/Z7eE3w.6MRN/C3.bhmjMOgUK2ozRwtP6fHkBZmov/9vdEy','\0','Scott Farwell','scott','\0'),
        (4,3,'\0','\0','ricardo@newnet.com',1,'2013-11-18 15:10:49',1,
          '$2a$10$026QVF/Z7eE3w.6MRN/C3.bhmjMOgUK2ozRwtP6fHkBZmov/9vdEy','\0','Ricardo Mastroleo','ricardo','\0'),
        (5,2,'\0','\0','ladi@newnet.com',1,'2013-11-05 16:18:37',1,
          '$2a$10$026QVF/Z7eE3w.6MRN/C3.bhmjMOgUK2ozRwtP6fHkBZmov/9vdEy','\0','Ladi Akinyemi','ladi','\0'),
        (6,0,'\0','\0','testlisten@newnet.com',1,NULL,1,
          '03ac674216f3e15c761ee1a5e255f067953623c8b388b4459e13f978d7c846f4','\0','test Listen','testlisten','\0'),
        (7,12,'\0','\0','daniel@newnet.com',1,'2013-11-26 17:15:17',1,
          '$2a$10$026QVF/Z7eE3w.6MRN/C3.bhmjMOgUK2ozRwtP6fHkBZmov/9vdEy','\0','Daniel Walter','dwalter','\0'),
        (8,0,'\0','\0','iisupport@newnet.com',1,'2013-11-05 15:23:38',1,
          '$2a$10$026QVF/Z7eE3w.6MRN/C3.bhmjMOgUK2ozRwtP6fHkBZmov/9vdEy','\0','ACD Voicemail','acdvoicemail','\0'),
        (9,5,'\0','\0','iisupport@newnet.com',1,'2013-11-05 15:23:38',1,
          '$2a$10$026QVF/Z7eE3w.6MRN/C3.bhmjMOgUK2ozRwtP6fHkBZmov/9vdEy','\0','ACD Tech Voicemail','acdtechvoicemail','\0'),
        (10,5,'\0','\0','iisupport@newnet.com',1,'2013-11-05 15:23:38',1,
          '$2a$10$026QVF/Z7eE3w.6MRN/C3.bhmjMOgUK2ozRwtP6fHkBZmov/9vdEy','\0','ACD Finance Voicemail','acdfinancevoicemail','\0'),
        (11,5,'\0','\0','iisupport@newnet.com',1,'2013-11-05 15:23:38',1,
          '$2a$10$026QVF/Z7eE3w.6MRN/C3.bhmjMOgUK2ozRwtP6fHkBZmov/9vdEy','\0','ACD Human Resources Voicemail','acdhumanresourcevoicemail','\0');
UNLOCK TABLES;


LOCK TABLES `phone_number` WRITE;
INSERT INTO `phone_number` VALUES (1,1,NULL,1,NULL,'357',2,'com.interact.listen.pbx.Extension',NULL,NULL),(2,0,NULL,NULL,NULL,'371',3,'com.interact.listen.pbx.Extension',NULL,NULL),(3,0,NULL,NULL,NULL,'112',4,'com.interact.listen.pbx.Extension',NULL,NULL),(4,0,NULL,NULL,NULL,'116',5,'com.interact.listen.pbx.Extension',NULL,NULL),(5,1,NULL,NULL,NULL,'338',6,'com.interact.listen.pbx.Extension',NULL,NULL),(6,1,NULL,NULL,NULL,'333',7,'com.interact.listen.pbx.Extension',NULL,NULL),(7,1,NULL,NULL,NULL,'777',7,'com.interact.listen.pbx.Extension',NULL,NULL);
UNLOCK TABLES;


LOCK TABLES `acd_user_status` WRITE;
INSERT INTO `acd_user_status` (id, owner_id, version, acd_queue_status, contact_number_id, status_modified, onacall,
                               onacall_modified)
        VALUES
        (2,2,0,'Available',NULL,NULL,0,NULL),
        (3,3,0,'Available',NULL,NULL,0,NULL),
        (4,4,0,'Available',NULL,NULL,0,NULL),
        (5,5,0,'Available',NULL,NULL,0,NULL),
        (6,6,0,'Available',NULL,NULL,0,NULL),
        (7,7,0,'Available',NULL,NULL,0,NULL),
        (8,8,0,'VoicemailBox',NULL,NULL,0,NULL),
        (9,9,0,'VoicemailBox',NULL,NULL,0,NULL),
        (10,10,0,'VoicemailBox',NULL,NULL,0,NULL),
        (11,11,0,'VoicemailBox',NULL,NULL,0,NULL);
UNLOCK TABLES;


LOCK TABLES `call_data` WRITE;
INSERT INTO `call_data` VALUES (1,1,'4024768786','4029992891','2013-11-05 15:55:48','7.0.CCXML_i0_d0.75.126.3.215.06BC34175B9420835416973128413836865697','2013-11-05 15:55:42'),(2,1,'4024768786','4024292891','2013-11-05 16:01:08','8.0.CCXML_i0_d0.75.126.3.215.06BC34175B9420835416973128413836865698','2013-11-05 15:57:56'),(3,1,'4024768786','4024292891','2013-11-06 08:45:26','2.1.CCXML_i0_d0.75.126.3.215.06BC34175B94208354169731284138368656912','2013-11-06 08:42:15'),(4,1,'338','14024768786','2013-11-18 16:06:47','0.0.CCXML_i0_d0.75.126.3.215.06BC34175B948720394613252513848120120','2013-11-18 16:06:39'),(5,1,'4024768786','338','2013-11-18 16:09:58','2.0.CCXML_i0_d0.75.126.3.215.06BC34175B948720394613252513848120122','2013-11-18 16:09:55'),(6,1,'338','14025700783','2013-11-21 11:47:32','4.0.CCXML_i0_d0.75.126.3.215.06BC34175B948720394613252513848120124','2013-11-21 11:46:37'),(7,1,'338','14024768786','2013-11-21 11:49:11','5.0.CCXML_i0_d0.75.126.3.215.06BC34175B948720394613252513848120125','2013-11-21 11:48:55'),(8,1,'338','14025700783','2013-11-21 11:49:44','6.0.CCXML_i0_d0.75.126.3.215.06BC34175B948720394613252513848120126','2013-11-21 11:49:36'),(9,1,'338','12247955050','2013-11-21 11:53:15','7.0.CCXML_i0_d0.75.126.3.215.06BC34175B948720394613252513848120127','2013-11-21 11:50:51'),(10,1,'338','14025700783','2013-11-21 12:00:06','0.0.CCXML_i0_d0.75.126.3.215.06BC34175B941083649698833713850567220','2013-11-21 11:59:49'),(11,1,'338','14025700783','2013-11-21 12:01:35','1.0.CCXML_i0_d0.75.126.3.215.06BC34175B941083649698833713850567221','2013-11-21 12:00:51'),(12,1,'338','14025700783','2013-11-21 12:08:44','0.0.CCXML_i0_d0.10.80.53.6.06BC34175B941299363544538713850572650','2013-11-21 12:08:30'),(13,1,'338','14025700783','2013-11-21 13:47:53','0.0.CCXML_i0_d0.75.126.3.215.06BC34175B9413352486073043413850631080','2013-11-21 13:47:34'),(14,1,'4024768786','338','2013-11-21 13:49:02','1.0.CCXML_i0_d0.75.126.3.215.06BC34175B9413352486073043413850631081','2013-11-21 13:48:29'),(15,1,'338','14025700783','2013-11-21 14:02:26','2.0.CCXML_i0_d0.75.126.3.215.06BC34175B9413352486073043413850631082','2013-11-21 14:02:08'),(16,1,'338','14025700783','2013-11-21 14:33:07','0.0.CCXML_i0_d0.10.80.53.6.06BC34175B945769809361977513850658820','2013-11-21 14:32:42');
UNLOCK TABLES;


LOCK TABLES `conference` WRITE;
INSERT INTO `conference` VALUES (1,0,NULL,'Brian Johnston\'s Conference','\0','\0',2,NULL,NULL),(2,0,NULL,'Scott Farwell\'s Conference','\0','\0',3,NULL,NULL),(3,0,NULL,'Ricardo Mastroleo\'s Conference','\0','\0',4,NULL,NULL),(4,0,NULL,'Ladi Akinyemi\'s Conference','\0','\0',5,NULL,NULL),(5,0,NULL,'test Listen\'s Conference','\0','\0',6,NULL,NULL),(6,0,NULL,'Daniel Walter\'s Conference','\0','\0',7,NULL,NULL);
UNLOCK TABLES;


LOCK TABLES `number_route` WRITE;
INSERT INTO `number_route` VALUES (1,0,'Attendant',NULL,1,'4022612738','EXTERNAL');
UNLOCK TABLES;


LOCK TABLES `organization_enabled_features` WRITE;
INSERT INTO `organization_enabled_features` VALUES (1,1,'IPPBX'),(2,1,'FINDME'),(3,1,'CONFERENCING'),(4,1,'ATTENDANT'),(5,1,'BROADCAST'),(6,1,'VOICEMAIL'),(7,1,'ACD');
UNLOCK TABLES;


LOCK TABLES `pin` WRITE;
INSERT INTO `pin` VALUES (1,0,1,'394047','ACTIVE'),(2,0,1,'828577','ADMIN'),(3,0,1,'259507','PASSIVE'),(4,0,2,'478301','ACTIVE'),(5,0,2,'460858','ADMIN'),(6,0,2,'020274','PASSIVE'),(7,0,3,'691106','ACTIVE'),(8,0,3,'519479','ADMIN'),(9,0,3,'188413','PASSIVE'),(10,0,4,'502138','ACTIVE'),(11,0,4,'962468','ADMIN'),(12,0,4,'267099','PASSIVE'),(13,0,5,'898090','ACTIVE'),(14,0,5,'082916','ADMIN'),(15,0,5,'314673','PASSIVE');
UNLOCK TABLES;


LOCK TABLES `user_role` WRITE;
INSERT INTO `user_role` VALUES (2,2),(3,2),(4,2),(5,2),(6,2),(7,2),(2,3),(3,3),(4,3),(5,3),(6,3),(7,3),(2,4),(3,4),(4,4),(5,4),(6,4),(7,4),(2,5),(3,5),(4,5),(5,5),(6,5),(7,5),(4,6),(5,6),(6,6),(7,6),(8,2),(8,3),(8,4),(8,5),(8,6),(2,6),(3,6),(1,7),(2,7),(3,7),(4,7),(5,7),(6,7),(7,7),(8,7),(7,9),(7,10),(7,11),(8,9),(8,10),(8,11);
UNLOCK TABLES;


LOCK TABLES `voicemail_preferences` WRITE;
INSERT INTO `voicemail_preferences` VALUES (1,1,'brian.johnston@newnet.com','','\0','4321','OLDEST_TO_NEWEST','\0',NULL,'\0',2),(2,0,NULL,'\0','\0','1234','OLDEST_TO_NEWEST','\0',NULL,'\0',3),(3,0,NULL,'\0','\0','1234','OLDEST_TO_NEWEST','\0',NULL,'\0',4),(4,0,NULL,'\0','\0','1234','OLDEST_TO_NEWEST','\0',NULL,'\0',5),(5,0,NULL,'\0','\0','1234','OLDEST_TO_NEWEST','\0',NULL,'\0',6);
UNLOCK TABLES;

LOCK TABLES `skill` WRITE;
INSERT INTO `skill` VALUES (1,0,'Tech',1,'Technical Support'),(2,0,'Finance',1,'Finance'),(3,0,'Human Resources',1,'Human Resources');
UNLOCK TABLES;

LOCK TABLES `user_skill` WRITE;
INSERT INTO `user_skill` VALUES (1,0,9,1), (2,0,10,2), (3,0,11,3), (4,0,7,1), (5,0,7,2), (6,0,7,3);
UNLOCK TABLES;

LOCK TABLES `acd_call` WRITE;
INSERT INTO `acd_call` VALUES (1,0,'test ani','test dnis',3,'test session id','2013-12-29 13:47:35','WAITING','test ivr','2013-12-29 13:47:35',1,'2013-12-29 13:47:35','2013-12-29 13:47:35'), (2,0,'lfasjdlkasdjjfl','another test dnis',2,'another test session id','2013-12-29 13:47:36','WAITING','another test ivr','2013-12-29 13:47:36',1,'2013-12-29 13:47:36','2013-12-29 13:47:36'), (3,0,'eeeadsflkqljljkklj','yet another test dnis',1,'yet another test session id','2013-12-29 13:47:37','WAITING','yet another test ivr','2013-12-29 13:47:37',1,'2013-12-29 13:47:37','2013-12-29 13:47:37');
UNLOCK TABLES;
