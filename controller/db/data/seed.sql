use listen2;

/* This appears to be invalid data */
/* LOCK TABLES `audio` WRITE; */
/* INSERT INTO `audio` VALUES (1,0,'2013-11-05 15:54:59',NULL,'PT0S','2013-11-05 15:54:59','','file:/interact/listen/artifacts/2/voicemail/greeting/357-greeting.wav'); */
/* UNLOCK TABLES; */


LOCK TABLES `call_data` WRITE;
INSERT INTO `call_data` VALUES (1,1,'4024768786','4024292891','2013-11-05 15:55:48','7.0.CCXML_i0_d0.75.126.3.215.06BC34175B9420835416973128413836865697','2013-11-05 15:55:42'),(2,1,'4024768786','4024292891','2013-11-05 16:01:08','8.0.CCXML_i0_d0.75.126.3.215.06BC34175B9420835416973128413836865698','2013-11-05 15:57:56'),(3,1,'4024768786','4024292891','2013-11-06 08:45:26','2.1.CCXML_i0_d0.75.126.3.215.06BC34175B94208354169731284138368656912','2013-11-06 08:42:15'),(4,1,'338','14024768786','2013-11-18 16:06:47','0.0.CCXML_i0_d0.75.126.3.215.06BC34175B948720394613252513848120120','2013-11-18 16:06:39'),(5,1,'4024768786','338','2013-11-18 16:09:58','2.0.CCXML_i0_d0.75.126.3.215.06BC34175B948720394613252513848120122','2013-11-18 16:09:55'),(6,1,'338','14025700783','2013-11-21 11:47:32','4.0.CCXML_i0_d0.75.126.3.215.06BC34175B948720394613252513848120124','2013-11-21 11:46:37'),(7,1,'338','14024768786','2013-11-21 11:49:11','5.0.CCXML_i0_d0.75.126.3.215.06BC34175B948720394613252513848120125','2013-11-21 11:48:55'),(8,1,'338','14025700783','2013-11-21 11:49:44','6.0.CCXML_i0_d0.75.126.3.215.06BC34175B948720394613252513848120126','2013-11-21 11:49:36'),(9,1,'338','12247955050','2013-11-21 11:53:15','7.0.CCXML_i0_d0.75.126.3.215.06BC34175B948720394613252513848120127','2013-11-21 11:50:51'),(10,1,'338','14025700783','2013-11-21 12:00:06','0.0.CCXML_i0_d0.75.126.3.215.06BC34175B941083649698833713850567220','2013-11-21 11:59:49'),(11,1,'338','14025700783','2013-11-21 12:01:35','1.0.CCXML_i0_d0.75.126.3.215.06BC34175B941083649698833713850567221','2013-11-21 12:00:51'),(12,1,'338','14025700783','2013-11-21 12:08:44','0.0.CCXML_i0_d0.10.80.53.6.06BC34175B941299363544538713850572650','2013-11-21 12:08:30'),(13,1,'338','14025700783','2013-11-21 13:47:53','0.0.CCXML_i0_d0.75.126.3.215.06BC34175B9413352486073043413850631080','2013-11-21 13:47:34'),(14,1,'4024768786','338','2013-11-21 13:49:02','1.0.CCXML_i0_d0.75.126.3.215.06BC34175B9413352486073043413850631081','2013-11-21 13:48:29'),(15,1,'338','14025700783','2013-11-21 14:02:26','2.0.CCXML_i0_d0.75.126.3.215.06BC34175B9413352486073043413850631082','2013-11-21 14:02:08'),(16,1,'338','14025700783','2013-11-21 14:33:07','0.0.CCXML_i0_d0.10.80.53.6.06BC34175B945769809361977513850658820','2013-11-21 14:32:42');
UNLOCK TABLES;


LOCK TABLES `call_history` WRITE;
INSERT INTO `call_history` VALUES (1,0,'4024768786','2013-11-05 15:55:42','4024292891','PT5.224S',NULL,1,NULL,'Call Answered'),(2,0,'4024768786','2013-11-05 15:57:56','4024292891','PT191.424S',NULL,1,NULL,'Call Answered'),(3,0,'4024768786','2013-11-06 08:42:15','4024292891','PT191.655S',NULL,1,NULL,'Call Answered'),(4,0,'4024768786','2013-11-18 16:09:55','338','PT2.916S',NULL,1,6,'Call Answered'),(5,0,'4024768786','2013-11-20 14:12:52','338','PT0S',NULL,1,6,'Call Failed'),(6,0,'4024768786','2013-11-21 13:48:29','338','PT32.906S',NULL,1,6,'Call Answered');
UNLOCK TABLES;


