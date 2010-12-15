package com.interact.listen.gui;

import com.interact.listen.HibernateUtil;
import com.interact.listen.OutputBufferFilter;
import com.interact.listen.ServletUtil;
import com.interact.listen.exception.ListenServletException;
import com.interact.listen.exception.UnauthorizedServletException;
import com.interact.listen.license.License;
import com.interact.listen.license.ListenFeature;
import com.interact.listen.license.NotLicensedException;
import com.interact.listen.resource.ConferenceRecording;
import com.interact.listen.resource.Subscriber;
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
import org.hibernate.Session;

public class GetConferenceRecordingServlet extends HttpServlet
{
    private static final long serialVersionUID = 1L;

    @Override
    public void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException
    {
        if(!License.isLicensed(ListenFeature.CONFERENCING))
        {
            throw new NotLicensedException(ListenFeature.CONFERENCING);
        }

        ServletUtil.sendStat(request, Stat.GUI_GET_CONFERENCE_RECORDING);

        Subscriber subscriber = ServletUtil.currentSubscriber(request);
        if(subscriber == null)
        {
            throw new UnauthorizedServletException("Not logged in");
        }

        Long id = ServletUtil.getNotNullLong("id", request, "Id");

        Session session = HibernateUtil.getSessionFactory().getCurrentSession();

        ConferenceRecording recording = ConferenceRecording.queryById(session, id);
        if(!subscriber.ownsConference(recording.getConference()))
        {
            throw new UnauthorizedServletException("Not allowed to download recording");
        }

        InputStream input = null;

        try
        {
            // input
            URL url = ServletUtil.encodeUri(recording.getUri());
            HttpURLConnection connection = (HttpURLConnection)url.openConnection();
            connection.setRequestMethod("GET");
            connection.connect();
            input = connection.getInputStream();

            // output
            OutputStream output = response.getOutputStream();
            response.setContentLength(Integer.parseInt(recording.getFileSize()));
            response.setContentType(recording.detectContentType());
            response.setHeader("Content-disposition", "attachment; filename=" + getFileName(recording));

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
            IOUtils.closeQuietly(input);
        }
    }

    private String getFileName(ConferenceRecording recording)
    {
        String uri = recording.getUri();
        return uri.substring(uri.lastIndexOf("/") + 1);
    }
}
