#!/bin/bash


echo "Creating Interact organization"
mysql -u root -e "insert into listen2.organization (id, version, name) values (1, 0, 'Interact Incorporated');"
mysql -u root -e "insert into listen2.menu_group (id, version, name, organization_id, is_default) values (1, 0, 'Default', 1, true);"
#Add enabled features
mysql -u root -e "insert into listen2.organization_enabled_features (organization_id, listen_feature) values (1, 'VOICEMAIL'), (1, 'FINDME'), (1, 'CUSTOM_APPLICATIONS'), (1, 'CONFERENCING'), (1, 'BROADCAST'), (1, 'IPPBX');"

echo "Migrating users"
mysql -u root -e "insert into listen2.user (id, version, email_address, last_login, password, real_name, username, enabled, is_active_directory) select id, version, work_email_address, last_login, password, real_name, username, 1, is_active_directory from listen.SUBSCRIBER where id > 1;"
mysql -u root -e "update listen2.user set organization_id = 1 where id > 1;"
#provide permissions to everyone
mysql -u root -e "insert into listen2.user_role (role_id, user_id) select 4, id from listen2.user where id > 1;"
mysql -u root -e "insert into listen2.user_role (role_id, user_id) select 5, id from listen2.user where id > 1;"
mysql -u root -e "insert into listen2.user_role (role_id, user_id) select 6, id from listen2.user where id > 1;"

#change passwords to 'super' for all non-AD accounts
mysql -u root -e "update listen2.user set password = '73d1b1b1bc1dabfb97f216d897b7968e44b06457920f00f2dc6c1ed3be25ad4c' where is_active_directory = false;"

#Grant Operator permissions for various users
mysql -u root -e "insert into listen2.user_role (role_id, user_id) values (2, (select id from listen2.user where username = 'tbritson')), (3, (select id from listen2.user where username = 'tbritson'));"

mysql -u root -e "insert into listen2.user_role (role_id, user_id) values (2, (select id from listen2.user where username = 'twilbrand')), (3, (select id from listen2.user where username = 'twilbrand'));"

mysql -u root -e "insert into listen2.user_role (role_id, user_id) values (2, (select id from listen2.user where username = 'rhruska')), (3, (select id from listen2.user where username = 'rhruska'));"

mysql -u root -e "insert into listen2.user_role (role_id, user_id) values (2, (select id from listen2.user where username = 'mwemhoff')), (3, (select id from listen2.user where username = 'mwemhoff'));"

mysql -u root -e "insert into listen2.user_role (role_id, user_id) values (2, (select id from listen2.user where username = 'vruenprom')), (3, (select id from listen2.user where username = 'vruenprom'));"

mysql -u root -e "insert into listen2.user_role (role_id, user_id) values (2, (select id from listen2.user where username = 'prapp')), (3, (select id from listen2.user where username = 'prapp'));"

mysql -u root -e "insert into listen2.user_role (role_id, user_id) values (2, (select id from listen2.user where username = 'lakinyemi')), (3, (select id from listen2.user where username = 'lakinyemi'));"

mysql -u root -e "insert into listen2.user_role (role_id, user_id) values (2, (select id from listen2.user where username = 'cscribner')), (3, (select id from listen2.user where username = 'cscribner'));"

echo "Migrating conferences and pins"
mysql -u root -e "insert into listen2.conference (id, version, arcade_id, description, is_recording, is_started, owner_id, recording_session_id, start_time) select id, version, arcade_id, description, is_recording, is_started, subscriber_id, recording_session_id, start_time from listen.CONFERENCE;"

mysql -u root -e "insert into listen2.pin (id, version, conference_id, number, pin_type) select id, version, conference_id, number, type from listen.PIN;"