LOCK TABLES `conference` WRITE;
INSERT INTO `conference` VALUES (1,0,NULL,'Brian Johnston\'s Conference','\0','\0',2,NULL,NULL),(2,0,NULL,'Scott Farwell\'s Conference','\0','\0',3,NULL,NULL),(3,0,NULL,'Ricardo Mastroleo\'s Conference','\0','\0',4,NULL,NULL),(4,0,NULL,'Ladi Akinyemi\'s Conference','\0','\0',5,NULL,NULL),(5,0,NULL,'test Listen\'s Conference','\0','\0',6,NULL,NULL);
UNLOCK TABLES;


/* LOCK TABLES `conferencing_configuration` WRITE; */
/* UNLOCK TABLES; */


/* LOCK TABLES `device_registration` WRITE; */
/* UNLOCK TABLES; */


/* LOCK TABLES `device_registration_registered_types` WRITE; */
/* UNLOCK TABLES; */


/* LOCK TABLES `find_me_number` WRITE; */
/* UNLOCK TABLES; */


/* LOCK TABLES `find_me_preferences` WRITE; */
/* UNLOCK TABLES; */


/* LOCK TABLES `global_outdial_restriction` WRITE; */
/* UNLOCK TABLES; */


/* LOCK TABLES `google_auth_configuration` WRITE; */
/* UNLOCK TABLES; */


/* LOCK TABLES `inbox_message` WRITE; */
/* UNLOCK TABLES; */


/* LOCK TABLES `mail_configuration` WRITE; */
/* INSERT INTO `mail_configuration` VALUES (1,0,'listen@newnet.com','pod51018.outlook.com','Summer2013!','listen@newnet.com'); */
/* UNLOCK TABLES; */


/* LOCK TABLES `menu` WRITE; */
/* INSERT INTO `menu` VALUES (12,0,34,'',1,'In Office Attendant','newestinofficewelcome.wav',35); */
/* UNLOCK TABLES; */


/* LOCK TABLES `menu_action` WRITE; */
/* INSERT INTO `menu_action` VALUES (12,36),(12,37),(12,38); */
/* UNLOCK TABLES; */


LOCK TABLES `menu_group` WRITE;
INSERT INTO `menu_group` VALUES (1,16,'','Mimio In Office',1);
UNLOCK TABLES;


/* LOCK TABLES `menu_group_time_restriction` WRITE; */
/* UNLOCK TABLES; */


LOCK TABLES `number_route` WRITE;
INSERT INTO `number_route` VALUES (1,0,'Attendant',NULL,1,'4022612738','EXTERNAL');
UNLOCK TABLES;


LOCK TABLES `organization` WRITE;
INSERT INTO `organization` VALUES (1,0,'Mimio','mimio','');
UNLOCK TABLES;


LOCK TABLES `organization_enabled_features` WRITE;
INSERT INTO `organization_enabled_features` VALUES (1,'IPPBX'),(1,'FINDME'),(1,'CONFERENCING'),(1,'ATTENDANT'),(1,'BROADCAST'),(1,'VOICEMAIL');
UNLOCK TABLES;


/* LOCK TABLES `outdial_restriction` WRITE; */
/* UNLOCK TABLES; */


/* LOCK TABLES `outdial_restriction_exception` WRITE; */
/* UNLOCK TABLES; */


/* LOCK TABLES `outgoing_fax` WRITE; */
/* UNLOCK TABLES; */


/* LOCK TABLES `outgoing_fax_user_file` WRITE; */
/* UNLOCK TABLES; */


/* LOCK TABLES `participant` WRITE; */
/* UNLOCK TABLES; */


LOCK TABLES `phone_number` WRITE;
INSERT INTO `phone_number` VALUES (1,1,NULL,1,NULL,'357',2,'com.interact.listen.pbx.Extension',NULL,NULL),(2,0,NULL,NULL,NULL,'371',3,'com.interact.listen.pbx.Extension',NULL,NULL),(3,0,NULL,NULL,NULL,'112',4,'com.interact.listen.pbx.Extension',NULL,NULL),(4,0,NULL,NULL,NULL,'116',5,'com.interact.listen.pbx.Extension',NULL,NULL),(5,1,NULL,NULL,NULL,'338',6,'com.interact.listen.pbx.Extension',NULL,NULL);
UNLOCK TABLES;


LOCK TABLES `pin` WRITE;
INSERT INTO `pin` VALUES (1,0,1,'394047','ACTIVE'),(2,0,1,'828577','ADMIN'),(3,0,1,'259507','PASSIVE'),(4,0,2,'478301','ACTIVE'),(5,0,2,'460858','ADMIN'),(6,0,2,'020274','PASSIVE'),(7,0,3,'691106','ACTIVE'),(8,0,3,'519479','ADMIN'),(9,0,3,'188413','PASSIVE'),(10,0,4,'502138','ACTIVE'),(11,0,4,'962468','ADMIN'),(12,0,4,'267099','PASSIVE'),(13,0,5,'898090','ACTIVE'),(14,0,5,'082916','ADMIN'),(15,0,5,'314673','PASSIVE');
UNLOCK TABLES;


