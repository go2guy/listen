package com.interact.listen.gui;

import com.interact.listen.HibernateUtil;
import com.interact.listen.exception.BadRequestServletException;
import com.interact.listen.exception.ListenServletException;
import com.interact.listen.exception.UnauthorizedServletException;
import com.interact.listen.license.License;
import com.interact.listen.license.ListenFeature;
import com.interact.listen.license.NotLicensedException;
import com.interact.listen.resource.ConferenceRecording;
import com.interact.listen.resource.User;
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

public class GetConferenceRecordingServlet extends HttpServlet
{
    private static final long serialVersionUID = 1L;

    private static final Logger LOG = Logger.getLogger(GetConferenceRecordingServlet.class);

    @Override
    public void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException
    {
        if(!License.isLicensed(ListenFeature.CONFERENCING))
        {
            throw new NotLicensedException(ListenFeature.CONFERENCING);
        }

        StatSender statSender = (StatSender)request.getSession().getServletContext().getAttribute("statSender");
        if(statSender == null)
        {
            statSender = new InsaStatSender();
        }
        statSender.send(Stat.GUI_GET_CONFERENCE_RECORDING);

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

        ConferenceRecording recording = (ConferenceRecording)session.get(ConferenceRecording.class, Long.valueOf(id));
        if(!(user.getIsAdministrator() || user.getConferences().contains(recording.getConference())))
        {
            throw new UnauthorizedServletException("Not allowed to download recording");
        }

        InputStream input = null;

        try
        {
            // input
            URL url = new URL(recording.getUri());
            HttpURLConnection connection = (HttpURLConnection)url.openConnection();
            connection.setRequestMethod("GET");
            connection.connect();
            input = connection.getInputStream();

            // output
            OutputStream output = response.getOutputStream();
            response.setContentLength(Integer.parseInt(recording.getFileSize()));
            response.setContentType("audio/x-wav");
            response.setHeader("Content-disposition", "attachment; filename=" + getFileName(recording));

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
                    LOG.warn("Unable to close InputStream when reading [" + recording.getUri() + "]");
                }
            }
        }
    }

    private String getFileName(ConferenceRecording recording)
    {
        String uri = recording.getUri();
        return uri.substring(uri.lastIndexOf("/") + 1);
    }
}
