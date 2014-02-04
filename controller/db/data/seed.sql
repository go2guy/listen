use listen2;

/* This appears to reference an audio file that is not deployed by default */
LOCK TABLES `audio` WRITE;
INSERT INTO `audio` VALUES (1,0,'2013-11-05 15:54:59',NULL,'PT0S','2013-11-05 15:54:59','','file:/interact/listen/artifacts/2/voicemail/greeting/357-greeting.wav');
UNLOCK TABLES;

LOCK TABLES `user` WRITE;
INSERT INTO `user` (id, version, account_expired, account_locked, email_address, enabled, last_login, organization_id,
                    password, password_expired, real_name, username, is_active_directory)
      VALUES
        (2,0,'\0','\0','operator@newnet.com',1,'2013-11-05 16:08:21',1,
          '$2a$10$026QVF/Z7eE3w.6MRN/C3.bhmjMOgUK2ozRwtP6fHkBZmov/9vdEy','\0','Operator','operator','\0');
UNLOCK TABLES;

LOCK TABLES `phone_number` WRITE;
INSERT INTO `phone_number` VALUES (1,1,NULL,1,NULL,'357',2,'com.interact.listen.pbx.Extension',NULL,NULL);
UNLOCK TABLES;


LOCK TABLES `acd_user_status` WRITE;
INSERT INTO `acd_user_status` (id, owner_id, version, acd_queue_status, contact_number_id, status_modified, onacall,
                               onacall_modified)
        VALUES
        (2,2,0,'Unavailable',NULL,NULL,0,NULL);
UNLOCK TABLES;

LOCK TABLES `conference` WRITE;
INSERT INTO `conference` VALUES (1,0,NULL,'Brian Johnston\'s Conference','\0','\0',2,NULL,NULL);
UNLOCK TABLES;

LOCK TABLES `organization_enabled_features` WRITE;
INSERT INTO `organization_enabled_features` VALUES (1,1,'IPPBX'),(2,1,'FINDME'),(3,1,'CONFERENCING'),(4,1,'ATTENDANT'),(5,1,'BROADCAST'),(6,1,'VOICEMAIL'),(7,1,'ACD');
UNLOCK TABLES;


LOCK TABLES `pin` WRITE;
INSERT INTO `pin` VALUES (1,0,1,'394047','ACTIVE'),(2,0,1,'828577','ADMIN'),(3,0,1,'259507','PASSIVE');
UNLOCK TABLES;


LOCK TABLES `user_role` WRITE;
INSERT INTO `user_role` VALUES (2,2),(3,2),(4,2),(5,2),(6,2),(7,2),(8,2);
UNLOCK TABLES;


LOCK TABLES `voicemail_preferences` WRITE;
INSERT INTO `voicemail_preferences` (id, version, email_notification_address, is_email_notification_enabled, is_sms_notification_enabled, passcode, playback_order,
                                     recurring_notifications_enabled, sms_notification_address, transcribe, user_id)
       VALUES
       (1,1,'brian.johnston@newnet.com','','\0','4321','OLDEST_TO_NEWEST','\0',NULL,'\0',2);
UNLOCK TABLES;
