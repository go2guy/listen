use listen2;


LOCK TABLES `action` WRITE;
INSERT INTO `action` VALUES (34,0,NULL,'','com.interact.listen.attendant.action.ReplayMenuAction',NULL,NULL,NULL,NULL,NULL),(35,0,NULL,'','com.interact.listen.attendant.action.ReplayMenuAction',NULL,NULL,NULL,NULL,NULL),(36,0,'*','','com.interact.listen.attendant.action.LaunchApplicationAction',NULL,NULL,NULL,NULL,'Mailbox'),(37,0,'???','thankYou1Moment.wav','com.interact.listen.attendant.action.DialPressedNumberAction',NULL,NULL,NULL,NULL,NULL),(38,0,'1','sorryInvalidNumber.wav','com.interact.listen.attendant.action.ReplayMenuAction',NULL,NULL,NULL,NULL,NULL);
UNLOCK TABLES;


LOCK TABLES `organization` WRITE;
INSERT INTO `organization` (id,version,name,context_path,enabled,outbound_callid,outbound_callid_by_did) VALUES (1,0,'NewNet','NewNet',1,'4024768786',1);
UNLOCK TABLES;


LOCK TABLES `user` WRITE;
INSERT INTO `user` (id, version, account_expired, account_locked, email_address, enabled, last_login, organization_id,
    password, password_expired, real_name, username, is_active_directory)
  VALUES (1,5,'\0','\0','iisupport@newnet.com',1,'2013-11-05 15:23:38',NULL,
    '$2a$10$026QVF/Z7eE3w.6MRN/C3.bhmjMOgUK2ozRwtP6fHkBZmov/9vdEy','\0','Listen Custodian','Custodian','\0');
UNLOCK TABLES;

LOCK TABLES `acd_user_status` WRITE;
INSERT INTO `acd_user_status` (id, owner_id, version, acd_queue_status, contact_number_id, status_modified, onacall,
                               onacall_modified) VALUES (1,1,0,'Available',NULL,NULL,0,NULL);
UNLOCK TABLES;


LOCK TABLES `mail_configuration` WRITE;
INSERT INTO `mail_configuration` VALUES (1,0,'listen@newnet.com','pod51018.outlook.com','Summer2013!','listen@newnet.com');
UNLOCK TABLES;


LOCK TABLES `menu_group` WRITE;
INSERT INTO `menu_group` VALUES (1,16,'','NewNet In Office',1);
UNLOCK TABLES;


LOCK TABLES `menu` WRITE;
INSERT INTO `menu` VALUES (12,0,34,'',1,'In Office Attendant','newestinofficewelcome.wav',35);
UNLOCK TABLES;


LOCK TABLES `menu_action` WRITE;
INSERT INTO `menu_action` VALUES (1,12,36),(2,12,37),(3,12,38);
UNLOCK TABLES;

LOCK TABLES `role` WRITE;
INSERT INTO `role` VALUES (1,0,'ROLE_CUSTODIAN'),(2,0,'ROLE_ATTENDANT_ADMIN'),(3,0,'ROLE_ORGANIZATION_ADMIN'),(4,0,'ROLE_CONFERENCE_USER'),(5,0,'ROLE_FAX_USER'),(6,0,'ROLE_FINDME_USER'),(7,0,'ROLE_VOICEMAIL_USER'),(8,0,'ROLE_ACD_USER'),(9,0,'ROLE_QUEUE_USER');
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

LOCK TABLES `prompt_override` WRITE;
INSERT INTO prompt_override (version,options_prompt,use_menu_id,start_date,end_date,event_type)
  values (0,'closedCompanyEvent.wav',1,now() - interval 2 day, now() - interval 1 day, 'UNSCHEDULED_EVENT');
UNLOCK TABLES;

--
-- Dumping data for table `provisioner_template`
--

