package com.interact.listen.config;

import javax.persistence.*;

@Entity
@Table(name = "PROPERTY")
public class Property
{
    public static enum Key {
        DNIS_MAPPING           ("com.interact.listen.dnisMapping",            "770:mailbox;990:conferencing;*:voicemail"),
        MAIL_SMTPHOST          ("com.interact.listen.mail.smtpHost",          "localhost"),
        MAIL_SMTPUSERNAME      ("com.interact.listen.mail.smtpUsername",      ""),
        MAIL_SMTPPASSWORD      ("com.interact.listen.mail.smtpPassword",      ""),
        MAIL_FROMADDRESS       ("com.interact.listen.mail.fromAddress",       "noreply@localhost"),
        CONFERENCING_PINLENGTH ("com.interact.listen.conferencing.pinLength", "10"),
        PAGER_NUMBER           ("com.interact.listen.pagerNumber",            "4024292889"),
        ALTERNATE_NUMBER       ("com.interact.listen.alternateNumber",        ""),
        PAGE_PREFIX            ("com.interact.listen.pagePrefix",             ""),
        REALIZE_URL            ("com.interact.listen.realizeUrl",             "http://localhost:8080/realize"),
        REALIZE_ALERT_NAME     ("com.interact.listen.realizeAlertName",       "Listen Pager Alert");

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
}