mysql -u root -e "insert into listen2.scheduled_conference (id, version, active_caller_addresses, date, date_created, email_body, email_subject, ends, for_conference_id, passive_caller_addresses, scheduled_by_id, starts) select a.id, a.version, (select group_concat(email_address) from listen.SCHEDULED_CONFERENCE_ACTIVE_CALLERS b where b.scheduled_conference_id=a.id), DATE(a.start_date), now(), a.notes, a.topic, time(a.end_date), a.conference_id, (select group_concat(email_address) from listen.SCHEDULED_CONFERENCE_PASSIVE_CALLERS c where c.scheduled_conference_id=a.id), a.scheduled_by_subscriber_id, time(a.start_date) from listen.SCHEDULED_CONFERENCE a;" 

echo "Migrating Voicemails"
#get the audio entries in place
mysql -u root -e "insert into listen2.audio (id, version, date_created, description, duration, file_size, last_updated, transcription, uri) select id, version, date_created, description, duration, file_size, date_created, transcription, uri from listen.AUDIO;"

#update durations to be joda Duration format
mysql -u root -e "update listen2.audio set duration  = CONCAT('PT', (duration / 1000), 'S');"

#now create the voicemail rows
mysql -u root -e "insert into listen2.voicemail (id, ani, audio_id, date_created, forwarded_by_id, is_new, left_by_id, owner_id) select c.id, c.left_by, c.id, c.date_created, c.forwarded_by_subscriber_id, c.is_new, (select a.id from listen2.user a, listen.AUDIO b where b.id=c.id and a.real_name=b.left_by_name), c.subscriber_id from listen.AUDIO c where c.dtype = 'Voicemail';"

mysql -u root -e "insert into listen2.recording (id, version, audio_id, conference_id) select c.id, c.version, c.id, c.conference_id from listen.AUDIO c where c.dtype = 'ConferenceRecording';"

echo "Migrating Access numbers"
#insert an audio record for each greeting location
mysql -u root -e "insert into listen2.audio (uri) select distinct greeting_location from listen.ACCESS_NUMBER where greeting_location IS NOT NULL;"

mysql -u root -e "insert into listen2.phone_number (id, version, forwarded_to, greeting_id, is_public, number, owner_id, supports_message_light, type) select b.id, b.version, b.forwarded_to, (select a.id from listen2.audio a where a.uri = b.greeting_location), b.is_public, b.number, b.subscriber_id, b.supports_message_light, b.number_type from listen.ACCESS_NUMBER b;"

echo "Migrating Voicemail preferences and time restrictions"
#time restriction first
mysql -u root -e "insert into listen2.time_restriction (id, version, start_time, end_time, monday, tuesday, wednesday, thursday, friday, saturday, sunday) select a.id, a.version, a.start_time, a.end_time, a.monday, a.tuesday, a.wednesday, a.thursday, a.friday, a.saturday, a.sunday from listen.TIME_RESTRICTION a;"

mysql -u root -e "insert into listen2.voicemail_preferences (email_notification_address, is_email_notification_enabled, is_sms_notification_enabled, passcode, playback_order, recurring_notifications_enabled, sms_notification_address, transcribe, user_id) select email_address, email_notification_enabled, sms_notification_enabled, voicemail_pin, voicemail_playback_order, is_subscribed_to_paging, sms_address, is_subscribed_to_transcription, id from listen.SUBSCRIBER;"

#email time restrictions first
mysql -u root -e "insert into listen2.voicemail_preferences_time_restriction (voicemail_preferences_email_time_restrictions_id, time_restriction_id) select (select b.id from listen2.voicemail_preferences b where b.user_id = a.subscriber_id), a.id from listen.TIME_RESTRICTION a where a.action = 'NEW_VOICEMAIL_EMAIL';"

#then sms time restrictions
mysql -u root -e "insert into listen2.voicemail_preferences_time_restriction (voicemail_preferences_sms_time_restrictions_id, time_restriction_id) select (select b.id from listen2.voicemail_preferences b where b.user_id = a.subscriber_id), a.id from listen.TIME_RESTRICTION a where a.action = 'NEW_VOICEMAIL_SMS';"

