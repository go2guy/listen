package com.interact.listen.gui;

import com.interact.listen.HibernateUtil;
import com.interact.listen.PersistenceService;
import com.interact.listen.exception.BadRequestServletException;
import com.interact.listen.exception.ListenServletException;
import com.interact.listen.exception.UnauthorizedServletException;
import com.interact.listen.license.License;
import com.interact.listen.license.ListenFeature;
import com.interact.listen.license.NotLicensedException;
import com.interact.listen.resource.Audio;
import com.interact.listen.resource.User;
import com.interact.listen.resource.Voicemail;
import com.interact.listen.stats.InsaStatSender;
import com.interact.listen.stats.Stat;
import com.interact.listen.stats.StatSender;

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

public class DownloadVoicemailServlet extends HttpServlet
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

        StatSender statSender = (StatSender)request.getSession().getServletContext().getAttribute("statSender");
        if(statSender == null)
        {
            statSender = new InsaStatSender();
        }
        statSender.send(Stat.GUI_DOWNLOAD_VOICEMAIL);

        User user = (User)(request.getSession().getAttribute("user"));
        if(user == null)
        {
            throw new UnauthorizedServletException("Not logged in");
        }

        String id = request.getParameter("id");
        if(id == null || id.trim().equals(""))
        {
            throw new BadRequestServletException("Please provide an id");
        }

        Session session = HibernateUtil.getSessionFactory().getCurrentSession();
        PersistenceService persistenceService = new PersistenceService(session);

        Voicemail voicemail = (Voicemail)session.get(Voicemail.class, Long.valueOf(id));
        if(!(user.getIsAdministrator() || user.getSubscriber() != null || user.getSubscriber()
                                                                              .equals(voicemail.getSubscriber())))
        {
            throw new UnauthorizedServletException("Not allowed to download voicemail");
        }

        InputStream input = null;

        try
        {
            // input
            URL url = new URL(voicemail.getUri());
            HttpURLConnection connection = (HttpURLConnection)url.openConnection();
            connection.setRequestMethod("GET");
            connection.connect();
            input = connection.getInputStream();

            // output
            OutputStream output = response.getOutputStream();
            response.setContentLength(Integer.parseInt(voicemail.getFileSize()));
            response.setContentType("audio/x-wav");
            response.setHeader("Content-disposition", "attachment; filename=" + getFileName(voicemail));

            Voicemail original = voicemail.copy(false);
            voicemail.setIsNew(false);
            persistenceService.update(voicemail, original);

            IOUtils.copy(input, output);
        }
        catch(MalformedURLException e)
        {
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
