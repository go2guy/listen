package com.interact.listen.api;

import com.interact.listen.*;
import com.interact.listen.api.security.AuthenticationFilter;
import com.interact.listen.api.security.AuthenticationFilter.Authentication;
import com.interact.listen.exception.BadRequestServletException;
import com.interact.listen.exception.ListenServletException;
import com.interact.listen.exception.UnauthorizedServletException;
import com.interact.listen.gui.DownloadVoicemailServlet;
import com.interact.listen.history.Channel;
import com.interact.listen.history.DefaultHistoryService;
import com.interact.listen.history.HistoryService;
import com.interact.listen.license.License;
import com.interact.listen.license.ListenFeature;
import com.interact.listen.license.NotLicensedException;
import com.interact.listen.resource.Audio;
import com.interact.listen.resource.Subscriber;
import com.interact.listen.resource.Voicemail;
import com.interact.listen.stats.Stat;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.io.IOUtils;
import org.apache.log4j.Logger;
import org.hibernate.Session;

public class GetAudioFileServlet extends HttpServlet
{
    private static final long serialVersionUID = 1L;

    private static final Logger LOG = Logger.getLogger(DownloadVoicemailServlet.class);

    @Override
    public void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException
    {
        if(!License.isLicensed(ListenFeature.VOICEMAIL))
        {
            throw new NotLicensedException(ListenFeature.VOICEMAIL);
        }
        
        ServletUtil.sendStat(request, Stat.GUI_DOWNLOAD_VOICEMAIL);

        Authentication authentication = (Authentication)request.getAttribute(AuthenticationFilter.AUTHENTICATION_KEY); 
        Subscriber subscriber = authentication.getSubscriber();
        
        if(subscriber == null)
        {
            throw new UnauthorizedServletException("Not logged in");
        }

        String id = ((HttpServletRequest)request).getPathInfo().substring(1);
        if(id == null || id.trim().equals(""))
        {
            throw new BadRequestServletException("Please provide an id");
        }
        
        Session session = HibernateUtil.getSessionFactory().getCurrentSession();
        PersistenceService persistenceService = new DefaultPersistenceService(session, subscriber, Channel.TUI);

        Voicemail voicemail = (Voicemail)session.get(Voicemail.class, Long.valueOf(id));

        InputStream input = null;

        try
        {
            // input
            URL url = ServletUtil.encodeUri(voicemail.getUri());
            HttpURLConnection connection = (HttpURLConnection)url.openConnection();
            connection.setRequestMethod("GET");
            connection.connect();
            input = connection.getInputStream();

            // output
            OutputStream output = response.getOutputStream();
            response.setContentLength(Integer.parseInt(voicemail.getFileSize()));
            response.setContentType(voicemail.detectContentType());
            response.setHeader("Content-disposition", "attachment; filename=" + getFileName(voicemail));

            HistoryService historyService = new DefaultHistoryService(persistenceService);
            historyService.writeDownloadedVoicemail(voicemail);

            request.setAttribute(OutputBufferFilter.OUTPUT_SUPPRESS_KEY, Boolean.TRUE);
            IOUtils.copy(input, output);
        }
        catch(MalformedURLException e)
        {
            request.setAttribute(OutputBufferFilter.OUTPUT_SUPPRESS_KEY, Boolean.FALSE);
            throw new ListenServletException(HttpServletResponse.SC_INTERNAL_SERVER_ERROR, e);
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
                    LOG.warn("Unable to close InputStream when reading [" + voicemail.getUri() + "]");
                }
            }
        }
    }

    private String getFileName(Audio audio)
    {
        String uri = audio.getUri();
        return uri.substring(uri.lastIndexOf("/") + 1);
    }
}