echo "Migrating Find Me numbers"
mysql -u root -e "insert into listen2.find_me_number (id, version, dial_duration, is_enabled, number, priority, user_id) select id, version, dial_duration, enabled, number, priority, subscriber_id from listen.FIND_ME_NUMBER;"

mysql -u root -e "insert into listen2.find_me_preferences (expires, reminder_number, send_reminder, user_id) select find_me_expiration, find_me_reminder_destination, send_find_me_reminder, id from listen.SUBSCRIBER where id in (select user_id from listen2.find_me_number);"

echo "Migrating Android device registrations"
mysql -u root -e "insert into listen2.device_registration (id, version, device_id, device_type, registration_token, user_id) select id, version, device_id, device_type, registration_token, subscriber_id from listen.DEVICE_REGISTRATION;"

mysql -u root -e "insert into listen2.device_registration_registered_types (device_registration_id, registered_type) select registration_id, type from listen.DEVICE_REGISTRATION_TYPE;"

echo "Migrating histories"
mysql -u root -e "insert into listen2.action_history (action, by_user_id, channel, date_created, description, on_user_id, organization_id) select action, subscriber_id, channel, date, description, on_subscriber_id, 1 from listen.HISTORY where dtype = 'ActionHistory';"

#Call histories didn't store a second subscriber in the old db.  It also used direction to show which end of a call a subscriber was on.  Since we don't have direction anymore, we need to populate different columns for different call directions.
mysql -u root -e "insert into listen2.call_history (ani, date_time, dnis, duration, from_user_id, organization_id, to_user_id, result) select ani, date, dnis, duration, subscriber_id, 1, on_subscriber_id, '' from listen.HISTORY where dtype = 'CallDetailRecord' and direction = 'OUTBOUND';"

mysql -u root -e "insert into listen2.call_history (ani, date_time, dnis, duration, from_user_id, organization_id, to_user_id, result) select ani, date, dnis, duration, on_subscriber_id, 1, subscriber_id, '' from listen.HISTORY where dtype = 'CallDetailRecord' and direction = 'INBOUND';"

#Update action history actions to match what grails implementation is using
mysql -u root -e "update listen2.action_history set action = 'CHANGED_VOICEMAIL_PIN' where action = 'Changed voicemail PIN';"
mysql -u root -e "update listen2.action_history set action = 'CREATED_USER' where action = 'Created subscriber';"
mysql -u root -e "update listen2.action_history set action = 'DELETED_FINDMENUMBER' where action = 'Deleted find me number';"
mysql -u root -e "update listen2.action_history set action = 'DELETED_USER' where action = 'Deleted subscriber';"
mysql -u root -e "update listen2.action_history set action = 'DELETED_VOICEMAIL' where action = 'Deleted voicemail';"
mysql -u root -e "update listen2.action_history set action = 'DOWNLOADED_VOICEMAIL' where action = 'Downloaded voicemail';"
mysql -u root -e "update listen2.action_history set action = 'DROPPED_CONFERENCE_CALLER' where action = 'Dropped conference caller';"
mysql -u root -e "update listen2.action_history set action = 'FORWARDED_VOICEMAIL' where action = 'Forwarded voicemail';"
mysql -u root -e "update listen2.action_history set action = 'LEFT_VOICEMAIL' where action = 'Left voicemail';"
mysql -u root -e "update listen2.action_history set action = 'LISTENED_TO_VOICEMAIL' where action = 'Listened to voicemail';"
mysql -u root -e "update listen2.action_history set action = 'LOGGED_IN' where action = 'Logged in';"
mysql -u root -e "update listen2.action_history set action = 'LOGGED_OUT' where action = 'Logged out';"
mysql -u root -e "update listen2.action_history set action = 'MUTED_CONFERENCE_CALLER' where action = 'Muted conference caller';"
mysql -u root -e "update listen2.action_history set action = 'SENT_NEW_VOICEMAIL_EMAIL' where action = 'Sent new voicemail email';"
mysql -u root -e "update listen2.action_history set action = 'SENT_NEW_VOICEMAIL_SMS' where action = 'Sent new voicemail sms';"
mysql -u root -e "update listen2.action_history set action = 'SENT_NEW_VOICEMAIL_SMS' where action = 'Sent voicemail page';"
mysql -u root -e "update listen2.action_history set action = 'SENT_VOICEMAIL_ALTERNATE_NUMBER_PAGE' where action = 'Sent voicemail alternate number page';"
mysql -u root -e "update listen2.action_history set action = 'STARTED_CONFERENCE' where action = 'Started conference';"
mysql -u root -e "update listen2.action_history set action = 'STARTED_RECORDING_CONFERENCE' where action = 'Started recording conference';"
mysql -u root -e "update listen2.action_history set action = 'STOPPED_CONFERENCE' where action = 'Stopped conference';"
mysql -u root -e "update listen2.action_history set action = 'STOPPED_RECORDING_CONFERENCE' where action = 'Stopped recording conference';"
mysql -u root -e "update listen2.action_history set action = 'UNMUTED_CONFERENCE_CALLER' where action = 'Unmuted conference caller';"
mysql -u root -e "update listen2.action_history set action = 'UPDATED_FINDMENUMBER' where action = 'Updated find me number';"
mysql -u root -e "update listen2.action_history set action = 'CHANGED_PAGER_ALTERNATE_NUMBER' where action = 'Changed pager alternate number';"

