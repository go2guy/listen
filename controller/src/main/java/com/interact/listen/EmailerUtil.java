package com.interact.listen;

import java.util.ArrayList;
import java.util.Arrays;

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
    public static final String EMAIL_NOTIFICATION_BODY = "<html><body>You have recieved a new voicemail from %s at %s.<br/><br/>"
                                                         + "Total new messages: %s<br/><br/>"
                                                         + "%s"
                                                         + "</body></html>";
    
    public static final String SMS_NOTIFICATION_BODY = "New voicemail from %s.  Retrieve it at %s";
    public static final String FILE_IS_ATTACHED = "The voicemail is attached.";
    public static final String FILE_NOT_ATTACHED = "The voicemail could not be attached.  Contact a system administrator for assistance.";
    
    public static final ArrayList<String> KNOWN_SMS_EMAIL_ADDRESSES = new ArrayList<String>
    (Arrays.asList(new String[] {"number@message.alltel.com",
                                 "number@messaging.sprintpcs.com",
                                 "number@sprintpcs.com",
                                 "number@vtext.com",
                                 "number@paging.acswireless.com",
                                 "number@txt.bellmobility.ca",
                                 "number@blsdcs.net",
                                 "number@blueskyfrog.com",
                                 "number@myboostmobile.com",
                                 "number@csouth1.com",
                                 "number@mobile.celloneusa.com",
                                 "number@gocbw.com",
                                 "number@mmode.com",
                                 "number@imcingular.com",
                                 "number@sms.edgewireless.com",
                                 "number@einsteinsms.com",
                                 "number@sms.goldentele.com",
                                 "number@mymetropcs.com",
                                 "number@page.metrocall.com",
                                 "number@page.mobilfone.com",
                                 "number@messaging.nextel.com",
                                 "number@pagenet.net",
                                 "number@pcs.rogers.com",
                                 "number@pager.qualcomm.com",
                                 "number@qwestmp.com",
                                 "number@safaricomsms.com",
                                 "number@satelindogsm.com",
                                 "number@text.simplefreedom.net",
                                 "number@skytel.com",
                                 "number@page.southernlinc.com",
                                 "number@tms.suncom.com",
                                 "number@mobile.surewest.com",
                                 "number@tmomail.net",
                                 "number@tmail.com",
                                 "number@movistar.net",
                                 "number@email.uscc.net",
                                 "number@myairmail.com",
                                 "number@vmobl.com",
                                 "number@airmessage.net",
                                 "number@wyndtell.com",
                                 "number@txt.att.net" }));

    private EmailerUtil()
    {
        throw new AssertionError("Cannot instantiate utility class EmailerUtil");
    }

}
