package com.interact.listen;

import com.interact.listen.config.Configuration;
import com.interact.listen.config.Property;
import com.interact.listen.marshal.converter.FriendlyIso8601DateConverter;
import com.interact.listen.resource.*;

import java.io.*;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.text.SimpleDateFormat;
import java.util.*;

import javax.activation.DataHandler;
import javax.activation.FileDataSource;
import javax.mail.*;
import javax.mail.internet.*;

import org.apache.commons.io.IOUtils;
import org.apache.log4j.Logger;
import org.hibernate.Criteria;
import org.hibernate.criterion.Projections;
import org.hibernate.criterion.Restrictions;

public class EmailerService
{
    private static final Logger LOG = Logger.getLogger(EmailerService.class);
    private static final String CONFERENCE_EMAIL_SUBJECT = "Listen Conference Invitation";
    
    private static final String EMAIL_BODY = "<html><body>Hello,<br/><br/>"
                                     + "You have been invited to a conference by %s.<br/><br/>"
                                     + "Conference details:<br/><br/>"
                                     + "Date/Time: %s<br/>"
                                     + "Description: %s<br/>"
                                     + "%s: %s<br/>" //this will be phone number or ip based on protocol
                                     + "Pin: %s<br/>"
                                     + "</body></html>";
    
    private static final String TEST_EMAIL_NOTIFICATION_BODY = "<html><body>Hello,<br/><br/>You have correctly configured your profile to " 
                                                       + "receive Listen E-mail notifications at this address.<br/></body></html>";
    
    private static final String TEST_SMS_NOTIFICATION_BODY = "You have correctly configured your profile to receive SMS notifications at this address";
    
    private static final String EMAIL_NOTIFICATION_SUBJECT = "New voicmeail from %s";
    private static final String EMAIL_NOTIFICATION_BODY = "<html><body>You have recieved a new voicemail from %s at %s.<br/><br/>"
                                                  + "Total new messages: %s<br/><br/>"
                                                  + "The voicemail is attached"
                                                  + "</body></html>";
    
    private static final String SMS_NOTIFICATION_BODY = "New voicemail from %s.  Retrieve it at %s";

    private final SimpleDateFormat sdf = new SimpleDateFormat(FriendlyIso8601DateConverter.ISO8601_FORMAT);

    private boolean sendEmail(InternetAddress[] toAddresses, String body, String subjectPrepend, String subject)
    {
        return sendEmail(toAddresses, body, subjectPrepend, subject, null);
    }
    
    private boolean sendEmail(InternetAddress[] toAddresses, String body, String subject, File attachment)
    {
        return sendEmail(toAddresses, body, "", subject, attachment);
    }
    
    private boolean sendEmail(InternetAddress[] toAddresses, String body, String subjectPrepend, String subject, File attachment)
    {
        Properties props = new Properties();
        props.setProperty("mail.transport.protocol", "smtp");
        props.setProperty("mail.host", Configuration.get(Property.Key.MAIL_SMTPHOST));
        props.setProperty("mail.user", Configuration.get(Property.Key.MAIL_SMTPUSERNAME));
        props.setProperty("mail.password", Configuration.get(Property.Key.MAIL_SMTPPASSWORD));
        boolean result = true;

        try
        {
            javax.mail.Session mailSession = Session.getDefaultInstance(props, null);
            Transport transport = mailSession.getTransport();
            InternetAddress fromAddress = new InternetAddress(Configuration.get(Property.Key.MAIL_FROMADDRESS));

            MimeMessage message = new MimeMessage(mailSession);
            message.setFrom(fromAddress);
            message.setSubject(subjectPrepend + subject);
            //message.setContent(body, "text/html; charset=UTF-8");
            message.addRecipients(MimeMessage.RecipientType.TO, toAddresses);
            
            if(attachment == null)
            {
                message.setContent(body, "text/html; charset=UTF-8");
            }
            else
            {
                MimeBodyPart mbp1 = new MimeBodyPart();
                mbp1.setText(body, "UTF-8", "html");
                
                MimeBodyPart mbp2 = new MimeBodyPart();
                FileDataSource fds = new FileDataSource(attachment);
                mbp2.setDataHandler(new DataHandler(fds));
                mbp2.setFileName(fds.getName());
                
                Multipart mp = new MimeMultipart();
                mp.addBodyPart(mbp1);
                mp.addBodyPart(mbp2);

                // add the Multipart to the message
                message.setContent(mp);
            }
            
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
            result = sendEmail(toAddresses, body, subjectPrepend, CONFERENCE_EMAIL_SUBJECT, null);
        }

        return result;
    }
    