echo "Migrating DNIS Mappings"
mysql -u root -e "insert into listen2.number_route (destination, label, organization_id, pattern, type) values ('Conferencing', 'Local and International #', 1, '4024203951', 'EXTERNAL'), ('IP PBX', NULL, 1, '4024768786', 'EXTERNAL'), ('Mailbox', NULL, 1, '4024203907', 'EXTERNAL'), ('Conferencing', 'Toll Free #', 1, '8002143520', 'EXTERNAL'), ('Direct Voicemail', NULL, 1, '4024203934', 'EXTERNAL'), ('Voicemail', NULL, 1, '40242039*', 'EXTERNAL'), ('Conferencing', 'In Office #', 1, '990', 'EXTERNAL'), ('Mailbox', NULL, 1, '770', 'EXTERNAL'), ('IP PBX', NULL, 1, '*', 'EXTERNAL'), ('Find Me Config', NULL, 1, '880', 'EXTERNAL');"

echo "Migrating Google Auth configurations"
mysql -u root -e "insert into listen2.google_auth_configuration (auth_token, auth_user, is_enabled, retry_timeout) values ('DQAAAMkAAACSYZRKTlhGFk82HsEKu67vP-tmO1HPGzvSehT_3XBpd8qOMWjYIkWfjB78DcSa3y0pYri0YnMGxDudCnZCN_rITRdbyXWXtQ4CbywfmlSfqp07b2BSgzRer5OCjdkcK8rMM9AWA2ClPXoeHkPbJdwtmgNX23wBBHERn79TotwamX4OC36Atj2JOrNrJjw6bc-rhrs0qEudAREd5O5Q4clwgQz-kSEW0kbwxtAPOTfppC9GFeUNhn6cWm3O9YA9Ja92XN4t5yoH9yNevg82RTj4', 'interactincorporated@gmail.com', true, 1000);"

echo "Adding After Hours configuration"
mysql -u root -e "insert into listen2.after_hours_configuration (organization_id, phone_number_id, realize_alert_name, realize_url, alternate_number) values (1, 7, 'Listen Pager Alert', 'http://thoth:8080/realize', (select property_value from listen.PROPERTY where property_key = 'com.interact.listen.alternateNumber'));"

echo "Adding Transcription configuration"
mysql -u root -e "insert into listen2.transcription_configuration (is_enabled, organization_id, phone_number) values (1, 1, '14026083919');"

echo "DONE"