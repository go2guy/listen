package com.interact.listen;

public final class EmailerUtil
{
    public static final String CONFERENCE_EMAIL_SUBJECT = "Listen Conference Invitation";
    
    public static final String EMAIL_BODY = "<html><body>Hello,<br/><br/>"
                                     + "You have been invited to a conference by %s.<br/><br/>"
                                     + "Conference details:<br/><br/>"
                                     + "Date/Time: %s until %s<br/>"
                                     + "Description: %s<br/>"
                                     + "%s: %s<br/>" //this will be phone number or ip based on protocol
                                     + "Pin: %s<br/>"
                                     + "</body></html>";
    
    public static final String TEST_EMAIL_NOTIFICATION_BODY = "<html><body>Hello,<br/><br/>You have correctly configured your profile to " 
                                                       + "receive Listen E-mail notifications at this address.<br/></body></html>";
    
    public static final String TEST_SMS_NOTIFICATION_BODY = "You have correctly configured your profile to receive SMS notifications " +
    		                                                "at this address.";
    
    public static final String EMAIL_NOTIFICATION_SUBJECT = "New voicemail from %s";

    public static final String SMS_NOTIFICATION_BODY = "New voicemail from %s.  Retrieve it at %s";
    public static final String ALTERNATE_NUMBER_SMS_NOTIFICATION_BODY = "%s New voicemail from %s.";
    public static final String FILE_IS_ATTACHED = "The voicemail is attached.";
    public static final String FILE_NOT_ATTACHED = "The voicemail could not be attached.  Contact a system administrator for assistance.";
    
    public static enum SmsEmailAddress
    {   
        ALLTEL       ("Alltel",        "message.alltel.com"),
        ATT          ("AT&T",          "txt.att.net"),
        BOOST_MOBILE ("Boost Mobile",  "myboostmobile.com"),
        CRICKET      ("Cricket",       "sms.mycricket.com"),
        QWEST        ("Qwest",         "qwestmp.com"),
        SPRINT       ("Sprint",        "messaging.sprintpcs.com"),
        TMOBILE      ("T-Mobile",      "tmomail.net"),
        US_CELLULAR  ("US Cellular",   "email.uscc.net"),
        VERIZON      ("Verizon",       "vtext.com"),
        VIRGIN_MOBILE("Virgin Mobile", "vmobl.com");
        
        private String provider;
        private String emailAddress;
        
        private SmsEmailAddress(String name, String address)
        {
            this.provider = name;
            this.emailAddress = address;
        }
        
        public String getProvider()
        {
            return provider;
        }
        
        public String getEmailAddress()
        {
            return emailAddress;
        }
    }

    private EmailerUtil()
    {
        throw new AssertionError("Cannot instantiate utility class EmailerUtil");
    }
}
