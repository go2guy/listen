package com.interact.listen;

import com.interact.listen.marshal.converter.FriendlyIso8601DateConverter;
import com.interact.listen.resource.Conference;
import com.interact.listen.resource.Pin;

import java.text.SimpleDateFormat;
import java.util.*;

import javax.mail.MessagingException;
import javax.mail.Session;
import javax.mail.Transport;
import javax.mail.internet.*;

import org.apache.log4j.Logger;

public class EmailerService
{
    private static final Logger LOG = Logger.getLogger(EmailerService.class);
    private static final String EMAIL_SUBJECT = "Listen Conference Invitation";    
    private static final String ISO8601_FORMAT = "yyyy-MM-dd'T'HH:mm:ss.SSS";
    
    private static final String EMAIL_BODY = "<html><body>Hello,<br/><br/>"
                                     + "You have been invited to a conference by %s.<br/><br/>"
                                     + "Conference details:<br/><br/>"
                                     + "Date/Time: %s<br/>"
                                     + "Description: %s<br/>"
                                     + "%s: %s<br/>" //this will be phone number or ip based on protocol
                                     + "Pin: %s<br/>"
                                     + "</body></html>";

    private final SimpleDateFormat sdf = new SimpleDateFormat(FriendlyIso8601DateConverter.ISO8601_FORMAT);

    private boolean sendEmail(InternetAddress[] toAddresses, String body, String subjectPrepend, Date dateTime)
    {
        Properties props = new Properties();
        props.setProperty("mail.transport.protocol", "smtp");
        props.setProperty("mail.host", System.getProperty("com.interact.listen.mail.host", ""));
        props.setProperty("mail.user", "");
        props.setProperty("mail.password", "");
        boolean result = true;
        
        try
        {
            Session mailSession = Session.getDefaultInstance(props, null);
            Transport transport = mailSession.getTransport();
            InternetAddress fromAddress = new InternetAddress(System.getProperty("com.interact.listen.mail.from.address", ""));

            MimeMessage message = new MimeMessage(mailSession);
            message.setFrom(fromAddress);
            message.setSubject(subjectPrepend + EMAIL_SUBJECT);
            message.setContent(body, "text/html; charset=UTF-8");
            message.addRecipients(MimeMessage.RecipientType.TO, toAddresses);
            
            transport.connect();
            transport.sendMessage(message, message.getRecipients(MimeMessage.RecipientType.TO));
            transport.close();
        }
        catch(AddressException e)
        {
            result = false;
            LOG.error("Error with com.interact.listen.mail.from.address, please configure a valid e-mail address", e);
        }
        catch(MessagingException e)
        {
            result = false;
            LOG.error("Error occurred sending email", e);
        }

        return result;
    }

    public boolean sendScheduleEmail(ArrayList<String> toAddressesList, String username, String description,
                                     Date dateTime, Conference conference, String phoneNumber, String protocol,
                                     String subjectPrepend, String emailType)
    {
        String body  = "";
        boolean result = true;
        String numberTitle = protocol.equals("PSTN") ? "Phone Number" : "IP Address";
        
        if(emailType.equals("ACTIVE"))
        {
            body = getActiveMessageBody(username, description, conference, numberTitle, phoneNumber, dateTime);
        }
        else if(emailType.equals("PASSIVE"))
        {
            body = getPassiveMessageBody(username, description, conference, numberTitle, phoneNumber, dateTime);
        }
        
        InternetAddress[] toAddresses = getInternetAddresses(toAddressesList);
        
        if(toAddresses.length > 0)
        {
            result = sendEmail(toAddresses, body, subjectPrepend, dateTime);
        }

        return result;
    }

    private InternetAddress[] getInternetAddresses(ArrayList<String> emails)
    {
        ArrayList<InternetAddress> mailAddresses = new ArrayList<InternetAddress>();
        
        for(String email : emails)
        {
            try
            {
                InternetAddress emailAddress = new InternetAddress(email);
                mailAddresses.add(emailAddress);
                    
            }
            catch(AddressException e)
            {
                LOG.warn("To address " + email + " is not a valid e-mail address, removing from the list of " +
                         "recipients", e);
            }
        }

        InternetAddress[] returnArray = new InternetAddress[mailAddresses.size()];
        return mailAddresses.toArray(returnArray);
    }

    private String getActiveMessageBody(String username, String description, Conference conference, String numberTitle,
                                        String phoneNumber, Date dateTime)
    {
        String formattedDateTime = sdf.format(dateTime);
        String activePin = getPin(conference.getPins(), Pin.PinType.ACTIVE);
        activePin = activePin + " (Active)";
        
        String formattedEmailBody = String.format(EMAIL_BODY, username, formattedDateTime, description, numberTitle,
                                                  phoneNumber, activePin);
        
        return formattedEmailBody;
    }
    
    private String getPassiveMessageBody(String username, String description, Conference conference,
                                         String numberTitle, String phoneNumber, Date dateTime)
    {
        String formattedDateTime = sdf.format(dateTime);
        String passivePin = getPin(conference.getPins(), Pin.PinType.PASSIVE);
        passivePin = passivePin + " (Passive)";
        
        String formattedEmailBody = String.format(EMAIL_BODY, username, formattedDateTime, description, numberTitle,
                                                  phoneNumber, passivePin);
        
        return formattedEmailBody;
    }
    
    private String getPin(Set<Pin> pins, Pin.PinType pinType)
    {
        for(Pin pin : pins)
        {
            if(pin.getType() == pinType)
            {
                return pin.getNumber();
            }
        }
        
        return null;
    }
}
