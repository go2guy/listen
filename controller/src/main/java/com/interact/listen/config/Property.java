package com.interact.listen.config;

import java.util.*;

import javax.persistence.*;

import org.apache.commons.lang.StringUtils;

@Entity
@Table(name = "PROPERTY")
public class Property
{
    public static enum Key
    {
        /* Comma-delimited mapping of dialed numbers to applications, format is NUMBER:APPLICATION, e.g. "100:conferencing;200:voicemail" */
        DNIS_MAPPING             ("com.interact.listen.dnisMapping",             "770:mailbox;990:conferencing;*:voicemail"),

        /* SMTP host for sending email */
        MAIL_SMTPHOST            ("com.interact.listen.mail.smtpHost",           "localhost"),

        /* SMTP username for sending email */
        MAIL_SMTPUSERNAME        ("com.interact.listen.mail.smtpUsername",       ""),

        /* SMTP password for sending email */
        MAIL_SMTPPASSWORD        ("com.interact.listen.mail.smtpPassword",       ""),

        /* Address to use in the "From:" field when sending email */
        MAIL_FROMADDRESS         ("com.interact.listen.mail.fromAddress",        "no-reply@localhost"),

        /* Length of newly-created conferencing PINs */
        CONFERENCING_PINLENGTH   ("com.interact.listen.conferencing.pinLength",  "10"),

        /* Phone number of the Interact pager phone */
        PAGER_NUMBER             ("com.interact.listen.pagerNumber",             "4024292889"),

        /* Alternate number to dial when the pager phone is notified of new voicemail */
        ALTERNATE_NUMBER         ("com.interact.listen.alternateNumber",         ""),

        /* Prefix for the message sent to the pager phone */
        PAGE_PREFIX              ("com.interact.listen.pagePrefix",              ""),

        /* Location of the Realize system */
        REALIZE_URL              ("com.interact.listen.realizeUrl",              "http://localhost:8080/realize"),

        /* Name of the Realize alert that is monitoring the Listen system and notifying when it is down */
        REALIZE_ALERT_NAME       ("com.interact.listen.realizeAlertName",        "Listen Pager Alert"),

        /* Whether or not Active Directory logins are enabled */
        ACTIVE_DIRECTORY_ENABLED ("com.interact.listen.activeDirectory.enabled", Boolean.FALSE.toString()),

        /* Active Directory server */
        ACTIVE_DIRECTORY_SERVER  ("com.interact.listen.activeDirectory.server",  ""),

        /* Active Directory domain */
        ACTIVE_DIRECTORY_DOMAIN  ("com.interact.listen.activeDirectory.domain",  ""),

        /* Comma-delimited list of SPOT systems */
        SPOT_SYSTEMS             ("com.interact.listen.spotSystems",             ""),

        /* Phone number that people dial from the outside world to reach the system, with protocol, e.g. "PSTN:4024768786" or "VOIP:127.0.0.1" */
        PHONE_NUMBER             ("com.interact.listen.phoneNumber",             ""),

        /* Whether or not API requests should be authenticated */
        AUTHENTICATE_API         ("com.interact.listen.auth.authenticateApi",    Boolean.TRUE.toString()),

        /* Whether or not Android C2DM is enabled */
        ANDROID_C2DM_ENABLED     ("com.interact.listen.google.c2dm.enabled",     Boolean.FALSE.toString()),

        /* Google Account used for C2DM */
        GOOGLE_AUTH_USER        ("com.interact.listen.google.username",          ""),

        /* Authorization token used for C2DM */
        GOOGLE_AUTH_TOKEN       ("com.interact.listen.google.authToken",         ""),
        
        /* semi-colon-delimited mapping of numbers and labels of conference bridge numbers to use in conference invitations */
        CONFERENCE_BRIDGES      ("com.interact.listen.conferenceBridges",        ""),
        
        /* Number to use in voicemail notifications for direct access to the voicemail inbox */
        DIRECT_VOICEMAIL_NUMBER ("com.interact.listen.directVoicemailNumber",    "");

        private final String key;
        private final String defaultValue;

        private Key(String key, String defaultValue)
        {
            this.key = key;
            this.defaultValue = defaultValue;
        }

        public String getKey()
        {
            return key;
        }

        public String getDefaultValue()
        {
            return defaultValue;
        }

        public static Key findByKey(String search)
        {
            for(Key k : Key.values())
            {
                if(k.getKey().equals(search))
                {
                    return k;
                }
            }
            return null;
        }
    }
    
    @Column(name = "PROPERTY_KEY", nullable = false, unique = true)
    @Id
    private String key;

    @Column(name = "PROPERTY_VALUE")
    private String value;

    public static Property newInstance(String key, String value)
    {
        Property property = new Property();
        property.setKey(key);
        property.setValue(value);
        return property;
    }

    public String getKey()
    {
        return key;
    }

    public void setKey(String key)
    {
        this.key = key;
    }

    public String getValue()
    {
        return value;
    }

    public void setValue(String value)
    {
        this.value = value;
    }

    public static Set<String> delimitedStringToSet(String string, String delimiter)
    {
        if(string == null)
        {
            return new HashSet<String>();
        }
        Set<String> result = new HashSet<String>();
        for(String s : string.split(delimiter))
        {
            if(s.trim().equals(""))
            {
                continue; 
            }
            result.add(s);
        }
        return result;
    }

    public static String setToDelimitedString(Set<String> set, String delimiter)
    {
        return StringUtils.join(set, delimiter);
    }
}
