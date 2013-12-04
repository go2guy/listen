use listen2;


LOCK TABLES `action` WRITE;
INSERT INTO `action` VALUES (34,0,NULL,'','com.interact.listen.attendant.action.ReplayMenuAction',NULL,NULL,NULL,NULL),(35,0,NULL,'','com.interact.listen.attendant.action.ReplayMenuAction',NULL,NULL,NULL,NULL),(36,0,'*','','com.interact.listen.attendant.action.LaunchApplicationAction',NULL,NULL,NULL,'Mailbox'),(37,0,'???','thankYou1Moment.wav','com.interact.listen.attendant.action.DialPressedNumberAction',NULL,NULL,NULL,NULL),(38,0,'1','sorryInvalidNumber.wav','com.interact.listen.attendant.action.ReplayMenuAction',NULL,NULL,NULL,NULL);
UNLOCK TABLES;


LOCK TABLES `organization` WRITE;
INSERT INTO `organization` VALUES (1,0,'Mimio','mimio','');
UNLOCK TABLES;


LOCK TABLES `user` WRITE;
INSERT INTO `user` VALUES (1,4,'\0','\0','brian.johnston@newnet.com','','2013-11-05 16:08:21',1,'4bc6ef18248a1160d1812a741f6e867f3baff7ac3a20b40be8018be41448ffd4','\0','Brian Johnston','Custodian','\0');
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
INSERT INTO `menu_action` VALUES (12,36),(12,37),(12,38);
UNLOCK TABLES;


/* LOCK TABLES `number_route` WRITE; */
/* INSERT INTO `number_route` VALUES (1,0,'Attendant',NULL,1,'4022612738','EXTERNAL'); */
/* UNLOCK TABLES; */


/* LOCK TABLES `organization_enabled_features` WRITE; */
/* INSERT INTO `organization_enabled_features` VALUES (1,'IPPBX'),(1,'FINDME'),(1,'CONFERENCING'),(1,'ATTENDANT'),(1,'BROADCAST'),(1,'VOICEMAIL'); */
/* UNLOCK TABLES; */


/* LOCK TABLES `phone_number` WRITE; */
/* INSERT INTO `phone_number` VALUES (1,1,NULL,1,NULL,'357',2,'com.interact.listen.pbx.Extension',NULL,NULL),(2,0,NULL,NULL,NULL,'371',3,'com.interact.listen.pbx.Extension',NULL,NULL),(3,0,NULL,NULL,NULL,'112',4,'com.interact.listen.pbx.Extension',NULL,NULL),(4,0,NULL,NULL,NULL,'116',5,'com.interact.listen.pbx.Extension',NULL,NULL),(5,1,NULL,NULL,NULL,'338',6,'com.interact.listen.pbx.Extension',NULL,NULL); */
/* UNLOCK TABLES; */


/* LOCK TABLES `pin` WRITE; */
/* INSERT INTO `pin` VALUES (1,0,1,'394047','ACTIVE'),(2,0,1,'828577','ADMIN'),(3,0,1,'259507','PASSIVE'),(4,0,2,'478301','ACTIVE'),(5,0,2,'460858','ADMIN'),(6,0,2,'020274','PASSIVE'),(7,0,3,'691106','ACTIVE'),(8,0,3,'519479','ADMIN'),(9,0,3,'188413','PASSIVE'),(10,0,4,'502138','ACTIVE'),(11,0,4,'962468','ADMIN'),(12,0,4,'267099','PASSIVE'),(13,0,5,'898090','ACTIVE'),(14,0,5,'082916','ADMIN'),(15,0,5,'314673','PASSIVE'); */
/* UNLOCK TABLES; */


LOCK TABLES `role` WRITE;
INSERT INTO `role` VALUES (1,0,'ROLE_CUSTODIAN'),(2,0,'ROLE_ATTENDANT_ADMIN'),(3,0,'ROLE_ORGANIZATION_ADMIN'),(4,0,'ROLE_CONFERENCE_USER'),(5,0,'ROLE_FAX_USER'),(6,0,'ROLE_FINDME_USER'),(7,0,'ROLE_VOICEMAIL_USER');
UNLOCK TABLES;


/* LOCK TABLES `single_organization_configuration` WRITE; */
/* INSERT INTO `single_organization_configuration` VALUES (1,0,1); */
/* UNLOCK TABLES; */


LOCK TABLES `spot_system` WRITE;
INSERT INTO `spot_system` VALUES (1,0,'http://listen1.nimblevox.com/spot');
UNLOCK TABLES;


/* LOCK TABLES `user` WRITE; */
/* INSERT INTO `user` VALUES (1,5,'\0','\0','iisupport@newnet.com','','2013-11-05 15:23:38',NULL,'4bc6ef18248a1160d1812a741f6e867f3baff7ac3a20b40be8018be41448ffd4','\0','Mimio Listen Custodian','Custodian','\0'),(2,4,'\0','\0','brian.johnston@newnet.com','','2013-11-05 16:08:21',1,'4bc6ef18248a1160d1812a741f6e867f3baff7ac3a20b40be8018be41448ffd4','\0','Brian Johnston','operator','\0'),(3,3,'\0','\0','scott.farwell@newnet.com','','2013-11-05 16:10:10',1,'73d1b1b1bc1dabfb97f216d897b7968e44b06457920f00f2dc6c1ed3be25ad4c','\0','Scott Farwell','scott','\0'),(4,3,'\0','\0','ricardo.mastroleo@newnet.com','','2013-11-18 15:10:49',1,'73d1b1b1bc1dabfb97f216d897b7968e44b06457920f00f2dc6c1ed3be25ad4c','\0','Ricardo Mastroleo','ricardo','\0'),(5,2,'\0','\0','ladi.akinyemi@newnet.com','','2013-11-05 16:18:37',1,'73d1b1b1bc1dabfb97f216d897b7968e44b06457920f00f2dc6c1ed3be25ad4c','\0','Ladi Akinyemi','ladi','\0'),(6,0,'\0','\0','testlisten@newnet.com','',NULL,1,'03ac674216f3e15c761ee1a5e255f067953623c8b388b4459e13f978d7c846f4','\0','test Listen','testlisten','\0'); */
/* UNLOCK TABLES; */



LOCK TABLES `user_role` WRITE;
INSERT INTO `user_role` VALUES (1,1);
/* INSERT INTO `user_role` VALUES (1,1),(2,2),(3,2),(4,2),(5,2),(6,2),(7,2),(2,3),(3,3),(4,3),(5,3),(6,3),(7,3),(2,4),(3,4),(4,4),(5,4),(6,4),(7,4),(2,5),(3,5),(4,5),(5,5),(6,5),(7,5),(4,6),(5,6),(6,6),(7,6); */
UNLOCK TABLES;


/* LOCK TABLES `voicemail_preferences` WRITE; */
/* INSERT INTO `voicemail_preferences` VALUES (1,1,'brian.johnston@newnet.com','','\0','4321','OLDEST_TO_NEWEST','\0',NULL,'\0',2),(2,0,NULL,'\0','\0','1234','OLDEST_TO_NEWEST','\0',NULL,'\0',3),(3,0,NULL,'\0','\0','1234','OLDEST_TO_NEWEST','\0',NULL,'\0',4),(4,0,NULL,'\0','\0','1234','OLDEST_TO_NEWEST','\0',NULL,'\0',5),(5,0,NULL,'\0','\0','1234','OLDEST_TO_NEWEST','\0',NULL,'\0',6); */
/* UNLOCK TABLES; */