LOCK TABLES `provisioner_template` WRITE;
/*!40000 ALTER TABLE `provisioner_template` DISABLE KEYS */;
INSERT INTO `provisioner_template` VALUES (1,'Polycom IP 550','<?xml version=\"1.0\" standalone=\"yes\"?>\r\n<!-- Default Master SIP Configuration File-->\r\n<!-- Edit and rename this file to <Ethernet-address>.cfg for each phone.-->\r\n<!-- $Revision: 1.12 $  $Date: 2003/06/17 15:26:10 $ -->\r\n<APPLICATION APP_FILE_PATH=\"sip.ld\" CONFIG_FILES=\"phonedefault.cfg, sip.cfg\" MISC_FILES=\"\" LOG_FILE_DIRECTORY=\"\"/>',32),(3,'Linksys SPA942','<flat-profile>\r\n  <GPP_A> 12345678</GPP_A>\r\n  <Enable_Web_Server group=\"System/System_Configuration\">Yes</Enable_Web_Server>\r\n  <Web_Server_Port group=\"System/System_Configuration\">80</Web_Server_Port>\r\n  <Enable_Web_Admin_Access group=\"System/System_Configuration\">Yes</Enable_Web_Admin_Access>\r\n  <Admin_Passwd group=\"System/System_Configuration\"><%=password%></Admin_Passwd>\r\n  <HostName group=\"System/Optional_Network_Configuration\"></HostName>\r\n  <Domain group=\"System/Optional_Network_Configuration\">newnet.local</Domain>\r\n  <Primary_DNS group=\"System/Optional_Network_Configuration\">10.10.11.199</Primary_DNS>\r\n  <Secondary_DNS group=\"System/Optional_Network_Configuration\">10.19.11.199</Secondary_DNS>\r\n  <Primary_NTP_Server group=\"System/Optional_Network_Configuration\">ntphost1</Primary_NTP_Server>\r\n  <Secondary_NTP_Server group=\"System/Optional_Network_Configuration\">ntphost2</Secondary_NTP_Server>\r\n  <RTP_Packet_Size group=\"SIP/RTP_Parameters\">0.020</RTP_Packet_Size>\r\n  <Dial_Tone group=\"Regional/Call_Progress_Tones\">420@-16;10(*/0/1)</Dial_Tone>\r\n  <Outside_Dial_Tone group=\"Regional/Call_Progress_Tones\">350@-19,440@-19;10(*/0/1+2)</Outside_Dial_Tone>\r\n  <Interdigit_Short_Timer group=\"Regional/Control_Timer_Values__sec_\">5</Interdigit_Short_Timer>\r\n  <Set_Local_Date__mm_dd_ group=\"Regional/Miscellaneous\"><% var d=new Date(); print((d.getMonth() < 10 ? \"0\"+d.getMonth() : d.getMonth())+\"/\"+(d.getDate() < 10 ? \"0\"+d.getDate() : d.getDate())); %></Set_Local_Date__mm_dd_>\r\n  <Set_Local_Time__HH_mm_ group=\"Regional/Miscellaneous\"><% var d=new Date(); print((d.getHours() < 10 ? \"0\"+d.getHours() : d.getHours())+\"/\"+(d.getMinutes() < 10 ? \"0\"+d.getMinutes() : d.getMinutes())); %></Set_Local_Time__HH_mm_>\r\n  <Time_Zone group=\"Regional/Miscellaneous\">GMT-06:00</Time_Zone>\r\n  <Daylight_Saving_Time_Rule group=\"Regional/Miscellaneous\">start=3/-14/7/2:0:0;end=11/-7/7/2:0:0;save=1</Daylight_Saving_Time_Rule>\r\n  <Daylight_Saving_Time_Enable group=\"Regional/Miscellaneous\">Yes</Daylight_Saving_Time_Enable>\r\n  <Station_Name group=\"Phone/General\">Ext <%=number%></Station_Name>\r\n  <Voice_Mail_Number group=\"Phone/General\">770</Voice_Mail_Number>\r\n  <Text_Logo group=\"Phone/General\">NewNet</Text_Logo>\r\n  <Select_Logo group=\"Phone/General\">Default</Select_Logo>\r\n  <Select_Background_Picture group=\"Phone/General\">Text Logo</Select_Background_Picture>\r\n  <Screen_Saver_Enable group=\"Phone/General\">No</Screen_Saver_Enable>\r\n  <Screen_Saver_Wait group=\"Phone/General\">300</Screen_Saver_Wait>\r\n  <Screen_Saver_Icon group=\"Phone/General\">Background Picture</Screen_Saver_Icon>\r\n  <Handset_Input_Gain group=\"Phone/Audio_Input_Gain__dB_\">0</Handset_Input_Gain>\r\n  <Headset_Input_Gain group=\"Phone/Audio_Input_Gain__dB_\">-6</Headset_Input_Gain>\r\n  <Speakerphone_Input_Gain group=\"Phone/Audio_Input_Gain__dB_\">-6</Speakerphone_Input_Gain>\r\n  <Handset_Additional_Input_Gain group=\"Phone/Audio_Input_Gain__dB_\">0</Handset_Additional_Input_Gain>\r\n  <Headset_Additional_Input_Gain group=\"Phone/Audio_Input_Gain__dB_\">-3</Headset_Additional_Input_Gain>\r\n  <Speakerphone_Additional_Input_Gain group=\"Phone/Audio_Input_Gain__dB_\">-3</Speakerphone_Additional_Input_Gain>\r\n  <Line_Enable_1_ group=\"Ext_1/General\">Yes</Line_Enable_1_>\r\n  <SIP_Port_1_ group=\"Ext_1/SIP_Settings\">5060</SIP_Port_1_>\r\n  <Proxy_1_ group=\"Ext_1/Proxy_and_Registration\">listen.iivip.com</Proxy_1_>\r\n  <Display_Name_1_ group=\"Ext_1/Subscriber_Information\"><%=realName%> Ext <%=number%></Display_Name_1_>\r\n  <User_ID_1_ group=\"Ext_1/Subscriber_Information\"><%=userId%></User_ID_1_>\r\n  <Password_1_ group=\"Ext_1/Subscriber_Information\"><%=password%></Password_1_>\r\n  <Use_Auth_ID_1_ group=\"Ext_1/Subscriber_Information\">Yes</Use_Auth_ID_1_>\r\n  <Auth_ID_1_ group=\"Ext_1/Subscriber_Information\"><%=username%></Auth_ID_1_>\r\n  <Dial_Plan_1_ group=\"Ext_1/Dial_Plan\">(S5 <9,:9>x.|xxx S2|x.*x.*x.*x. S3|#xx.|#9xxx)</Dial_Plan_1_>\r\n  <SIP_Port_2_ group=\"Ext_2/SIP_Settings\">5060</SIP_Port_2_>\r\n  <SIP_Port_3_ group=\"Ext_2/SIP_Settings\">5060</SIP_Port_3_>\r\n  <SIP_Port_4_ group=\"Ext_2/SIP_Settings\">5060</SIP_Port_4_>\r\n  <Auto_Answer_Page group=\"User/Supplementary_Services\">Yes</Auto_Answer_Page>\r\n  <Ringer_Volume group=\"User/Audio_Volume\">7</Ringer_Volume>\r\n  <Speaker_Volume group=\"User/Audio_Volume\">9</Speaker_Volume>\r\n  <Handset_Volume group=\"User/Audio_Volume\">9</Handset_Volume>\r\n  <Headset_Volume group=\"User/Audio_Volume\">7</Headset_Volume>\r\n  <LCD_Contrast group=\"User/Audio_Volume\">7</LCD_Contrast>\r\n  <Back_Light_Timer group=\"User/Audio_Volume\">10 s</Back_Light_Timer>\r\n</flat-profile>',20),(4,'Mitel 5330','<Parameter Model=\"5330\">\r\n   <addr_type>0</addr_type>\r\n   <poundkeydial>1</poundkeydial>\r\n   <dialtonekey>12</dialtonekey>\r\n   <htmlpuseraccess>1</htmlpuseraccess>\r\n   <remote_reboot>1</remote_reboot>\r\n   <sipkeepalive>1</sipkeepalive>\r\n   <rss_feed></rss_feed>\r\n   <blf_pickup>*98</blf_pickup>\r\n   <sntp>pool.ntp.org</sntp>\r\n   <session_timer>0</session_timer>\r\n   <beep_on_hold>1</beep_on_hold>\r\n   <on_hold_alert>60</on_hold_alert>\r\n   <system_mode>0</system_mode>\r\n   <callCountIn>0</callCountIn>\r\n   <callCountOut>0</callCountOut>\r\n   <discovery>0</discovery>\r\n   <pbIndex>0</pbIndex>\r\n   <adminId>admin</adminId>\r\n   <admin_dispname>Administrator</admin_dispname>\r\n   <admin_passwd>cbc462e27100dad71cdbf606d396ddad</admin_passwd>\r\n   <busy_fwd_mode>0</busy_fwd_mode>\r\n   <always_fwd_mode>0</always_fwd_mode>\r\n   <pcport>0</pcport>\r\n   <lanport>0</lanport>\r\n   <lcd>5</lcd>\r\n   <lcd_brightness>9</lcd_brightness>\r\n   <dtringtype1>1</dtringtype1>\r\n   <dtringtype3>2</dtringtype3>\r\n   <http_task_enable>1</http_task_enable>\r\n   <https_task_enable>0</https_task_enable>\r\n   <httpport>80</httpport>\r\n   <httpsport>443</httpsport>\r\n   <telnet_task_enable>1</telnet_task_enable>\r\n   <voicemail_ringnum>4</voicemail_ringnum>\r\n   <gruu_ctl>1</gruu_ctl>\r\n   <proxyrequire_ctl>0</proxyrequire_ctl>\r\n   <fwEnable>0</fwEnable>\r\n   <fwWanurl></fwWanurl>\r\n   <sym_udp>0</sym_udp>\r\n   <stunip>213.192.59.75</stunip>\r\n   <fwWanDurl></fwWanDurl>\r\n   <fwMode>0</fwMode>\r\n   <start_port>50000</start_port>\r\n   <end_port>50511</end_port>\r\n   <multi_user_enable>0</multi_user_enable>\r\n   <bksrvtm>3</bksrvtm>\r\n   <ntfcfg>0</ntfcfg>\r\n   <lancode>en_US</lancode>\r\n   <tonecode>US</tonecode>\r\n   <dsmode>1</dsmode>\r\n   <dsmonth>3</dsmonth>\r\n   <dsweek>1</dsweek>\r\n   <dsday>1</dsday>\r\n   <dsemonth>11</dsemonth>\r\n   <dseweek>1</dseweek>\r\n   <dseday>1</dseday>\r\n   <ds_transition_time>2</ds_transition_time>\r\n   <flashVer>201</flashVer>\r\n   <prov_server_url></prov_server_url>\r\n   <dialpl></dialpl>\r\n   <gtEnable>1</gtEnable>\r\n   <dtimer>5</dtimer>\r\n   <autoanswer>0</autoanswer>\r\n   <ringPitch>0</ringPitch>\r\n   <ringerVol>6</ringerVol>\r\n   <handsfreeVol>3</handsfreeVol>\r\n   <keysys_enable>0</keysys_enable>\r\n   <srtp>0</srtp>\r\n   <cw_tone>1</cw_tone>\r\n   <missedcallsctl>1</missedcallsctl>\r\n   <callforwardctl>1</callforwardctl>\r\n   <lcdbacklightctl>1</lcdbacklightctl>\r\n   <time_format>1</time_format>\r\n   <textsize>1</textsize>\r\n   <csta_enable>0</csta_enable>\r\n   <cfg_poll_timer>1440</cfg_poll_timer>\r\n   <reboot_phone>1</reboot_phone>\r\n   <firmware_timer>1440</firmware_timer>\r\n   <firmware_abs_timer_hr>23</firmware_abs_timer_hr>\r\n   <firmware_abs_timer_min>59</firmware_abs_timer_min>\r\n   <firmware_abs_enable>1</firmware_abs_enable>\r\n   <installer_passcode></installer_passcode>\r\n   <user_passwd>fb229f77148b399364a04a1ee12df999</user_passwd>\r\n   <html_filename></html_filename>\r\n   <htmlapp_mandatory_dwnld>0</htmlapp_mandatory_dwnld>\r\n   <http_user></http_user>\r\n   <http_passwd>******</http_passwd>\r\n   <facDef>90</facDef>\r\n   <checkpeercert>0</checkpeercert>\r\n   <dhcpenable>1</dhcpenable>\r\n   <tftp_config>1</tftp_config>\r\n   <cfg_update_enable>1</cfg_update_enable>\r\n   <pppoe_enable>0</pppoe_enable>\r\n   <tftp_task_enable>1</tftp_task_enable>\r\n   <tftp_upgrade>0</tftp_upgrade>\r\n   <http_upgrade>0</http_upgrade>\r\n   <local_sip_port>5060</local_sip_port>\r\n   <tos>0</tos>\r\n   <e802_priority>5</e802_priority>\r\n   <sntp>pool.ntp.org</sntp>\r\n   <time_zone>-6</time_zone>\r\n   <auth_method>2</auth_method>\r\n   <register_expire>3000</register_expire>\r\n   <audio_codec>5</audio_codec>\r\n   <audio_pkt_size>20</audio_pkt_size>\r\n   <dtmf_type>0</dtmf_type>\r\n   <dtmf_payload>101</dtmf_payload>\r\n   <dtringtype1>1</dtringtype1>\r\n   <dtringtype3>2</dtringtype3>\r\n   <downloadtype>1</downloadtype>\r\n   <snmp>1</snmp>\r\n   <web_logo1>&lt;img src=\"http://www.newnet.com/wp-content/uploads/2014/04/logo.png\" alt=\"Mitel\" width=\"143\" height=\"43\" hspace=\"0\" align=\"left\" /&gt;</web_logo1>\r\n   <voicemail_key>770</voicemail_key>\r\n   <!-- User-specific config beyond this point -->\r\n   <host_name>RBachta-PHN</host_name>\r\n   <domain>newnet.local</domain>\r\n   <time_zone>-6</time_zone>\r\n   <pkDescription>\r\n      <Key Line=\"25\" Fea=\"6\" Des=\"Line 1\" Addr=\"\" Addr2=\"\" Mode=\"1\" Mode2=\"1\" UserID=\"<%=userId%>\"></Key>\r\n      <Key Line=\"26\" Fea=\"7\" Des=\"Line 2\" Addr=\"\" Addr2=\"\" Mode=\"1\" Mode2=\"1\" UserID=\"<%=userId%>\"></Key>\r\n      <Key Line=\"27\" Fea=\"8\" Des=\"Line 3\" Addr=\"\" Addr2=\"\" Mode=\"1\" Mode2=\"1\" UserID=\"<%=userId%>\"></Key>\r\n      <Key Line=\"28\" Fea=\"9\" Des=\"Line 4\" Addr=\"\" Addr2=\"\" Mode=\"1\" Mode2=\"1\" UserID=\"<%=userId%>\"></Key>\r\n      <Key Line=\"29\" Fea=\"19\" Des=\"Weather\" Addr=\"http://weather.yahooapis.com/forecastrss?w=2441249\" Addr2=\"\" Mode=\"1\" Mode2=\"1\" UserID=\"\"></Key>\r\n      <Key Line=\"30\" Fea=\"17\" Des=\"Do Not Disturb   \" Addr=\"\" Addr2=\"\" Mode=\"1\" Mode2=\"1\" UserID=\"\"></Key>\r\n      <Key Line=\"31\" Fea=\"2\" Des=\"Call History\" Addr=\"\" Addr2=\"\" Mode=\"1\" Mode2=\"1\" UserID=\"\"></Key>\r\n      <Key Line=\"32\" Fea=\"4\" Des=\"Headset\" Addr=\"\" Addr2=\"\" Mode=\"1\" Mode2=\"1\" UserID=\"\"></Key>\r\n   </pkDescription>\r\n   <user_list>\r\n      <User State=\"1\" ID=\"<%=userId%>\" DispName=\"<%=username%> - Blarg\" Pwd=\"<%=password%>\" AuthName=\"<%=username%>\" Realm=\"\" RegSvr=\"10.19.10.10\" RegPort=\"5060\" RegScheme=\"2\" ProxySvr=\"10.10.40.59\" ProxyPort=\"5060\" ProxyScheme=\"2\" VMSvr=\"10.10.40.59\" VMPort=\"5060\" VMScheme=\"2\" OutSvr=\"\" OutPort=\"5060\" OutCtr=\"0\" Ring=\"1\" Line=\"0\" EventSvr=\"\" EventPort=\"5060\" EventScheme=\"2\" NatMode=\"0\" NatType=\"option\" NatIp=\"0\" BlfGroup=\"\"></User>\r\n   </user_list>\r\n</Parameter>',1);
/*!40000 ALTER TABLE `provisioner_template` ENABLE KEYS */;
UNLOCK TABLES;
