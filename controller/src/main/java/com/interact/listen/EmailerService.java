package com.interact.listen;

import com.interact.listen.HibernateUtil.Environment;
import com.interact.listen.api.GetDnisServlet;
import com.interact.listen.config.Configuration;
import com.interact.listen.config.Property;
import com.interact.listen.history.DefaultHistoryService;
import com.interact.listen.history.HistoryService;
import com.interact.listen.marshal.converter.FriendlyIso8601DateConverter;
import com.interact.listen.resource.*;

import java.io.*;
import java.net.HttpURLConnection;
import java.net.URL;
import java.text.SimpleDateFormat;
import java.util.*;

import javax.activation.DataHandler;
import javax.activation.FileDataSource;
import javax.mail.MessagingException;
import javax.mail.Multipart;
import javax.mail.Transport;
import javax.mail.internet.*;

import org.apache.commons.io.IOUtils;
import org.apache.log4j.Logger;

public class EmailerService
{
    private static final Logger LOG = Logger.getLogger(EmailerService.class);
    private static final int MAX_SMS_LENGTH = 160;

    private final SimpleDateFormat sdf = new SimpleDateFormat(FriendlyIso8601DateConverter.ISO8601_FORMAT);
    
    private PersistenceService persistenceService;

    public EmailerService()
    { }

    public EmailerService(PersistenceService persistenceService)
    {
        this.persistenceService = persistenceService;
    }

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
        if(HibernateUtil.ENVIRONMENT == Environment.STAGE)
        {
            LOG.warn("Environment is STAGE, emails will not be sent");
            return true;
        }

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
            
            String s = subject;
            if(subjectPrepend.trim().length() > 0)
            {
                s = subjectPrepend + " - " + s;
            }
            message.setSubject(s);
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

    public boolean sendScheduledConferenceActiveEmail(ScheduledConference scheduledConference, String phoneNumber)
    {
        String body = getActiveMessageBody(scheduledConference.getScheduledBy().friendlyName(),
                                           scheduledConference.getNotes(), scheduledConference.getConference(),
                                           phoneNumber, scheduledConference.getStartDate(),
                                           scheduledConference.getEndDate());
        return sendScheduledConferenceEmail(scheduledConference.getActiveCallers(), scheduledConference.getTopic(), body);
    }

    public boolean sendScheduledConferencePassiveEmail(ScheduledConference scheduledConference, String phoneNumber)
    {
        String body = getPassiveMessageBody(scheduledConference.getScheduledBy().friendlyName(),
                                            scheduledConference.getNotes(), scheduledConference.getConference(),
                                            phoneNumber, scheduledConference.getStartDate(),
                                            scheduledConference.getEndDate());
        return sendScheduledConferenceEmail(scheduledConference.getPassiveCallers(), scheduledConference.getTopic(), body);
    }

    private boolean sendScheduledConferenceEmail(Set<String> addresses, String subject, String body)
    {
        InternetAddress[] toAddresses = getInternetAddresses(addresses);
        if(toAddresses.length > 0)
        {
            return sendEmail(toAddresses, body, subject, EmailerUtil.CONFERENCE_EMAIL_SUBJECT, null);
        }
        return true;
    }

    public boolean sendTestNotificationSettingsMessage(String type, String toAddress)
    {
        boolean result = true;
        InternetAddress[] toAddresses = getInternetAddresses(toAddress);
        
        if(toAddresses.length > 0)
        {
            result = sendEmail(toAddresses, type.equals("email") ? EmailerUtil.TEST_EMAIL_NOTIFICATION_BODY
                               : EmailerUtil.TEST_SMS_NOTIFICATION_BODY, "Listen Notification Test Message", "", null);
        }
        
        return result;
    }

    public void sendEmailVoicmailNotification(Voicemail voicemail, Subscriber subscriber)
    {
        boolean fileReadyForAttachment = false;
        InternetAddress[] toAddresses = getInternetAddresses(subscriber.getEmailAddress());

        if(toAddresses.length > 0)
        {
            File attachment = null;
            try
            {
                attachment = getAttachment(voicemail.getUri());
                fileReadyForAttachment = true;
            }
            catch(IOException e)
            {
                LOG.error("Error attaching voicemail", e);
            }

            Long newCount = Voicemail.countNewBySubscriber(persistenceService.getSession(), subscriber);
            String body = getEmailNotificationBody(voicemail, newCount, fileReadyForAttachment);
            String subject = String.format(EmailerUtil.EMAIL_NOTIFICATION_SUBJECT, voicemail.getLeftBy());
            sendEmail(toAddresses, body, subject, attachment);

            if(attachment != null && !attachment.delete())
            {
                LOG.error("Error removing temporary voicemail attachment file");
            }
        }
    }

    private String getEmailNotificationBody(Voicemail voicemail, Long newCount, boolean withAttachment)
    {
        StringBuilder body = new StringBuilder();
        body.append("<html><body>");
        body.append("You received a new voicemail from ").append(voicemail.getLeftBy());
        body.append(" at ").append(sdf.format(voicemail.getDateCreated())).append(".");
        body.append("<br/><br/>");
        if(voicemail.hasTranscription())
        {
            body.append("<i>Transcription:</i> ").append(voicemail.getTranscription());
            body.append("<br/><br/>");
        }
        body.append("You currently have ").append(newCount).append(" new message");
        body.append(newCount == 1 ? "." : "s.");
        body.append("<br/><br/>");
        if(withAttachment)
        {
            body.append(EmailerUtil.FILE_IS_ATTACHED);
        }
        else
        {
            body.append(EmailerUtil.FILE_NOT_ATTACHED);
        }
        body.append("</body></html>");
        return body.toString();
    }