/* LOCK TABLES `prompt_override` WRITE; */
/* UNLOCK TABLES; */


/* LOCK TABLES `recording` WRITE; */
/* UNLOCK TABLES; */


/* LOCK TABLES `role` WRITE; */
/* INSERT INTO `role` VALUES (1,0,'ROLE_CUSTODIAN'),(2,0,'ROLE_ATTENDANT_ADMIN'),(3,0,'ROLE_ORGANIZATION_ADMIN'),(4,0,'ROLE_CONFERENCE_USER'),(5,0,'ROLE_FAX_USER'),(6,0,'ROLE_FINDME_USER'),(7,0,'ROLE_VOICEMAIL_USER'); */
/* UNLOCK TABLES; */


/* LOCK TABLES `scheduled_conference` WRITE; */
/* UNLOCK TABLES; */


LOCK TABLES `single_organization_configuration` WRITE;
INSERT INTO `single_organization_configuration` VALUES (1,0,1);
UNLOCK TABLES;


/* LOCK TABLES `spot_system` WRITE; */
/* INSERT INTO `spot_system` VALUES (1,0,'http://listen1.nimblevox.com/spot'); */
/* UNLOCK TABLES; */


/* LOCK TABLES `time_restriction` WRITE; */
/* UNLOCK TABLES; */


/* LOCK TABLES `transcription_configuration` WRITE; */
/* UNLOCK TABLES; */


LOCK TABLES `user` WRITE;
INSERT INTO `user` VALUES (2,5,'\0','\0','iisupport@newnet.com','^A','2013-11-05 15:23:38',NULL,'4bc6ef18248a1160d1812a741f6e867f3baff7ac3a20b40be8018be41448ffd4','\0','Mimio Listen Custodian','Custodian','\0'),(3,3,'\0','\0','scott.farwell@newnet.com','^A','2013-11-05 16:10:10',1,'73d1b1b1bc1dabfb97f216d897b7968e44b06457920f00f2dc6c1ed3be25ad4c','\0','Scott Farwell','scott','\0'),(4,3,'\0','\0','ricardo.mastroleo@newnet.com','^A','2013-11-18 15:10:49',1,'73d1b1b1bc1dabfb97f216d897b7968e44b06457920f00f2dc6c1ed3be25ad4c','\0','Ricardo Mastroleo','ricardo','\0'),(5,2,'\0','\0','ladi.akinyemi@newnet.com','^A','2013-11-05 16:18:37',1,'73d1b1b1bc1dabfb97f216d897b7968e44b06457920f00f2dc6c1ed3be25ad4c','\0','Ladi Akinyemi','ladi','\0'),(6,0,'\0','\0','testlisten@newnet.com','^A',NULL,1,'03ac674216f3e15c761ee1a5e255f067953623c8b388b4459e13f978d7c846f4','\0','test Listen','testlisten','\0');
UNLOCK TABLES;


/* LOCK TABLES `user_file` WRITE; */
/* UNLOCK TABLES; */


LOCK TABLES `user_role` WRITE;
INSERT INTO `user_role` VALUES (2,1),(3,2),(4,2),(5,2),(6,2),(7,2),(2,3),(3,3),(4,3),(5,3),(6,3),(7,3),(2,4),(3,4),(4,4),(5,4),(6,4),(7,4),(2,5),(3,5),(4,5),(5,5),(6,5),(7,5),(4,6),(5,6),(6,6),(7,6);
UNLOCK TABLES;


LOCK TABLES `voicemail_preferences` WRITE;
INSERT INTO `voicemail_preferences` VALUES (1,1,'brian.johnston@newnet.com','','\0','4321','OLDEST_TO_NEWEST','\0',NULL,'\0',2),(2,0,NULL,'\0','\0','1234','OLDEST_TO_NEWEST','\0',NULL,'\0',3),(3,0,NULL,'\0','\0','1234','OLDEST_TO_NEWEST','\0',NULL,'\0',4),(4,0,NULL,'\0','\0','1234','OLDEST_TO_NEWEST','\0',NULL,'\0',5),(5,0,NULL,'\0','\0','1234','OLDEST_TO_NEWEST','\0',NULL,'\0',6);
UNLOCK TABLES;


/* LOCK TABLES `voicemail_preferences_time_restriction` WRITE; */
/* UNLOCK TABLES; */
