databaseChangeLog = {

	changeSet(author: "root (generated)", id: "1307548968555-1") {
		createTable(tableName: "action") {
			column(autoIncrement: "true", name: "id", type: "bigint") {
				constraints(nullable: "false", primaryKey: "true", primaryKeyName: "action_pk")
			}

			column(name: "version", type: "bigint") {
				constraints(nullable: "false")
			}

			column(name: "keys_pressed", type: "varchar(255)")

			column(name: "prompt_before", type: "varchar(255)") {
				constraints(nullable: "false")
			}

			column(name: "class", type: "varchar(255)") {
				constraints(nullable: "false")
			}

			column(name: "destination_menu_group_name", type: "varchar(255)")

			column(name: "destination_menu_name", type: "varchar(255)")

			column(name: "number", type: "varchar(255)")

			column(name: "application_name", type: "varchar(255)")
		}
	}

	changeSet(author: "root (generated)", id: "1307548968555-2") {
		createTable(tableName: "action_history") {
			column(autoIncrement: "true", name: "id", type: "bigint") {
				constraints(nullable: "false", primaryKey: "true", primaryKeyName: "action_history_pk")
			}

			column(name: "version", type: "bigint") {
				constraints(nullable: "false")
			}

			column(name: "action", type: "varchar(255)") {
				constraints(nullable: "false")
			}

			column(name: "by_user_id", type: "bigint")

			column(name: "channel", type: "varchar(255)") {
				constraints(nullable: "false")
			}

			column(name: "date_created", type: "datetime") {
				constraints(nullable: "false")
			}

			column(name: "description", type: "varchar(255)") {
				constraints(nullable: "false")
			}

			column(name: "on_user_id", type: "bigint")

			column(name: "organization_id", type: "bigint")
		}
	}

	changeSet(author: "root (generated)", id: "1307548968555-3") {
		createTable(tableName: "after_hours_configuration") {
			column(autoIncrement: "true", name: "id", type: "bigint") {
				constraints(nullable: "false", primaryKey: "true", primaryKeyName: "after_hours_conf_pk")
			}

			column(name: "version", type: "bigint") {
				constraints(nullable: "false")
			}

			column(name: "alternate_number", type: "varchar(255)") {
				constraints(nullable: "false")
			}

			column(name: "organization_id", type: "bigint") {
				constraints(nullable: "false")
			}

			column(name: "phone_number_id", type: "bigint")

			column(name: "realize_alert_name", type: "varchar(255)") {
				constraints(nullable: "false")
			}

			column(name: "realize_url", type: "varchar(255)") {
				constraints(nullable: "false")
			}
		}
	}

	changeSet(author: "root (generated)", id: "1307548968555-4") {
		createTable(tableName: "audio") {
			column(autoIncrement: "true", name: "id", type: "bigint") {
				constraints(nullable: "false", primaryKey: "true", primaryKeyName: "audio_pk")
			}

			column(name: "version", type: "bigint") {
				constraints(nullable: "false")
			}

			column(name: "date_created", type: "datetime") {
				constraints(nullable: "false")
			}

			column(name: "description", type: "varchar(200)")

			column(name: "duration", type: "varchar(255)") {
				constraints(nullable: "false")
			}

			column(name: "file_size", type: "varchar(20)") {
				constraints(nullable: "false")
			}

			column(name: "last_updated", type: "datetime") {
				constraints(nullable: "false")
			}

			column(name: "transcription", type: "longtext") {
				constraints(nullable: "false")
			}

			column(name: "uri", type: "longtext") {
				constraints(nullable: "false")
			}
		}
	}

	changeSet(author: "root (generated)", id: "1307548968555-5") {
		createTable(tableName: "call_history") {
			column(autoIncrement: "true", name: "id", type: "bigint") {
				constraints(nullable: "false", primaryKey: "true", primaryKeyName: "call_history_pk")
			}

			column(name: "version", type: "bigint") {
				constraints(nullable: "false")
			}

			column(name: "ani", type: "varchar(255)") {
				constraints(nullable: "false")
			}

			column(name: "date_time", type: "datetime") {
				constraints(nullable: "false")
			}

			column(name: "dnis", type: "varchar(255)") {
				constraints(nullable: "false")
			}

			column(name: "duration", type: "varchar(255)") {
				constraints(nullable: "false")
			}

			column(name: "from_user_id", type: "bigint")

			column(name: "organization_id", type: "bigint") {
				constraints(nullable: "false")
			}

			column(name: "to_user_id", type: "bigint")
		}
	}

	changeSet(author: "root (generated)", id: "1307548968555-6") {
		createTable(tableName: "conference") {
			column(autoIncrement: "true", name: "id", type: "bigint") {
				constraints(nullable: "false", primaryKey: "true", primaryKeyName: "conference_pk")
			}

			column(name: "version", type: "bigint") {
				constraints(nullable: "false")
			}

			column(name: "arcade_id", type: "varchar(255)")

			column(name: "description", type: "varchar(100)") {
				constraints(nullable: "false")
			}

			column(name: "is_recording", type: "bit") {
				constraints(nullable: "false")
			}

			column(name: "is_started", type: "bit") {
				constraints(nullable: "false")
			}

			column(name: "owner_id", type: "bigint") {
				constraints(nullable: "false")
			}

			column(name: "recording_session_id", type: "varchar(255)")

			column(name: "start_time", type: "datetime")
		}
	}

	changeSet(author: "root (generated)", id: "1307548968555-7") {
		createTable(tableName: "find_me_number") {
			column(autoIncrement: "true", name: "id", type: "bigint") {
				constraints(nullable: "false", primaryKey: "true", primaryKeyName: "find_me_number_pk")
			}

			column(name: "version", type: "bigint") {
				constraints(nullable: "false")
			}

			column(name: "dial_duration", type: "integer") {
				constraints(nullable: "false")
			}

			column(name: "is_enabled", type: "bit") {
				constraints(nullable: "false")
			}

			column(name: "number", type: "varchar(50)") {
				constraints(nullable: "false")
			}

			column(name: "priority", type: "integer") {
				constraints(nullable: "false")
			}

			column(name: "user_id", type: "bigint") {
				constraints(nullable: "false")
			}
		}
	}

	changeSet(author: "root (generated)", id: "1307548968555-8") {
		createTable(tableName: "find_me_preferences") {
			column(autoIncrement: "true", name: "id", type: "bigint") {
				constraints(nullable: "false", primaryKey: "true", primaryKeyName: "find_me_prefs_pk")
			}

			column(name: "version", type: "bigint") {
				constraints(nullable: "false")
			}

			column(name: "expires", type: "datetime")

			column(name: "reminder_number", type: "varchar(255)")

			column(name: "send_reminder", type: "bit") {
				constraints(nullable: "false")
			}

			column(name: "user_id", type: "bigint") {
				constraints(nullable: "false")
			}
		}
	}

	changeSet(author: "root (generated)", id: "1307548968555-9") {
		createTable(tableName: "global_outdial_restriction") {
			column(autoIncrement: "true", name: "id", type: "bigint") {
				constraints(nullable: "false", primaryKey: "true", primaryKeyName: "global_outdial_pk")
			}

			column(name: "version", type: "bigint") {
				constraints(nullable: "false")
			}

			column(name: "pattern", type: "varchar(50)") {
				constraints(nullable: "false")
			}
		}
	}

	changeSet(author: "root (generated)", id: "1307548968555-10") {
		createTable(tableName: "menu") {
			column(autoIncrement: "true", name: "id", type: "bigint") {
				constraints(nullable: "false", primaryKey: "true", primaryKeyName: "menu_pk")
			}

			column(name: "version", type: "bigint") {
				constraints(nullable: "false")
			}

			column(name: "default_action_id", type: "bigint") {
				constraints(nullable: "false")
			}

			column(name: "is_entry", type: "bit") {
				constraints(nullable: "false")
			}

			column(name: "menu_group_id", type: "bigint") {
				constraints(nullable: "false")
			}

			column(name: "name", type: "varchar(50)") {
				constraints(nullable: "false")
			}

			column(name: "options_prompt", type: "varchar(255)") {
				constraints(nullable: "false")
			}

			column(name: "timeout_action_id", type: "bigint") {
				constraints(nullable: "false")
			}
		}
	}

	changeSet(author: "root (generated)", id: "1307548968555-11") {
		createTable(tableName: "menu_action") {
			column(name: "menu_keypress_actions_id", type: "bigint")

			column(name: "action_id", type: "bigint")
		}
	}

	changeSet(author: "root (generated)", id: "1307548968555-12") {
		createTable(tableName: "menu_group") {
			column(autoIncrement: "true", name: "id", type: "bigint") {
				constraints(nullable: "false", primaryKey: "true", primaryKeyName: "menu_group_pk")
			}

			column(name: "version", type: "bigint") {
				constraints(nullable: "false")
			}

			column(name: "is_default", type: "bit") {
				constraints(nullable: "false")
			}

			column(name: "name", type: "varchar(30)") {
				constraints(nullable: "false")
			}

			column(name: "organization_id", type: "bigint") {
				constraints(nullable: "false")
			}
		}
	}

	changeSet(author: "root (generated)", id: "1307548968555-13") {
		createTable(tableName: "menu_group_time_restriction") {
			column(name: "menu_group_restrictions_id", type: "bigint")

			column(name: "time_restriction_id", type: "bigint")
		}
	}

	changeSet(author: "root (generated)", id: "1307548968555-14") {
		createTable(tableName: "number_route") {
			column(autoIncrement: "true", name: "id", type: "bigint") {
				constraints(nullable: "false", primaryKey: "true", primaryKeyName: "number_route_pk")
			}

			column(name: "version", type: "bigint") {
				constraints(nullable: "false")
			}

			column(name: "destination", type: "varchar(100)") {
				constraints(nullable: "false")
			}

			column(name: "label", type: "varchar(50)")

			column(name: "organization_id", type: "bigint") {
				constraints(nullable: "false")
			}

			column(name: "pattern", type: "varchar(50)") {
				constraints(nullable: "false")
			}

			column(name: "type", type: "varchar(255)") {
				constraints(nullable: "false")
			}
		}
	}

	changeSet(author: "root (generated)", id: "1307548968555-15") {
		createTable(tableName: "organization") {
			column(autoIncrement: "true", name: "id", type: "bigint") {
				constraints(nullable: "false", primaryKey: "true", primaryKeyName: "organization_pk")
			}

			column(name: "version", type: "bigint") {
				constraints(nullable: "false")
			}

			column(name: "name", type: "varchar(100)") {
				constraints(nullable: "false", unique: "true")
			}
		}
	}

	changeSet(author: "root (generated)", id: "1307548968555-16") {
		createTable(tableName: "organization_enabled_features") {
			column(name: "organization_id", type: "bigint")

			column(name: "listen_feature", type: "varchar(255)")
		}
	}

	changeSet(author: "root (generated)", id: "1307548968555-17") {
		createTable(tableName: "outdial_restriction") {
			column(autoIncrement: "true", name: "id", type: "bigint") {
				constraints(nullable: "false", primaryKey: "true", primaryKeyName: "outdial_restriction_pk")
			}

			column(name: "version", type: "bigint") {
				constraints(nullable: "false")
			}

			column(name: "organization_id", type: "bigint") {
				constraints(nullable: "false")
			}

			column(name: "pattern", type: "varchar(50)") {
				constraints(nullable: "false")
			}

			column(name: "target_id", type: "bigint")
		}
	}

	changeSet(author: "root (generated)", id: "1307548968555-18") {
		createTable(tableName: "outdial_restriction_exception") {
			column(autoIncrement: "true", name: "id", type: "bigint") {
				constraints(nullable: "false", primaryKey: "true", primaryKeyName: "outdial_restriction_pk")
			}

			column(name: "version", type: "bigint") {
				constraints(nullable: "false")
			}

			column(name: "restriction_id", type: "bigint") {
				constraints(nullable: "false")
			}

			column(name: "target_id", type: "bigint") {
				constraints(nullable: "false")
			}
		}
	}

	changeSet(author: "root (generated)", id: "1307548968555-19") {
		createTable(tableName: "participant") {
			column(autoIncrement: "true", name: "id", type: "bigint") {
				constraints(nullable: "false", primaryKey: "true", primaryKeyName: "participant_pk")
			}

			column(name: "version", type: "bigint") {
				constraints(nullable: "false")
			}

			column(name: "ani", type: "varchar(50)") {
				constraints(nullable: "false")
			}

			column(name: "conference_id", type: "bigint") {
				constraints(nullable: "false")
			}

			column(name: "date_created", type: "datetime") {
				constraints(nullable: "false")
			}

			column(name: "is_admin", type: "bit") {
				constraints(nullable: "false")
			}

			column(name: "is_admin_muted", type: "bit") {
				constraints(nullable: "false")
			}

			column(name: "is_muted", type: "bit") {
				constraints(nullable: "false")
			}

			column(name: "is_passive", type: "bit") {
				constraints(nullable: "false")
			}

			column(name: "recorded_name_id", type: "bigint") {
				constraints(nullable: "false")
			}

			column(name: "session_id", type: "varchar(255)") {
				constraints(nullable: "false")
			}

			column(name: "user_id", type: "bigint")
		}
	}

	changeSet(author: "root (generated)", id: "1307548968555-20") {
		createTable(tableName: "phone_number") {
			column(autoIncrement: "true", name: "id", type: "bigint") {
				constraints(nullable: "false", primaryKey: "true", primaryKeyName: "phone_number_pk")
			}

			column(name: "version", type: "bigint") {
				constraints(nullable: "false")
			}

			column(name: "forwarded_to", type: "varchar(50)")

			column(name: "greeting_id", type: "bigint")

			column(name: "is_public", type: "bit") {
				constraints(nullable: "false")
			}

			column(name: "number", type: "varchar(50)") {
				constraints(nullable: "false")
			}

			column(name: "owner_id", type: "bigint") {
				constraints(nullable: "false")
			}

			column(name: "supports_message_light", type: "bit") {
				constraints(nullable: "false")
			}

			column(name: "type", type: "varchar(255)") {
				constraints(nullable: "false")
			}
		}
	}

	changeSet(author: "root (generated)", id: "1307548968555-21") {
		createTable(tableName: "pin") {
			column(autoIncrement: "true", name: "id", type: "bigint") {
				constraints(nullable: "false", primaryKey: "true", primaryKeyName: "pin_pk")
			}

			column(name: "version", type: "bigint") {
				constraints(nullable: "false")
			}

			column(name: "conference_id", type: "bigint") {
				constraints(nullable: "false")
			}

			column(name: "number", type: "varchar(20)") {
				constraints(nullable: "false")
			}

			column(name: "pin_type", type: "varchar(255)") {
				constraints(nullable: "false")
			}
		}
	}

	changeSet(author: "root (generated)", id: "1307548968555-22") {
		createTable(tableName: "recording") {
			column(autoIncrement: "true", name: "id", type: "bigint") {
				constraints(nullable: "false", primaryKey: "true", primaryKeyName: "recording_pk")
			}

			column(name: "version", type: "bigint") {
				constraints(nullable: "false")
			}

			column(name: "audio_id", type: "bigint") {
				constraints(nullable: "false")
			}

			column(name: "conference_id", type: "bigint") {
				constraints(nullable: "false")
			}
		}
	}

	changeSet(author: "root (generated)", id: "1307548968555-23") {
		createTable(tableName: "role") {
			column(autoIncrement: "true", name: "id", type: "bigint") {
				constraints(nullable: "false", primaryKey: "true", primaryKeyName: "role_pk")
			}

			column(name: "version", type: "bigint") {
				constraints(nullable: "false")
			}

			column(name: "authority", type: "varchar(255)") {
				constraints(nullable: "false", unique: "true")
			}
		}
	}

	changeSet(author: "root (generated)", id: "1307548968555-24") {
		createTable(tableName: "scheduled_conference") {
			column(autoIncrement: "true", name: "id", type: "bigint") {
				constraints(nullable: "false", primaryKey: "true", primaryKeyName: "scheduled_conference_pk")
			}

			column(name: "version", type: "bigint") {
				constraints(nullable: "false")
			}

			column(name: "active_caller_addresses", type: "longtext") {
				constraints(nullable: "false")
			}

			column(name: "date", type: "date") {
				constraints(nullable: "false")
			}

			column(name: "date_created", type: "datetime") {
				constraints(nullable: "false")
			}

			column(name: "email_body", type: "longtext") {
				constraints(nullable: "false")
			}

			column(name: "email_subject", type: "varchar(255)") {
				constraints(nullable: "false")
			}

			column(name: "ends", type: "varchar(255)") {
				constraints(nullable: "false")
			}

			column(name: "for_conference_id", type: "bigint") {
				constraints(nullable: "false")
			}

			column(name: "passive_caller_addresses", type: "longtext") {
				constraints(nullable: "false")
			}

			column(name: "scheduled_by_id", type: "bigint") {
				constraints(nullable: "false")
			}

			column(name: "starts", type: "varchar(255)") {
				constraints(nullable: "false")
			}
		}
	}

	changeSet(author: "root (generated)", id: "1307548968555-25") {
		createTable(tableName: "spot_system") {
			column(autoIncrement: "true", name: "id", type: "bigint") {
				constraints(nullable: "false", primaryKey: "true", primaryKeyName: "spot_system_pk")
			}

			column(name: "version", type: "bigint") {
				constraints(nullable: "false")
			}

			column(name: "name", type: "varchar(255)") {
				constraints(nullable: "false")
			}
		}
	}

	changeSet(author: "root (generated)", id: "1307548968555-26") {
		createTable(tableName: "time_restriction") {
			column(autoIncrement: "true", name: "id", type: "bigint") {
				constraints(nullable: "false", primaryKey: "true", primaryKeyName: "time_restriction_pk")
			}

			column(name: "version", type: "bigint") {
				constraints(nullable: "false")
			}

			column(name: "end_time", type: "varchar(255)") {
				constraints(nullable: "false")
			}

			column(name: "friday", type: "bit") {
				constraints(nullable: "false")
			}

			column(name: "monday", type: "bit") {
				constraints(nullable: "false")
			}

			column(name: "saturday", type: "bit") {
				constraints(nullable: "false")
			}

			column(name: "start_time", type: "varchar(255)") {
				constraints(nullable: "false")
			}

			column(name: "sunday", type: "bit") {
				constraints(nullable: "false")
			}

			column(name: "thursday", type: "bit") {
				constraints(nullable: "false")
			}

			column(name: "tuesday", type: "bit") {
				constraints(nullable: "false")
			}

			column(name: "wednesday", type: "bit") {
				constraints(nullable: "false")
			}
		}
	}

	changeSet(author: "root (generated)", id: "1307548968555-27") {
		createTable(tableName: "transcription_configuration") {
			column(autoIncrement: "true", name: "id", type: "bigint") {
				constraints(nullable: "false", primaryKey: "true", primaryKeyName: "transcription_pk")
			}

			column(name: "version", type: "bigint") {
				constraints(nullable: "false")
			}

			column(name: "is_enabled", type: "bit") {
				constraints(nullable: "false")
			}

			column(name: "organization_id", type: "bigint") {
				constraints(nullable: "false", unique: "true")
			}

			column(name: "phone_number", type: "varchar(255)") {
				constraints(nullable: "false")
			}
		}
	}

	changeSet(author: "root (generated)", id: "1307548968555-28") {
		createTable(tableName: "user") {
			column(autoIncrement: "true", name: "id", type: "bigint") {
				constraints(nullable: "false", primaryKey: "true", primaryKeyName: "user_pk")
			}

			column(name: "version", type: "bigint") {
				constraints(nullable: "false")
			}

			column(name: "account_expired", type: "bit") {
				constraints(nullable: "false")
			}

			column(name: "account_locked", type: "bit") {
				constraints(nullable: "false")
			}

			column(name: "email_address", type: "varchar(100)") {
				constraints(nullable: "false")
			}

			column(name: "enabled", type: "bit") {
				constraints(nullable: "false")
			}

			column(name: "last_login", type: "datetime")

			column(name: "organization_id", type: "bigint")

			column(name: "password", type: "varchar(255)") {
				constraints(nullable: "false")
			}

			column(name: "password_expired", type: "bit") {
				constraints(nullable: "false")
			}

			column(name: "real_name", type: "varchar(50)") {
				constraints(nullable: "false")
			}

			column(name: "username", type: "varchar(50)") {
				constraints(nullable: "false")
			}
		}
	}

	changeSet(author: "root (generated)", id: "1307548968555-29") {
		createTable(tableName: "user_role") {
			column(name: "role_id", type: "bigint") {
				constraints(nullable: "false")
			}

			column(name: "user_id", type: "bigint") {
				constraints(nullable: "false")
			}
		}
	}

	changeSet(author: "root (generated)", id: "1307548968555-30") {
		createTable(tableName: "voicemail") {
			column(autoIncrement: "true", name: "id", type: "bigint") {
				constraints(nullable: "false", primaryKey: "true", primaryKeyName: "voicemail_pk")
			}

			column(name: "version", type: "bigint") {
				constraints(nullable: "false")
			}

			column(name: "ani", type: "varchar(255)") {
				constraints(nullable: "false")
			}

			column(name: "audio_id", type: "bigint") {
				constraints(nullable: "false")
			}

			column(name: "date_created", type: "datetime") {
				constraints(nullable: "false")
			}

			column(name: "forwarded_by_id", type: "bigint")

			column(name: "is_new", type: "bit") {
				constraints(nullable: "false")
			}

			column(name: "left_by_id", type: "bigint")

			column(name: "owner_id", type: "bigint") {
				constraints(nullable: "false")
			}
		}
	}

	changeSet(author: "root (generated)", id: "1307548968555-31") {
		createTable(tableName: "voicemail_preferences") {
			column(autoIncrement: "true", name: "id", type: "bigint") {
				constraints(nullable: "false", primaryKey: "true", primaryKeyName: "voicemail_preferences_pk")
			}

			column(name: "version", type: "bigint") {
				constraints(nullable: "false")
			}

			column(name: "email_notification_address", type: "varchar(255)")

			column(name: "is_email_notification_enabled", type: "bit") {
				constraints(nullable: "false")
			}

			column(name: "is_sms_notification_enabled", type: "bit") {
				constraints(nullable: "false")
			}

			column(name: "passcode", type: "varchar(20)") {
				constraints(nullable: "false")
			}

			column(name: "playback_order", type: "varchar(255)") {
				constraints(nullable: "false")
			}

			column(name: "recurring_notifications_enabled", type: "bit") {
				constraints(nullable: "false")
			}

			column(name: "sms_notification_address", type: "varchar(255)")

			column(name: "transcribe", type: "bit") {
				constraints(nullable: "false")
			}

			column(name: "user_id", type: "bigint") {
				constraints(nullable: "false")
			}
		}
	}

	changeSet(author: "root (generated)", id: "1307548968555-32") {
		createTable(tableName: "voicemail_preferences_time_restriction") {
			column(name: "voicemail_preferences_email_time_restrictions_id", type: "bigint")

			column(name: "time_restriction_id", type: "bigint")

			column(name: "voicemail_preferences_sms_time_restrictions_id", type: "bigint")
		}
	}

	changeSet(author: "root (generated)", id: "1307548968555-33") {
		addPrimaryKey(columnNames: "role_id, user_id", constraintName: "user_role_pk", tableName: "user_role")
	}

	changeSet(author: "root (generated)", id: "1307548968555-34") {
		createIndex(indexName: "FKC510738B26425C6E", tableName: "action_history") {
			column(name: "by_user_id")
		}
	}

	changeSet(author: "root (generated)", id: "1307548968555-35") {
		createIndex(indexName: "FKC510738B2F0C55F6", tableName: "action_history") {
			column(name: "on_user_id")
		}
	}

	changeSet(author: "root (generated)", id: "1307548968555-36") {
		createIndex(indexName: "FKC510738B56D05B56", tableName: "action_history") {
			column(name: "organization_id")
		}
	}

	changeSet(author: "root (generated)", id: "1307548968555-37") {
		createIndex(indexName: "FK7C7614E356D05B56", tableName: "after_hours_configuration") {
			column(name: "organization_id")
		}
	}

	changeSet(author: "root (generated)", id: "1307548968555-38") {
		createIndex(indexName: "FK7C7614E362B74C9B", tableName: "after_hours_configuration") {
			column(name: "phone_number_id")
		}
	}

	changeSet(author: "root (generated)", id: "1307548968555-39") {
		createIndex(indexName: "FKC404C7B356D05B56", tableName: "call_history") {
			column(name: "organization_id")
		}
	}

	changeSet(author: "root (generated)", id: "1307548968555-40") {
		createIndex(indexName: "FKC404C7B388BFFA92", tableName: "call_history") {
			column(name: "to_user_id")
		}
	}

	changeSet(author: "root (generated)", id: "1307548968555-41") {
		createIndex(indexName: "FKC404C7B3F96764C1", tableName: "call_history") {
			column(name: "from_user_id")
		}
	}

	changeSet(author: "root (generated)", id: "1307548968555-42") {
		createIndex(indexName: "FK2B5F451C6D23A06E", tableName: "conference") {
			column(name: "owner_id")
		}
	}

	changeSet(author: "root (generated)", id: "1307548968555-43") {
		createIndex(indexName: "FK71CAACCA13CF056", tableName: "find_me_number") {
			column(name: "user_id")
		}
	}

	changeSet(author: "root (generated)", id: "1307548968555-44") {
		createIndex(indexName: "FK46C4D61713CF056", tableName: "find_me_preferences") {
			column(name: "user_id")
		}
	}

	changeSet(author: "root (generated)", id: "1307548968555-45") {
		createIndex(indexName: "FK33155F2A28807E", tableName: "menu") {
			column(name: "menu_group_id")
		}
	}

	changeSet(author: "root (generated)", id: "1307548968555-46") {
		createIndex(indexName: "FK33155F414E9077", tableName: "menu") {
			column(name: "timeout_action_id")
		}
	}

	changeSet(author: "root (generated)", id: "1307548968555-47") {
		createIndex(indexName: "FK33155F5C58157", tableName: "menu") {
			column(name: "default_action_id")
		}
	}

	changeSet(author: "root (generated)", id: "1307548968555-48") {
		createIndex(indexName: "unique-name", tableName: "menu") {
			column(name: "menu_group_id")

			column(name: "name")
		}
	}

	changeSet(author: "root (generated)", id: "1307548968555-49") {
		createIndex(indexName: "FK424A6D162E667B4", tableName: "menu_action") {
			column(name: "menu_keypress_actions_id")
		}
	}

	changeSet(author: "root (generated)", id: "1307548968555-50") {
		createIndex(indexName: "FK424A6D16C2B2CDD5", tableName: "menu_action") {
			column(name: "action_id")
		}
	}

	changeSet(author: "root (generated)", id: "1307548968555-51") {
		createIndex(indexName: "FKFA3CAB9F56D05B56", tableName: "menu_group") {
			column(name: "organization_id")
		}
	}

	changeSet(author: "root (generated)", id: "1307548968555-52") {
		createIndex(indexName: "FK910B81FA382B3C56", tableName: "menu_group_time_restriction") {
			column(name: "menu_group_restrictions_id")
		}
	}

	changeSet(author: "root (generated)", id: "1307548968555-53") {
		createIndex(indexName: "FK910B81FA6B8BA9DE", tableName: "menu_group_time_restriction") {
			column(name: "time_restriction_id")
		}
	}

	changeSet(author: "root (generated)", id: "1307548968555-54") {
		createIndex(indexName: "FK534695D356D05B56", tableName: "number_route") {
			column(name: "organization_id")
		}
	}

	changeSet(author: "root (generated)", id: "1307548968555-55") {
		createIndex(indexName: "unique-label", tableName: "number_route") {
			column(name: "type")

			column(name: "organization_id")

			column(name: "label")
		}
	}

	changeSet(author: "root (generated)", id: "1307548968555-56") {
		createIndex(indexName: "unique-pattern", tableName: "number_route") {
			column(name: "type")

			column(name: "organization_id")

			column(name: "pattern")
		}
	}

	changeSet(author: "root (generated)", id: "1307548968555-57") {
		createIndex(indexName: "name_unique_1307548966223", tableName: "organization", unique: "true") {
			column(name: "name")
		}
	}

	changeSet(author: "root (generated)", id: "1307548968555-58") {
		createIndex(indexName: "FK8CA0C50756D05B56", tableName: "organization_enabled_features") {
			column(name: "organization_id")
		}
	}

	changeSet(author: "root (generated)", id: "1307548968555-59") {
		createIndex(indexName: "FK3AAF28CB56D05B56", tableName: "outdial_restriction") {
			column(name: "organization_id")
		}
	}

	changeSet(author: "root (generated)", id: "1307548968555-60") {
		createIndex(indexName: "FK3AAF28CBD9654CD0", tableName: "outdial_restriction") {
			column(name: "target_id")
		}
	}

	changeSet(author: "root (generated)", id: "1307548968555-61") {
		createIndex(indexName: "FKB8C7B1DBAF1F7D18", tableName: "outdial_restriction_exception") {
			column(name: "restriction_id")
		}
	}

	changeSet(author: "root (generated)", id: "1307548968555-62") {
		createIndex(indexName: "FKB8C7B1DBD9654CD0", tableName: "outdial_restriction_exception") {
			column(name: "target_id")
		}
	}

	changeSet(author: "root (generated)", id: "1307548968555-63") {
		createIndex(indexName: "FK2DBDEF3313CF056", tableName: "participant") {
			column(name: "user_id")
		}
	}

	changeSet(author: "root (generated)", id: "1307548968555-64") {
		createIndex(indexName: "FK2DBDEF337B2692B3", tableName: "participant") {
			column(name: "conference_id")
		}
	}

	changeSet(author: "root (generated)", id: "1307548968555-65") {
		createIndex(indexName: "FK2DBDEF33B498DB9A", tableName: "participant") {
			column(name: "recorded_name_id")
		}
	}

	changeSet(author: "root (generated)", id: "1307548968555-66") {
		createIndex(indexName: "participant_ani_uk", tableName: "participant") {
			column(name: "conference_id")

			column(name: "ani")
		}
	}

	changeSet(author: "root (generated)", id: "1307548968555-67") {
		createIndex(indexName: "FKDB80433A12722FBB", tableName: "phone_number") {
			column(name: "greeting_id")
		}
	}

	changeSet(author: "root (generated)", id: "1307548968555-68") {
		createIndex(indexName: "FKDB80433A6D23A06E", tableName: "phone_number") {
			column(name: "owner_id")
		}
	}

	changeSet(author: "root (generated)", id: "1307548968555-69") {
		createIndex(indexName: "FK1B1957B2692B3", tableName: "pin") {
			column(name: "conference_id")
		}
	}

	changeSet(author: "root (generated)", id: "1307548968555-70") {
		createIndex(indexName: "FK3B387DF16F5092FE", tableName: "recording") {
			column(name: "audio_id")
		}
	}

	changeSet(author: "root (generated)", id: "1307548968555-71") {
		createIndex(indexName: "FK3B387DF17B2692B3", tableName: "recording") {
			column(name: "conference_id")
		}
	}

	changeSet(author: "root (generated)", id: "1307548968555-72") {
		createIndex(indexName: "authority_unique_1307548966258", tableName: "role", unique: "true") {
			column(name: "authority")
		}
	}

	changeSet(author: "root (generated)", id: "1307548968555-73") {
		createIndex(indexName: "FK44069BEE42175DBD", tableName: "scheduled_conference") {
			column(name: "for_conference_id")
		}
	}

	changeSet(author: "root (generated)", id: "1307548968555-74") {
		createIndex(indexName: "FK44069BEE57EDA238", tableName: "scheduled_conference") {
			column(name: "scheduled_by_id")
		}
	}

	changeSet(author: "root (generated)", id: "1307548968555-75") {
		createIndex(indexName: "FKF351DC0956D05B56", tableName: "transcription_configuration") {
			column(name: "organization_id")
		}
	}

	changeSet(author: "root (generated)", id: "1307548968555-76") {
		createIndex(indexName: "organization_id_unique_1307548966277", tableName: "transcription_configuration", unique: "true") {
			column(name: "organization_id")
		}
	}

	changeSet(author: "root (generated)", id: "1307548968555-77") {
		createIndex(indexName: "FK36EBCB56D05B56", tableName: "user") {
			column(name: "organization_id")
		}
	}

	changeSet(author: "root (generated)", id: "1307548968555-78") {
		createIndex(indexName: "unique-username", tableName: "user") {
			column(name: "organization_id")

			column(name: "username")
		}
	}

	changeSet(author: "root (generated)", id: "1307548968555-79") {
		createIndex(indexName: "FK143BF46A13CF056", tableName: "user_role") {
			column(name: "user_id")
		}
	}

	changeSet(author: "root (generated)", id: "1307548968555-80") {
		createIndex(indexName: "FK143BF46A5C122C76", tableName: "user_role") {
			column(name: "role_id")
		}
	}

	changeSet(author: "root (generated)", id: "1307548968555-81") {
		createIndex(indexName: "FKC34DFDE96D23A06E", tableName: "voicemail") {
			column(name: "owner_id")
		}
	}

	changeSet(author: "root (generated)", id: "1307548968555-82") {
		createIndex(indexName: "FKC34DFDE96F5092FE", tableName: "voicemail") {
			column(name: "audio_id")
		}
	}

	changeSet(author: "root (generated)", id: "1307548968555-83") {
		createIndex(indexName: "FKC34DFDE99BC21612", tableName: "voicemail") {
			column(name: "left_by_id")
		}
	}

	changeSet(author: "root (generated)", id: "1307548968555-84") {
		createIndex(indexName: "FKC34DFDE9FC1CAE6F", tableName: "voicemail") {
			column(name: "forwarded_by_id")
		}
	}

	changeSet(author: "root (generated)", id: "1307548968555-85") {
		createIndex(indexName: "FK1C02A5A213CF056", tableName: "voicemail_preferences") {
			column(name: "user_id")
		}
	}

	changeSet(author: "root (generated)", id: "1307548968555-86") {
		createIndex(indexName: "FK566E6E576B285DF1", tableName: "voicemail_preferences_time_restriction") {
			column(name: "voicemail_preferences_email_time_restrictions_id")
		}
	}

	changeSet(author: "root (generated)", id: "1307548968555-87") {
		createIndex(indexName: "FK566E6E576B8BA9DE", tableName: "voicemail_preferences_time_restriction") {
			column(name: "time_restriction_id")
		}
	}

	changeSet(author: "root (generated)", id: "1307548968555-88") {
		createIndex(indexName: "FK566E6E5776CE5F94", tableName: "voicemail_preferences_time_restriction") {
			column(name: "voicemail_preferences_sms_time_restrictions_id")
		}
	}

	changeSet(author: "root (generated)", id: "1307548968555-89") {
		addForeignKeyConstraint(baseColumnNames: "by_user_id", baseTableName: "action_history", constraintName: "action_history_by_user_id_fk", deferrable: "false", initiallyDeferred: "false", referencedColumnNames: "id", referencedTableName: "user", referencesUniqueColumn: "false")
	}

	changeSet(author: "root (generated)", id: "1307548968555-90") {
		addForeignKeyConstraint(baseColumnNames: "on_user_id", baseTableName: "action_history", constraintName: "action_history_on_user_id_fk", deferrable: "false", initiallyDeferred: "false", referencedColumnNames: "id", referencedTableName: "user", referencesUniqueColumn: "false")
	}

	changeSet(author: "root (generated)", id: "1307548968555-91") {
		addForeignKeyConstraint(baseColumnNames: "organization_id", baseTableName: "action_history", constraintName: "action_history_organizati_fk", deferrable: "false", initiallyDeferred: "false", referencedColumnNames: "id", referencedTableName: "organization", referencesUniqueColumn: "false")
	}

	changeSet(author: "root (generated)", id: "1307548968555-92") {
		addForeignKeyConstraint(baseColumnNames: "organization_id", baseTableName: "after_hours_configuration", constraintName: "after_hours_conf_organiza_fk", deferrable: "false", initiallyDeferred: "false", referencedColumnNames: "id", referencedTableName: "organization", referencesUniqueColumn: "false")
	}

	changeSet(author: "root (generated)", id: "1307548968555-93") {
		addForeignKeyConstraint(baseColumnNames: "phone_number_id", baseTableName: "after_hours_configuration", constraintName: "after_hours_conf_phone_nu_fk", deferrable: "false", initiallyDeferred: "false", referencedColumnNames: "id", referencedTableName: "phone_number", referencesUniqueColumn: "false")
	}

	changeSet(author: "root (generated)", id: "1307548968555-94") {
		addForeignKeyConstraint(baseColumnNames: "from_user_id", baseTableName: "call_history", constraintName: "call_history_from_user__fk", deferrable: "false", initiallyDeferred: "false", referencedColumnNames: "id", referencedTableName: "user", referencesUniqueColumn: "false")
	}

	changeSet(author: "root (generated)", id: "1307548968555-95") {
		addForeignKeyConstraint(baseColumnNames: "organization_id", baseTableName: "call_history", constraintName: "call_history_organizati_fk", deferrable: "false", initiallyDeferred: "false", referencedColumnNames: "id", referencedTableName: "organization", referencesUniqueColumn: "false")
	}

	changeSet(author: "root (generated)", id: "1307548968555-96") {
		addForeignKeyConstraint(baseColumnNames: "to_user_id", baseTableName: "call_history", constraintName: "call_history_to_user_id_fk", deferrable: "false", initiallyDeferred: "false", referencedColumnNames: "id", referencedTableName: "user", referencesUniqueColumn: "false")
	}

	changeSet(author: "root (generated)", id: "1307548968555-97") {
		addForeignKeyConstraint(baseColumnNames: "owner_id", baseTableName: "conference", constraintName: "conference_owner_id_fk", deferrable: "false", initiallyDeferred: "false", referencedColumnNames: "id", referencedTableName: "user", referencesUniqueColumn: "false")
	}

	changeSet(author: "root (generated)", id: "1307548968555-98") {
		addForeignKeyConstraint(baseColumnNames: "user_id", baseTableName: "find_me_number", constraintName: "find_me_number_user_id_fk", deferrable: "false", initiallyDeferred: "false", referencedColumnNames: "id", referencedTableName: "user", referencesUniqueColumn: "false")
	}

	changeSet(author: "root (generated)", id: "1307548968555-99") {
		addForeignKeyConstraint(baseColumnNames: "user_id", baseTableName: "find_me_preferences", constraintName: "find_me_preferenc_user_id_fk", deferrable: "false", initiallyDeferred: "false", referencedColumnNames: "id", referencedTableName: "user", referencesUniqueColumn: "false")
	}

	changeSet(author: "root (generated)", id: "1307548968555-100") {
		addForeignKeyConstraint(baseColumnNames: "default_action_id", baseTableName: "menu", constraintName: "menu_default_ac_fk", deferrable: "false", initiallyDeferred: "false", referencedColumnNames: "id", referencedTableName: "action", referencesUniqueColumn: "false")
	}

	changeSet(author: "root (generated)", id: "1307548968555-101") {
		addForeignKeyConstraint(baseColumnNames: "menu_group_id", baseTableName: "menu", constraintName: "menu_menu_group_fk", deferrable: "false", initiallyDeferred: "false", referencedColumnNames: "id", referencedTableName: "menu_group", referencesUniqueColumn: "false")
	}

	changeSet(author: "root (generated)", id: "1307548968555-102") {
		addForeignKeyConstraint(baseColumnNames: "timeout_action_id", baseTableName: "menu", constraintName: "menu_timeout_ac_fk", deferrable: "false", initiallyDeferred: "false", referencedColumnNames: "id", referencedTableName: "action", referencesUniqueColumn: "false")
	}

	changeSet(author: "root (generated)", id: "1307548968555-103") {
		addForeignKeyConstraint(baseColumnNames: "action_id", baseTableName: "menu_action", constraintName: "menu_action_action_id_fk", deferrable: "false", initiallyDeferred: "false", referencedColumnNames: "id", referencedTableName: "action", referencesUniqueColumn: "false")
	}

	changeSet(author: "root (generated)", id: "1307548968555-104") {
		addForeignKeyConstraint(baseColumnNames: "menu_keypress_actions_id", baseTableName: "menu_action", constraintName: "menu_action_menu_keypress_fk", deferrable: "false", initiallyDeferred: "false", referencedColumnNames: "id", referencedTableName: "menu", referencesUniqueColumn: "false")
	}

	changeSet(author: "root (generated)", id: "1307548968555-105") {
		addForeignKeyConstraint(baseColumnNames: "organization_id", baseTableName: "menu_group", constraintName: "menu_group_organization_fk", deferrable: "false", initiallyDeferred: "false", referencedColumnNames: "id", referencedTableName: "organization", referencesUniqueColumn: "false")
	}

	changeSet(author: "root (generated)", id: "1307548968555-106") {
		addForeignKeyConstraint(baseColumnNames: "menu_group_restrictions_id", baseTableName: "menu_group_time_restriction", constraintName: "menu_group_time_menu_grou_fk", deferrable: "false", initiallyDeferred: "false", referencedColumnNames: "id", referencedTableName: "menu_group", referencesUniqueColumn: "false")
	}

	changeSet(author: "root (generated)", id: "1307548968555-107") {
		addForeignKeyConstraint(baseColumnNames: "time_restriction_id", baseTableName: "menu_group_time_restriction", constraintName: "menu_group_time_time_rest_fk", deferrable: "false", initiallyDeferred: "false", referencedColumnNames: "id", referencedTableName: "time_restriction", referencesUniqueColumn: "false")
	}

	changeSet(author: "root (generated)", id: "1307548968555-108") {
		addForeignKeyConstraint(baseColumnNames: "organization_id", baseTableName: "number_route", constraintName: "number_route_organizati_fk", deferrable: "false", initiallyDeferred: "false", referencedColumnNames: "id", referencedTableName: "organization", referencesUniqueColumn: "false")
	}

	changeSet(author: "root (generated)", id: "1307548968555-109") {
		addForeignKeyConstraint(baseColumnNames: "organization_id", baseTableName: "organization_enabled_features", constraintName: "organization_en_organizat_fk", deferrable: "false", initiallyDeferred: "false", referencedColumnNames: "id", referencedTableName: "organization", referencesUniqueColumn: "false")
	}

	changeSet(author: "root (generated)", id: "1307548968555-110") {
		addForeignKeyConstraint(baseColumnNames: "organization_id", baseTableName: "outdial_restriction", constraintName: "outdial_restrict_organizati_fk", deferrable: "false", initiallyDeferred: "false", referencedColumnNames: "id", referencedTableName: "organization", referencesUniqueColumn: "false")
	}

	changeSet(author: "root (generated)", id: "1307548968555-111") {
		addForeignKeyConstraint(baseColumnNames: "target_id", baseTableName: "outdial_restriction", constraintName: "outdial_restrict_target_id_fk", deferrable: "false", initiallyDeferred: "false", referencedColumnNames: "id", referencedTableName: "user", referencesUniqueColumn: "false")
	}

	changeSet(author: "root (generated)", id: "1307548968555-112") {
		addForeignKeyConstraint(baseColumnNames: "restriction_id", baseTableName: "outdial_restriction_exception", constraintName: "outdial_restric2_restrictio_fk", deferrable: "false", initiallyDeferred: "false", referencedColumnNames: "id", referencedTableName: "outdial_restriction", referencesUniqueColumn: "false")
	}

	changeSet(author: "root (generated)", id: "1307548968555-113") {
		addForeignKeyConstraint(baseColumnNames: "target_id", baseTableName: "outdial_restriction_exception", constraintName: "outdial_restric2_target_id_fk", deferrable: "false", initiallyDeferred: "false", referencedColumnNames: "id", referencedTableName: "user", referencesUniqueColumn: "false")
	}

	changeSet(author: "root (generated)", id: "1307548968555-114") {
		addForeignKeyConstraint(baseColumnNames: "conference_id", baseTableName: "participant", constraintName: "participant_conference_fk", deferrable: "false", initiallyDeferred: "false", referencedColumnNames: "id", referencedTableName: "conference", referencesUniqueColumn: "false")
	}

	changeSet(author: "root (generated)", id: "1307548968555-115") {
		addForeignKeyConstraint(baseColumnNames: "recorded_name_id", baseTableName: "participant", constraintName: "participant_recorded_name_fk", deferrable: "false", initiallyDeferred: "false", referencedColumnNames: "id", referencedTableName: "audio", referencesUniqueColumn: "false")
	}

	changeSet(author: "root (generated)", id: "1307548968555-116") {
		addForeignKeyConstraint(baseColumnNames: "user_id", baseTableName: "participant", constraintName: "participant_user_id_fk", deferrable: "false", initiallyDeferred: "false", referencedColumnNames: "id", referencedTableName: "user", referencesUniqueColumn: "false")
	}

	changeSet(author: "root (generated)", id: "1307548968555-117") {
		addForeignKeyConstraint(baseColumnNames: "greeting_id", baseTableName: "phone_number", constraintName: "phone_number_greeting_i_fk", deferrable: "false", initiallyDeferred: "false", referencedColumnNames: "id", referencedTableName: "audio", referencesUniqueColumn: "false")
	}

	changeSet(author: "root (generated)", id: "1307548968555-118") {
		addForeignKeyConstraint(baseColumnNames: "owner_id", baseTableName: "phone_number", constraintName: "phone_number_owner_id_fk", deferrable: "false", initiallyDeferred: "false", referencedColumnNames: "id", referencedTableName: "user", referencesUniqueColumn: "false")
	}

	changeSet(author: "root (generated)", id: "1307548968555-119") {
		addForeignKeyConstraint(baseColumnNames: "conference_id", baseTableName: "pin", constraintName: "pin_conference_fk", deferrable: "false", initiallyDeferred: "false", referencedColumnNames: "id", referencedTableName: "conference", referencesUniqueColumn: "false")
	}

	changeSet(author: "root (generated)", id: "1307548968555-120") {
		addForeignKeyConstraint(baseColumnNames: "audio_id", baseTableName: "recording", constraintName: "recording_audio_id_fk", deferrable: "false", initiallyDeferred: "false", referencedColumnNames: "id", referencedTableName: "audio", referencesUniqueColumn: "false")
	}

	changeSet(author: "root (generated)", id: "1307548968555-121") {
		addForeignKeyConstraint(baseColumnNames: "conference_id", baseTableName: "recording", constraintName: "recording_conference_id_fk", deferrable: "false", initiallyDeferred: "false", referencedColumnNames: "id", referencedTableName: "conference", referencesUniqueColumn: "false")
	}

	changeSet(author: "root (generated)", id: "1307548968555-122") {
		addForeignKeyConstraint(baseColumnNames: "for_conference_id", baseTableName: "scheduled_conference", constraintName: "scheduled_conf_for_confer_fk", deferrable: "false", initiallyDeferred: "false", referencedColumnNames: "id", referencedTableName: "conference", referencesUniqueColumn: "false")
	}

	changeSet(author: "root (generated)", id: "1307548968555-123") {
		addForeignKeyConstraint(baseColumnNames: "scheduled_by_id", baseTableName: "scheduled_conference", constraintName: "scheduled_conf_scheduled_b_fk", deferrable: "false", initiallyDeferred: "false", referencedColumnNames: "id", referencedTableName: "user", referencesUniqueColumn: "false")
	}

	changeSet(author: "root (generated)", id: "1307548968555-124") {
		addForeignKeyConstraint(baseColumnNames: "organization_id", baseTableName: "transcription_configuration", constraintName: "transcription_c_organizat_fk", deferrable: "false", initiallyDeferred: "false", referencedColumnNames: "id", referencedTableName: "organization", referencesUniqueColumn: "false")
	}

	changeSet(author: "root (generated)", id: "1307548968555-125") {
		addForeignKeyConstraint(baseColumnNames: "organization_id", baseTableName: "user", constraintName: "user_organizati_fk", deferrable: "false", initiallyDeferred: "false", referencedColumnNames: "id", referencedTableName: "organization", referencesUniqueColumn: "false")
	}

	changeSet(author: "root (generated)", id: "1307548968555-126") {
		addForeignKeyConstraint(baseColumnNames: "role_id", baseTableName: "user_role", constraintName: "user_role_role_id_fk", deferrable: "false", initiallyDeferred: "false", referencedColumnNames: "id", referencedTableName: "role", referencesUniqueColumn: "false")
	}

	changeSet(author: "root (generated)", id: "1307548968555-127") {
		addForeignKeyConstraint(baseColumnNames: "user_id", baseTableName: "user_role", constraintName: "user_role_user_id_fk", deferrable: "false", initiallyDeferred: "false", referencedColumnNames: "id", referencedTableName: "user", referencesUniqueColumn: "false")
	}

	changeSet(author: "root (generated)", id: "1307548968555-128") {
		addForeignKeyConstraint(baseColumnNames: "audio_id", baseTableName: "voicemail", constraintName: "voicemail_audio_id_fk", deferrable: "false", initiallyDeferred: "false", referencedColumnNames: "id", referencedTableName: "audio", referencesUniqueColumn: "false")
	}

	changeSet(author: "root (generated)", id: "1307548968555-129") {
		addForeignKeyConstraint(baseColumnNames: "forwarded_by_id", baseTableName: "voicemail", constraintName: "voicemail_forwarded__fk", deferrable: "false", initiallyDeferred: "false", referencedColumnNames: "id", referencedTableName: "user", referencesUniqueColumn: "false")
	}

	changeSet(author: "root (generated)", id: "1307548968555-130") {
		addForeignKeyConstraint(baseColumnNames: "left_by_id", baseTableName: "voicemail", constraintName: "voicemail_left_by_id_fk", deferrable: "false", initiallyDeferred: "false", referencedColumnNames: "id", referencedTableName: "user", referencesUniqueColumn: "false")
	}

	changeSet(author: "root (generated)", id: "1307548968555-131") {
		addForeignKeyConstraint(baseColumnNames: "owner_id", baseTableName: "voicemail", constraintName: "voicemail_owner_id_fk", deferrable: "false", initiallyDeferred: "false", referencedColumnNames: "id", referencedTableName: "user", referencesUniqueColumn: "false")
	}

	changeSet(author: "root (generated)", id: "1307548968555-132") {
		addForeignKeyConstraint(baseColumnNames: "user_id", baseTableName: "voicemail_preferences", constraintName: "voicemail_prefer_user_id_fk", deferrable: "false", initiallyDeferred: "false", referencedColumnNames: "id", referencedTableName: "user", referencesUniqueColumn: "false")
	}

	changeSet(author: "root (generated)", id: "1307548968555-133") {
		addForeignKeyConstraint(baseColumnNames: "time_restriction_id", baseTableName: "voicemail_preferences_time_restriction", constraintName: "voicemail_prefe2_time_restr_fk", deferrable: "false", initiallyDeferred: "false", referencedColumnNames: "id", referencedTableName: "time_restriction", referencesUniqueColumn: "false")
	}

	changeSet(author: "root (generated)", id: "1307548968555-134") {
		addForeignKeyConstraint(baseColumnNames: "voicemail_preferences_email_time_restrictions_id", baseTableName: "voicemail_preferences_time_restriction", constraintName: "voicemail_prefe2_email_time_fk", deferrable: "false", initiallyDeferred: "false", referencedColumnNames: "id", referencedTableName: "voicemail_preferences", referencesUniqueColumn: "false")
	}

	changeSet(author: "root (generated)", id: "1307548968555-135") {
		addForeignKeyConstraint(baseColumnNames: "voicemail_preferences_sms_time_restrictions_id", baseTableName: "voicemail_preferences_time_restriction", constraintName: "voicemail_prefe2_sms_time_r_fk", deferrable: "false", initiallyDeferred: "false", referencedColumnNames: "id", referencedTableName: "voicemail_preferences", referencesUniqueColumn: "false")
	}
    
    changeSet(author: "root (generated)", id: "1307574571654-1") {
        createTable(tableName: "device_registration") {
            column(autoIncrement: "true", name: "id", type: "bigint") {
                constraints(nullable: "false", primaryKey: "true", primaryKeyName: "device_regist_pk")
            }
            
            column(name: "version", type: "bigint") {
                                constraints(nullable: "false")
            }

            column(name: "device_id", type: "varchar(255)") {
                constraints(nullable: "false")
            }

            column(name: "device_type", type: "varchar(255)") {
                constraints(nullable: "false")
            }
            
            column(name: "registration_token", type: "varchar(255)") {
                constraints(nullable: "false")
            }
            
            column(name: "user_id", type: "bigint") {
                constraints(nullable: "false")
            }
        }
    }
    
    changeSet(author: "root (generated)", id: "1307574571654-2") {
        createTable(tableName: "device_registration_registered_types") {
            column(name: "device_registration_id", type: "bigint")

            column(name: "registered_type", type: "varchar(255)")
        }
    }

    changeSet(author: "root (generated)", id: "1307574571654-3") {
        createTable(tableName: "google_auth_configuration") {
            column(autoIncrement: "true", name: "id", type: "bigint") {
                constraints(nullable: "false", primaryKey: "true", primaryKeyName: "google_auth_c_pk")
            }
            
            column(name: "version", type: "bigint") {
                constraints(nullable: "false")
            }
            
            column(name: "auth_pass", type: "varchar(255)") {
                constraints(nullable: "false")
            }
            
            column(name: "auth_token", type: "longtext") {
                constraints(nullable: "false")
            }
            
            column(name: "auth_user", type: "varchar(255)") {
                constraints(nullable: "false")
            }
            
            column(name: "is_enabled", type: "bit") {
                constraints(nullable: "false")
            }
            
            column(name: "last_error", type: "varchar(255)")
            
            column(name: "next_retry", type: "datetime")
            
            column(name: "retry_timeout", type: "bigint") {
                constraints(nullable: "false")
            }
        }
    }

    changeSet(author: "root (generated)", id: "1307574571654-5") {
        createIndex(indexName: "FKA5BAB00213CF056", tableName: "device_registration") {
            column(name: "user_id")
        }
    }

    changeSet(author: "root (generated)", id: "1307574571654-6") {
        createIndex(indexName: "FK4606E719490FB402", tableName: "device_registration_registered_types") {
            column(name: "device_registration_id")
        }
    }

    changeSet(author: "root (generated)", id: "1307574571654-8") {
        addForeignKeyConstraint(baseColumnNames: "user_id", baseTableName: "device_registration", constraintName: "FKA5BAB00213CF056", deferrable: "false", initiallyDeferred: "false", referencedColumnNames: "id", referencedTableName: "user", referencesUniqueColumn: "false")
    }

    changeSet(author: "root (generated)", id: "1307574571654-9") {
        addForeignKeyConstraint(baseColumnNames: "device_registration_id", baseTableName: "device_registration_registered_types", constraintName: "FK4606E719490FB402", deferrable: "false", initiallyDeferred: "false", referencedColumnNames: "id", referencedTableName: "device_registration", referencesUniqueColumn: "false")
    }

    changeSet(author: "root (generated)", id: "1307633792819-1") {
        addNotNullConstraint(columnDataType: "bigint", columnName: "device_registration_id", tableName: "device_registration_registered_types")
    }

	changeSet(author: "root (generated)", id: "1307718740932-1") {
		addColumn(tableName: "call_history") {
			column(name: "result", type: "varchar(255)") {
				constraints(nullable: "false")
			}
		}
	}

	changeSet(author: "root (generated)", id: "1307995568606-1") {
		addColumn(tableName: "user") {
			column(name: "is_active_directory", type: "bit") {
				constraints(nullable: "false")
			}
		}
	}

    changeSet(author: "root (generated)", id: "1308759117297-1") {
        addColumn(tableName: "scheduled_conference") {
            column(name: "uid", type: "varchar(255)") {
                constraints(nullable: "true")
            }
        }
    }

    changeSet(author: "root (generated)", id: "1308759117297-2") {
        addNotNullConstraint(columnDataType: "bigint", columnName: "organization_id", tableName: "organization_enabled_features")
    }

	changeSet(author: "rob", id: "1309205752211-2") {
		addColumn(tableName: "phone_number") {
			column(name: "class", type: "varchar(255)") {
				constraints(nullable: "false")
			}
		}
		addColumn(tableName: "phone_number") {
			column(name: "ip", type: "varchar(50)") {
				constraints(unique: "true", uniqueConstraintName: 'uk_phone_number_ip')
			}
		}
		addColumn(tableName: "phone_number") {
			column(name: "sms_domain", type: "varchar(50)")
		}

        update(tableName: 'phone_number') {
            column(name: 'class', value: 'com.interact.listen.pbx.Extension')
            where "type='EXTENSION'"
        }

        update(tableName: 'phone_number') {
            column(name: 'class', value: 'com.interact.listen.voicemail.DirectVoicemailNumber')
            where "type='VOICEMAIL'"
        }

        update(tableName: 'phone_number') {
            column(name: 'class', value: 'com.interact.listen.MobilePhone')
            where "type='MOBILE'"
        }

        update(tableName: 'phone_number') {
            column(name: 'class', value: 'com.interact.listen.OtherPhone')
            where "type='OTHER'"
        }

        update(tableName: 'phone_number') {
            column(name: 'class', value: 'com.interact.listen.OtherPhone')
            where "type='HOME'"
        }

		dropNotNullConstraint(columnDataType: "bit", columnName: "is_public", tableName: "phone_number")
		dropColumn(columnName: "supports_message_light", tableName: "phone_number")
		dropColumn(columnName: "type", tableName: "phone_number")
	}

	changeSet(author: "root (generated)", id: "1309205752211-1") {
		addColumn(tableName: "after_hours_configuration") {
			column(name: "mobile_phone_id", type: "bigint")
		}

		createIndex(indexName: "FK7C7614E359C40479", tableName: "after_hours_configuration") {
			column(name: "mobile_phone_id")
		}

		addForeignKeyConstraint(baseColumnNames: "mobile_phone_id", baseTableName: "after_hours_configuration", constraintName: "FK7C7614E359C40479", deferrable: "false", initiallyDeferred: "false", referencedColumnNames: "id", referencedTableName: "phone_number", referencesUniqueColumn: "false")

		dropForeignKeyConstraint(baseTableName: "after_hours_configuration", constraintName: "after_hours_conf_phone_nu_fk")
		dropColumn(columnName: "phone_number_id", tableName: "after_hours_configuration")
	}

	changeSet(author: "root (generated)", id: "1309447761436-1") {
		createTable(tableName: "conferencing_configuration") {
			column(autoIncrement: "true", name: "id", type: "bigint") {
				constraints(nullable: "false", primaryKey: "true", primaryKeyName: "conferencing_PK")
			}

			column(name: "version", type: "bigint") {
				constraints(nullable: "false")
			}

			column(name: "organization_id", type: "bigint") {
				constraints(nullable: "false")
			}

			column(name: "pin_length", type: "integer") {
				constraints(nullable: "false")
			}
		}
		createIndex(indexName: "FKC3E7621056D05B56", tableName: "conferencing_configuration") {
			column(name: "organization_id")
		}
		addForeignKeyConstraint(baseColumnNames: "organization_id", baseTableName: "conferencing_configuration", constraintName: "FKC3E7621056D05B56", deferrable: "false", initiallyDeferred: "false", referencedColumnNames: "id", referencedTableName: "organization", referencesUniqueColumn: "false")
	}

	changeSet(author: "root (generated)", id: "1309530345925-1") {
		createTable(tableName: "mail_configuration") {
			column(autoIncrement: "true", name: "id", type: "bigint") {
				constraints(nullable: "false", primaryKey: "true", primaryKeyName: "mail_configurPK")
			}

			column(name: "version", type: "bigint") {
				constraints(nullable: "false")
			}

			column(name: "default_from", type: "varchar(255)")

			column(name: "host", type: "varchar(255)") {
				constraints(nullable: "false")
			}

			column(name: "password", type: "varchar(255)")

			column(name: "username", type: "varchar(255)")
		}
	}

	changeSet(author: "root (generated)", id: "1309965402630-2") {
		addColumn(tableName: "organization") {
			column(name: "context_path", type: "varchar(50)") {
				constraints(nullable: "false", unique: "true")
			}
		}
		createIndex(indexName: "context_path_unique_1309965401489", tableName: "organization", unique: "true") {
			column(name: "context_path")
		}
	}

    changeSet(author: "root (generated)", id: "1309967981861-1") {
        createTable(tableName: "prompt_override") {
            column(autoIncrement: "true", name: "id", type: "bigint") {
                constraints(nullable: "false", primaryKey: "true", primaryKeyName: "prompt_overriPK")
            }

            column(name: "version", type: "bigint") {
                constraints(nullable: "false")
            }

            column(name: "date", type: "date") {
                constraints(nullable: "false")
            }

            column(name: "menu_id", type: "bigint") {
                constraints(nullable: "false")
            }

            column(name: "options_prompt", type: "varchar(255)") {
                constraints(nullable: "false")
            }

            column(name: "organization_id", type: "bigint") {
                constraints(nullable: "false")
            }
        }
    }

    changeSet(author: "root (generated)", id: "1309967981861-2") {
        createIndex(indexName: "organization_unique_9087444756", tableName: "prompt_override") {
            column(name: "organization_id")
        }
    }

    changeSet(author: "root (generated)", id: "1309967981861-3") {
        createIndex(indexName: "menu_id_unique_9087444767", tableName: "prompt_override") {
            column(name: "menu_id")
        }
    }

    changeSet(author: "root (generated)", id: "1309967981861-4") {
        addForeignKeyConstraint(baseColumnNames: "menu_id", baseTableName: "prompt_override", constraintName: "prompt_override_menu_id_fk", deferrable: "false", initiallyDeferred: "false", referencedColumnNames: "id", referencedTableName: "menu", referencesUniqueColumn: "false")
    }

    changeSet(author: "root (generated)", id: "1309967981861-5") {
        addForeignKeyConstraint(baseColumnNames: "organization_id", baseTableName: "prompt_override", constraintName: "prompt_overri_organizati_fk", deferrable: "false", initiallyDeferred: "false", referencedColumnNames: "id", referencedTableName: "organization", referencesUniqueColumn: "false")
    }

	changeSet(author: "root (generated)", id: "1310396460702-1") {
		addColumn(tableName: "prompt_override") {
			column(name: "menu_group_id", type: "bigint") {
				constraints(nullable: "false")
			}
		}

		dropForeignKeyConstraint(baseTableName: "prompt_override", baseTableSchemaName: "listen2", constraintName: "prompt_override_menu_id_fk")

		dropForeignKeyConstraint(baseTableName: "prompt_override", baseTableSchemaName: "listen2", constraintName: "prompt_overri_organizati_fk")

		createIndex(indexName: "FK908744472A28807E", tableName: "prompt_override") {
			column(name: "menu_group_id")
		}

		addForeignKeyConstraint(baseColumnNames: "menu_group_id", baseTableName: "prompt_override", constraintName: "FK908744472A28807E", deferrable: "false", initiallyDeferred: "false", referencedColumnNames: "id", referencedTableName: "menu_group", referencesUniqueColumn: "false")

		dropColumn(columnName: "menu_id", tableName: "prompt_override")

		dropColumn(columnName: "organization_id", tableName: "prompt_override")

        createIndex(indexName: "unique-date", tableName: "prompt_override") {
			column(name: "menu_group_id")

			column(name: "date")
		}
	}

}
