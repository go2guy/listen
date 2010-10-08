package com.interact.listen.gui;

import com.interact.listen.OutputBufferFilter;
import com.interact.listen.ServletUtil;
import com.interact.listen.config.Configuration;
import com.interact.listen.exception.ListenServletException;
import com.interact.listen.exception.UnauthorizedServletException;
import com.interact.listen.httpclient.HttpClient;
import com.interact.listen.httpclient.HttpClientImpl;
import com.interact.listen.license.License;
import com.interact.listen.license.ListenFeature;
import com.interact.listen.license.NotLicensedException;
import com.interact.listen.marshal.json.JsonMarshaller;
import com.interact.listen.resource.Subscriber;

import java.io.IOException;
import java.util.List;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.log4j.Logger;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.JSONValue;

public class GetMenuPromptsServlet extends HttpServlet
{
    private static final long serialVersionUID = 1L;

    private static final Logger LOG = Logger.getLogger(GetMenuPromptsServlet.class);

    @Override
    public void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException
    {
        if(!License.isLicensed(ListenFeature.ATTENDANT))
        {
            throw new NotLicensedException(ListenFeature.ATTENDANT);
        }

        Subscriber subscriber = ServletUtil.currentSubscriber(request);
        if(subscriber == null)
        {
            throw new UnauthorizedServletException("Not logged in");
        }

        if(!subscriber.getIsAdministrator())
        {
            throw new UnauthorizedServletException("Insufficient permissions");
        }

        try
        {
            String uri = Configuration.firstSpotSystem() + "/interact/listen/getPrompts.php";
            HttpClient client = new HttpClientImpl();

            client.get(uri);
            String entity = client.getResponseEntity();

            JSONObject json = (JSONObject)JSONValue.parse(entity);
            String status = (String)json.get("Status");
            if(!status.equals("Success"))
            {
                LOG.error("Error retrieving prompt list, reason: [" + json.get("Reason") + "]");
                throw new ListenServletException(500, "Error retrieving prompt list", "text/plain");
            }

            JSONArray files = (JSONArray)json.get("Files");
            String content = buildResponse(files);
            OutputBufferFilter.append(request, content, new JsonMarshaller().getContentType());
        }
        catch(IOException e)
        {
            throw new ListenServletException(500, e);
        }
        catch(IllegalStateException e)
        {
            LOG.error(e);
            OutputBufferFilter.append(request, "[]", new JsonMarshaller().getContentType());
        }
    }

    private String buildResponse(JSONArray prompts)
    {
        StringBuilder json = new StringBuilder("[");
        for(JSONObject prompt : (List<JSONObject>)prompts)
        {
            json.append("{");
            if(prompt.containsKey("fileName"))
            {
                String file = (String)prompt.get("fileName");
                json.append("\"file\":\"").append(file).append("\"");
            }
            json.append("},");
        }
        if(prompts.size() > 0)
        {
            json.deleteCharAt(json.length() - 1); // last ','
        }
        json.append("]");
        return json.toString();
    }
}
