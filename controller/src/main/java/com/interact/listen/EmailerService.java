package com.interact.listen;

import com.interact.listen.api.GetDnisServlet;
import com.interact.listen.config.Configuration;
import com.interact.listen.config.Property;
import com.interact.listen.history.Channel;
import com.interact.listen.history.HistoryService;
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
import org.hibernate.Session;
import org.hibernate.criterion.Projections;
import org.hibernate.criterion.Restrictions;

public class EmailerService
{
    private static final Logger LOG = Logger.getLogger(EmailerService.class);

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
            javax.mail.Session mailSession = javax.mail.Session.getDefaultInstance(props, null);
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
                                     Date dateTime, Date endDateTime, Conference conference, String phoneNumber, String protocol,
                                     String subjectPrepend, String emailType)
    {
        String body  = "";
        boolean result = true;
        String numberTitle = protocol.equals("PSTN") ? "Phone Number" : "IP Address";
        
        if(emailType.equals("ACTIVE"))
        {
            body = getActiveMessageBody(username, description, conference, numberTitle, phoneNumber, dateTime, endDateTime);
        }
        else if(emailType.equals("PASSIVE"))
        {
            body = getPassiveMessageBody(username, description, conference, numberTitle, phoneNumber, dateTime, endDateTime);
        }
        
        InternetAddress[] toAddresses = getInternetAddresses(toAddressesList);
        
        if(toAddresses.length > 0)
        {
            result = sendEmail(toAddresses, body, subjectPrepend, EmailerUtil.CONFERENCE_EMAIL_SUBJECT, null);
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
            result = sendEmail(toAddresses, type.equals("email") ? EmailerUtil.TEST_EMAIL_NOTIFICATION_BODY
                               : EmailerUtil.TEST_SMS_NOTIFICATION_BODY, "Listen Notification Test Message", "", null);
        }
        
        return result;
    }
    
    public void sendEmailVoicmailNotification(Voicemail voicemail, Subscriber subscriber)
    {
        File attachment = null;
        boolean fileReadyForAttachment = false;
        ArrayList<String> mailAddresses = new ArrayList<String>();
        mailAddresses.add(subscriber.getEmailAddress());
        InternetAddress[] toAddresses = getInternetAddresses(mailAddresses);
        
        if(toAddresses.length > 0)
        {
            String subject = String.format(EmailerUtil.EMAIL_NOTIFICATION_SUBJECT, voicemail.getLeftBy());
            
            try
            {
                attachment = getAttachment(voicemail.getUri());
                fileReadyForAttachment = true;
            }
            catch(Exception e)
            {
                LOG.error("An error occured trying to obtain the voicemail to attach. Won't attach e-mail");
            }
            
            String body = String.format(EmailerUtil.EMAIL_NOTIFICATION_BODY, voicemail.getLeftBy(),
                                        sdf.format(voicemail.getDateCreated()), getNewVoicemailCount(subscriber),
                                        fileReadyForAttachment ? EmailerUtil.FILE_IS_ATTACHED
                                                              : EmailerUtil.FILE_NOT_ATTACHED);
            sendEmail(toAddresses, body, subject, attachment);
            
            if(attachment != null)
            {
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
    }
    
    public void sendSmsVoicemailNotification(Voicemail voicemail, Subscriber subscriber)
    {
        sendSmsVoicemailNotification(voicemail, subscriber.getSmsAddress());
    }
    
    public void sendSmsVoicemailNotification(Voicemail voicemail, String smsAddress)
    {
        ArrayList<String> mailAddresses = new ArrayList<String>();
        mailAddresses.add(smsAddress);
        InternetAddress[] toAddresses = getInternetAddresses(mailAddresses);
        
        if(toAddresses.length > 0)
        {
            String directVoicemailAccessNumber = getDirectVoicemailAccessNumber();
            String body = String.format(EmailerUtil.SMS_NOTIFICATION_BODY, voicemail.getLeftBy(), directVoicemailAccessNumber);
        
            sendEmail(toAddresses, body, "", "");
        }
    }
    
    public void sendAlternateNumberSmsVoicemailNotification(Voicemail voicemail)
    {
        Session session = HibernateUtil.getSessionFactory().getCurrentSession();
        PersistenceService persistenceService = new PersistenceService(session, null, Channel.AUTO);
        HistoryService historyService = new HistoryService(persistenceService);
        String alternateNumber = Configuration.get(Property.Key.ALTERNATE_NUMBER);
        
        if(alternateNumber != null && !alternateNumber.trim().equals(""))
        {
            String pagePrefix = Configuration.get(Property.Key.PAGE_PREFIX);
            ArrayList<String> mailAddresses = new ArrayList<String>();
            
            for(String address : EmailerUtil.KNOWN_SMS_EMAIL_ADDRESSES)
            {
                mailAddresses.add(address.replace("number", alternateNumber));
            }
            
            InternetAddress[] toAddresses = getInternetAddresses(mailAddresses);
            
            if(toAddresses.length > 0)
            {
                String directVoicemailAccessNumber = getDirectVoicemailAccessNumber();
                String body = String.format(EmailerUtil.SMS_NOTIFICATION_BODY, voicemail.getLeftBy(), directVoicemailAccessNumber);
                
                //pages to the alternate number need to differentiate themselves from regular pages that person might be getting
                body = pagePrefix + " " + body;
            
                //need to send out these pages fast, so just generate x requests to send an e-mail rather than letting the 
                //sendEmail method do the iteration.  Seems to be too slow
                for(InternetAddress internetAddress : toAddresses)
                {
                    InternetAddress[] singleAddress = new InternetAddress[] {internetAddress};
                    sendEmail(singleAddress, body, "", "");
                }
                
                historyService.writeSentVoicemailAlternatePage(voicemail);
            }
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
                                        String phoneNumber, Date dateTime, Date endDateTime)
    {
        String formattedDateTime = sdf.format(dateTime);
        String formattedEndDateTime = sdf.format(endDateTime);
        String activePin = getPin(conference.getPins(), Pin.PinType.ACTIVE);
        activePin = activePin + " (Active)";
        
        String formattedEmailBody = String.format(EmailerUtil.EMAIL_BODY, username, formattedDateTime, formattedEndDateTime,
                                                  description, numberTitle, phoneNumber, activePin);
        
        return formattedEmailBody;
    }
    
    private String getPassiveMessageBody(String username, String description, Conference conference,
                                         String numberTitle, String phoneNumber, Date dateTime, Date endDateTime)
    {
        String formattedDateTime = sdf.format(dateTime);
        String formattedEndDateTime = sdf.format(endDateTime);
        String passivePin = getPin(conference.getPins(), Pin.PinType.PASSIVE);
        passivePin = passivePin + " (Passive)";
        
        String formattedEmailBody = String.format(EmailerUtil.EMAIL_BODY, username, formattedDateTime, formattedEndDateTime,
                                                  description, numberTitle, phoneNumber, passivePin);
        
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
    
    private File getAttachment(String uri) throws Exception
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
            throw e;
        }
        catch(Exception e)
        {
            LOG.error("Error getting voicemail file for e-mail notification", e);
            throw e;
        }
        finally
        {
            IOUtils.closeQuietly(input);
            IOUtils.closeQuietly(output);
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
    
    private String getDirectVoicemailAccessNumber()
    {
        List<String> directVoicemailAccessNumbers = GetDnisServlet.getMappingByType("directVoicemail");
        
        //Even if multiple ones are configured, all should be valid and we can just return the first one
        return directVoicemailAccessNumbers.size() > 0 ? directVoicemailAccessNumbers.get(0) : "N/A";
    }
}