    public boolean sendTestNotificationSettingsMessage(String type, String toAddress)
    {
        boolean result = true;
        
        ArrayList<String> mailAddresses = new ArrayList<String>();
        mailAddresses.add(toAddress);
        InternetAddress[] toAddresses = getInternetAddresses(mailAddresses);
        
        if(toAddresses.length > 0)
        {
            result = sendEmail(toAddresses, type.equals("email") ? TEST_EMAIL_NOTIFICATION_BODY : TEST_SMS_NOTIFICATION_BODY, 
                                                                 "Listen Notification Test Message", "", null);
        }
        
        return result;
    }
    
    public void sendEmailVoicmailNotification(Voicemail voicemail, Subscriber subscriber)
    {
        ArrayList<String> mailAddresses = new ArrayList<String>();
        mailAddresses.add(subscriber.getEmailAddress());
        InternetAddress[] toAddresses = getInternetAddresses(mailAddresses);
        
        if(toAddresses.length > 0)
        {
            String subject = String.format(EMAIL_NOTIFICATION_SUBJECT, voicemail.getLeftBy());
            String body = String.format(EMAIL_NOTIFICATION_BODY, voicemail.getLeftBy(), sdf.format(voicemail.getDateCreated()),
                                    getNewVoicemailCount(subscriber));
            File attachment = getAttachment(voicemail.getUri());
            
            sendEmail(toAddresses, body, subject, attachment);
            
            try
            {
                attachment.delete();
            }
            catch(Exception e)
            {
                LOG.error("Error deleting temp voicemail file", e);
            }
        }
    }
    
    public void sendSmsVoicemailNotification(Voicemail voicemail, Subscriber subscriber)
    {
        ArrayList<String> mailAddresses = new ArrayList<String>();
        mailAddresses.add(subscriber.getSmsAddress());
        InternetAddress[] toAddresses = getInternetAddresses(mailAddresses);
        
        if(toAddresses.length > 0)
        {
            // hard-coded access number for now
            String body = String.format(SMS_NOTIFICATION_BODY, voicemail.getLeftBy(), "402-476-8786");
        
            sendEmail(toAddresses, body, "", "");
        }
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
    
    private String getNewVoicemailCount(Subscriber subscriber)
    {
        org.hibernate.Session session = HibernateUtil.getSessionFactory().getCurrentSession();
        Criteria criteria = session.createCriteria(Voicemail.class);

        // only new records
        criteria.add(Restrictions.eq("isNew", true));

        // belonging to this subscriber
        criteria.createAlias("subscriber", "subscriber_alias");
        criteria.add(Restrictions.eq("subscriber_alias.id", subscriber.getId()));

        criteria.setResultTransformer(Criteria.DISTINCT_ROOT_ENTITY);
        criteria.setFirstResult(0);
        criteria.setProjection(Projections.rowCount());

        return String.valueOf((Long)criteria.list().get(0));
    }
    
    private File getAttachment(String uri)
    {
        String filename = getFilenameFromUri(uri);
        File file = new File(System.getProperty("java.io.tmpdir") + System.getProperty("file.separator") + filename);
        InputStream input = null;
        OutputStream output = null;
        
        try
        {
            URL url = new URL(uri);
            HttpURLConnection connection = (HttpURLConnection)url.openConnection();
            connection.setRequestMethod("GET");
            connection.connect();
            input = connection.getInputStream();
            output = new FileOutputStream(file);
            
            IOUtils.copy(input, output);
        }
        catch(MalformedURLException e)
        {
            LOG.error("Error with URL when getting voicemail file for attachment to notification e-mail", e);
        }
        catch(Exception e)
        {
            LOG.error("Error getting voicemail file for e-mail notification", e);
        }
        finally
        {
            if(input != null)
            {
                try
                {
                    input.close();
                }
                catch(IOException e)
                {
                    LOG.warn("Unable to close InputStream when reading [" + uri + "]");
                }
            }
            
            if(output != null)
            {
                try
                {
                    output.close();
                }
                catch(IOException e)
                {
                    LOG.warn("Unable to close OutputStream when reading [" + uri + "]");
                }
            }
        }
        
        return file;
    }
    
    private String getFilenameFromUri(String uri)
    {
        if(uri.indexOf(System.getProperty("file.separator")) != -1)
        {
            return uri.substring(uri.lastIndexOf(System.getProperty("file.separator")) + 1);
        }
        
        return uri;
    }
}
