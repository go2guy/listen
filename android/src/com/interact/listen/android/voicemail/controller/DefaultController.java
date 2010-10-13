package com.interact.listen.android.voicemail.controller;

import android.util.Log;

import com.interact.listen.android.voicemail.Voicemail;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.util.*;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.HttpClient;
import org.apache.http.client.ResponseHandler;
import org.apache.http.client.methods.HttpDelete;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPut;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.params.BasicHttpParams;
import org.apache.http.params.HttpConnectionParams;
import org.apache.http.params.HttpParams;
import org.apache.http.util.EntityUtils;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

public class DefaultController implements Controller
{
    private static final String TAG = DefaultController.class.getName();

    private int connectionTimeout = 5000;
    private int socketTimeout = 3000;

    @Override
    public List<Voicemail> retrieveVoicemails(String api, Long subscriberId) throws ControllerException, ConnectionException
    {
        Map<String, String> query = new HashMap<String, String>();
        query.put("subscriber", "/subscribers/" + subscriberId);
        query.put("_fields", "id,isNew,leftBy,description,dateCreated,duration,transcription,hasNotified");
        query.put("_sortBy", "dateCreated");
        query.put("_sortOrder", "DESCENDING");

        HttpGet httpGet = new HttpGet(api + "/voicemails" + buildQueryString(query));
        httpGet.addHeader("Accept", "application/json");

        HttpClient httpClient = getHttpClient();
        List<Voicemail> voicemails = new ArrayList<Voicemail>();
        try
        {
            Log.d(TAG, "Sending GET request to " + httpGet.getURI().toString());
            JSONObject json = httpClient.execute(httpGet, new JsonObjectResponseHandler());
            JSONArray results = (JSONArray)json.get("results");
            for(int i = 0, total = json.getInt("total"); i < total; i++)
            {
                JSONObject result = results.getJSONObject(i);
                Voicemail v = new Voicemail(result.getString("id"),
                                            result.getString("isNew"),
                                            result.getString("leftBy"),
                                            result.getString("description"),
                                            result.getString("dateCreated"),
                                            result.getString("duration"),
                                            result.getString("transcription"),
                                            result.getString("hasNotified"));
                voicemails.add(v);
            }
            Log.d(TAG, "Retrieved " + voicemails.size() + " voicemails from controller");
        }
        catch(IOException e)
        {
            throw new ConnectionException(e, api);
        }
        catch(JSONException e)
        {
            throw new ControllerException(e);
        }
        return voicemails;
    }

    @Override
    public void markVoicemailsNotified(String api, long[] ids) throws ControllerException, ConnectionException
    {
        HttpClient httpClient = getHttpClient();

        try
        {
            JSONObject voicemail = new JSONObject();
            voicemail.put("hasNotified", true);

            HttpEntity entity = new StringEntity(voicemail.toString(), "UTF-8");
            for(int i = 0; i < ids.length; i++)
            {
                HttpPut httpPut = new HttpPut(api + "/voicemails/" + ids[i]);
                httpPut.setEntity(entity);
                httpPut.setHeader("Accept", "application/json");
                httpPut.setHeader("Content-Type", "application/json");

                Log.d(TAG, "Sending PUT request to " + httpPut.getURI().toString());
                httpClient.execute(httpPut);
            }
        }
        catch(UnsupportedEncodingException e)
        {
            throw new ControllerException(e);
        }
        catch(IOException e)
        {
            throw new ConnectionException(e, api);
        }
        catch(JSONException e)
        {
            throw new ControllerException(e);
        }
    }

    @Override
    public void markVoicemailsRead(String api, Long[] ids) throws ControllerException, ConnectionException
    {
        HttpClient httpClient = getHttpClient();

        try
        {
            JSONObject voicemail = new JSONObject();
            voicemail.put("isNew", false);

            HttpEntity entity = new StringEntity(voicemail.toString(), "UTF-8");
            for(long id : ids)
            {
                HttpPut httpPut = new HttpPut(api + "/voicemails/" + id);
                httpPut.setEntity(entity);
                httpPut.setHeader("Accept", "application/json");
                httpPut.setHeader("Content-Type", "application/json");

                Log.d(TAG, "Sending PUT request to " + httpPut.getURI().toString());
                httpClient.execute(httpPut);
            }
        }
        catch(UnsupportedEncodingException e)
        {
            throw new ControllerException(e);
        }
        catch(IOException e)
        {
            throw new ConnectionException(e, api);
        }
        catch(JSONException e)
        {
            throw new ControllerException(e);
        }
    }
    
    @Override
    public Long getSubscriberIdFromUsername(String api, String username) throws ControllerException, ConnectionException, UserNotFoundException
    {
        HttpClient httpClient = getHttpClient();

        Map<String, String> query = new HashMap<String, String>();
        query.put("username", username);
        query.put("_fields", "id");

        HttpGet httpGet = new HttpGet(api + "/subscribers" + buildQueryString(query));
        httpGet.addHeader("Accept", "application/json");

        Log.d(TAG, "Sending GET request to " + httpGet.getURI().toString());
        try
        {
            JSONObject json = httpClient.execute(httpGet, new JsonObjectResponseHandler());
            int total = json.getInt("total");

            if(total == 1)
            {
                JSONArray subscribers = json.getJSONArray("results");
                JSONObject subscriber = subscribers.getJSONObject(0);
                return subscriber.getLong("id");
            }
        }
        catch(IOException e)
        {
            throw new ConnectionException(e, api);
        }
        catch(JSONException e)
        {
            throw new ControllerException(e);
        }

        throw new UserNotFoundException(username);
    }

    @Override
    public void deleteVoicemails(String api, Long[] ids) throws ConnectionException
    {
        HttpClient httpClient = getHttpClient();

        try
        {
            for(long id : ids)
            {
                HttpDelete httpDelete = new HttpDelete(api + "/voicemails/" + id);
                httpDelete.setHeader("Accept", "application/json");

                Log.d(TAG, "Sending DELETE request to " + httpDelete.getURI().toString());
                httpClient.execute(httpDelete);
            }
        }
        catch(IOException e)
        {
            throw new ConnectionException(e, api);
        }
    }

    private HttpClient getHttpClient()
    {
        HttpParams params = new BasicHttpParams();
        HttpConnectionParams.setConnectionTimeout(params, connectionTimeout);
        HttpConnectionParams.setSoTimeout(params, socketTimeout);
        return new DefaultHttpClient(params);
    }

    private String buildQueryString(Map<String, String> params)
    {
        if(params == null || params.size() == 0)
        {
            return "";
        }

        StringBuilder builder = new StringBuilder("?");
        for(Map.Entry<String, String> entry : params.entrySet())
        {
            builder.append(entry.getKey()).append("=").append(entry.getValue()).append("&");
        }
        builder.deleteCharAt(builder.length() - 1); // remove last '&'
        return builder.toString();
    }
    
    /**
     * Handles {@link HttpClient} responses, converting them to a {@link JSONObject}.
     */
    private class JsonObjectResponseHandler implements ResponseHandler<JSONObject>
    {
        @Override
        public JSONObject handleResponse(HttpResponse response) throws IOException
        {
            int status = response.getStatusLine().getStatusCode();
            if(status < 200 || status > 299)
            {
                // TODO log or throw
                return null;
            }

            HttpEntity entity = response.getEntity();
            if(entity == null)
            {
                return null;
            }

            String s = EntityUtils.toString(entity);
            try
            {
                return new JSONObject(s);
            }
            catch(JSONException e)
            {
                throw new RuntimeException(e);
            }
        }
    }
}
