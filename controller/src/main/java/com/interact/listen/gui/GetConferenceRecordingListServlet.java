package com.interact.listen.gui;

import com.interact.listen.HibernateUtil;
import com.interact.listen.OutputBufferFilter;
import com.interact.listen.marshal.Marshaller;
import com.interact.listen.marshal.converter.FriendlyIso8601DateConverter;
import com.interact.listen.marshal.json.JsonMarshaller;
import com.interact.listen.resource.Conference;
import com.interact.listen.resource.ConferenceRecording;
import com.interact.listen.resource.Resource;
import com.interact.listen.stats.Stat;

import java.util.Date;
import java.util.List;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.hibernate.Session;

public class GetConferenceRecordingListServlet extends HttpServlet
{
    private static final long serialVersionUID = 1L;

    @Override
    public void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException
    {
        Conference conference = GetConferenceParticipantsServlet.validateAndGetConference(request,
                                                                                          Stat.GUI_GET_CONFERENCE_RECORDING_LIST);

        int first = 0;
        int max = Resource.DEFAULT_PAGE_SIZE;
        if(request.getParameter("first") != null)
        {
            first = Integer.parseInt(request.getParameter("first"));
        }
        if(request.getParameter("max") != null)
        {
            max = Integer.parseInt(request.getParameter("max"));
        }

        Session session = HibernateUtil.getSessionFactory().getCurrentSession();
        List<ConferenceRecording> results = ConferenceRecording.queryByConferencePaged(session, conference, first, max);
        long total = results.size() > 0 ? ConferenceRecording.countByConference(session, conference) : 0;

        Marshaller marshaller = new JsonMarshaller();
        marshaller.registerConverterClass(Date.class, FriendlyIso8601DateConverter.class);

        StringBuilder json = new StringBuilder();
        json.append("{");
        json.append("\"first\":").append(first).append(",");
        json.append("\"max\":").append(max).append(",");
        json.append("\"count\":").append(results.size()).append(",");
        json.append("\"total\":").append(total).append(",");
        json.append("\"results\":[");
        for(ConferenceRecording result : results)
        {
            json.append(marshalConferenceRecording(result, marshaller));
            json.append(",");
        }
        if(results.size() > 0)
        {
            json.deleteCharAt(json.length() - 1);
        }
        json.append("]}");
        OutputBufferFilter.append(request, json.toString(), marshaller.getContentType());
    }

    private String marshalConferenceRecording(ConferenceRecording recording, Marshaller marshaller)
    {
        StringBuilder json = new StringBuilder();
        json.append("{");

        json.append("\"id\":").append(recording.getId()).append(",");

        String dateCreated = marshaller.convertAndEscape(Date.class, recording.getDateCreated());
        json.append("\"dateCreated\":\"").append(dateCreated).append("\",");

        String description = marshaller.convertAndEscape(String.class, recording.getDescription());
        json.append("\"description\":\"").append(description).append("\",");

        String duration = marshaller.convertAndEscape(String.class, recording.getDuration());
        json.append("\"duration\":\"").append(duration).append("\",");

        String fileSize = marshaller.convertAndEscape(String.class, recording.getFileSize());
        json.append("\"fileSize\":\"").append(fileSize).append("\"");

        json.append("}");
        return json.toString();
    }
}