    public void sendSmsVoicemailNotification(Voicemail voicemail, Subscriber subscriber)
    {
        sendSmsVoicemailNotification(voicemail, subscriber.getSmsAddress());
    }
    
    public void sendSmsVoicemailNotification(Voicemail voicemail, String smsAddress)
    {
        InternetAddress[] toAddresses = getInternetAddresses(smsAddress);
        
        if(toAddresses.length > 0)
        {
            String directVoicemailAccessNumber = getDirectVoicemailAccessNumber();
            String body = String.format(EmailerUtil.SMS_NOTIFICATION_BODY,
                                        AccessNumber.querySubscriberNameByAccessNumber(persistenceService.getSession(), voicemail.getLeftBy()), 
                                        voicemail.getLeftBy(), voicemail.getTranscription(), directVoicemailAccessNumber);
            
            body = truncateSMSBody(body, directVoicemailAccessNumber);
        
            sendEmail(toAddresses, body, "", body);
        }
    }

    public void sendAlternateNumberSmsVoicemailNotification(Voicemail voicemail)
    {
        HistoryService historyService = new DefaultHistoryService(persistenceService);
        String alternateNumber = Configuration.get(Property.Key.ALTERNATE_NUMBER);
        
        if(alternateNumber != null && !alternateNumber.trim().equals(""))
        {
            String pagePrefix = Configuration.get(Property.Key.PAGE_PREFIX);
            InternetAddress[] toAddresses = getInternetAddresses(alternateNumber);
            
            if(toAddresses.length > 0)
            {
                String body = String.format(EmailerUtil.ALTERNATE_NUMBER_SMS_NOTIFICATION_BODY, pagePrefix, voicemail.getLeftBy());
                
                sendEmail(toAddresses, body, "", body);
                
                historyService.writeSentVoicemailAlternatePage(voicemail);
            }
        }
    }

    private InternetAddress[] getInternetAddresses(String oneAddress)
    {
        Set<String> singleAddress = new HashSet<String>();
        singleAddress.add(oneAddress);
        
        return getInternetAddresses(singleAddress);
    }

    private InternetAddress[] getInternetAddresses(Set<String> emails)
    {
        List<InternetAddress> mailAddresses = new ArrayList<InternetAddress>();
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

    private String getActiveMessageBody(String username, String description, Conference conference, String phoneNumber,
                                        Date dateTime, Date endDateTime)
    {
        String formattedDateTime = sdf.format(dateTime);
        String formattedEndDateTime = sdf.format(endDateTime);
        String activePin = getPin(conference.getPins(), Pin.PinType.ACTIVE);
        activePin = activePin + " (Active)";
        
        String formattedEmailBody = String.format(EmailerUtil.EMAIL_BODY, username, formattedDateTime, formattedEndDateTime,
                                                  description, getPhoneNumberHtml(phoneNumber), activePin);
        
        return formattedEmailBody;
    }
    
    private String getPassiveMessageBody(String username, String description, Conference conference,
                                         String phoneNumber, Date dateTime, Date endDateTime)
    {
        String formattedDateTime = sdf.format(dateTime);
        String formattedEndDateTime = sdf.format(endDateTime);
        String passivePin = getPin(conference.getPins(), Pin.PinType.PASSIVE);
        passivePin = passivePin + " (Passive)";
        
        String formattedEmailBody = String.format(EmailerUtil.EMAIL_BODY, username, formattedDateTime, formattedEndDateTime,
                                                  description, getPhoneNumberHtml(phoneNumber), passivePin);
        
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
    
    private File getAttachment(String uri) throws IOException
    {
        String filename = getFilenameFromUri(uri);
        File file = new File(System.getProperty("java.io.tmpdir") + System.getProperty("file.separator") + filename);
        InputStream input = null;
        OutputStream output = null;

        try
        {
            URL url = ServletUtil.encodeUri(uri);
            HttpURLConnection connection = (HttpURLConnection)url.openConnection();
            connection.setRequestMethod("GET");
            connection.connect();
            input = connection.getInputStream();
            output = new FileOutputStream(file);

            IOUtils.copy(input, output);
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
    
    private String truncateSMSBody(String body, String directVoicemailAccessNumber)
    {
        String returnBody = body;
        
        if(returnBody.length() > MAX_SMS_LENGTH)
        {
            String directVoicemailAccessNumberWithPrefix = EmailerUtil.DIRECT_VOICEMAIL_HEADER + directVoicemailAccessNumber;
            returnBody = returnBody.substring(0, returnBody.length() - directVoicemailAccessNumberWithPrefix.length());
        }
        
        if(returnBody.length() > MAX_SMS_LENGTH)
        {
            returnBody = returnBody.substring(0, MAX_SMS_LENGTH - 3);
            returnBody = returnBody.concat("...");
        }
        
        return returnBody;
    }
    
    private String getPhoneNumberHtml(String phoneNumbers)
    {
        StringBuilder phoneNumbersAsHtml = new StringBuilder("<br/><ul>");
        String[] individualNumbers = phoneNumbers.split(";");
        
        for(String oneNumber : individualNumbers)
        {
            if(oneNumber.contains(":"))
            {
                String[] oneNumberParts = oneNumber.split(":");
                phoneNumbersAsHtml.append("<li>").append(oneNumberParts[1]).append(": <b>").append(oneNumberParts[0]).append("</b></li>");
            }
            else
            {
                phoneNumbersAsHtml.append("<li>").append(oneNumber).append("</li>");
            }
        }
        
        phoneNumbersAsHtml.append("</ul>");
        
        return phoneNumbersAsHtml.toString();
    }
}
