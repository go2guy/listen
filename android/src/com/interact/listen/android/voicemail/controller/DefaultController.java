package com.interact.listen.android.voicemail.controller;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.io.IOUtils;
import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.ResponseHandler;
import org.apache.http.client.methods.HttpDelete;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPut;
import org.apache.http.client.methods.HttpRequestBase;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.params.BasicHttpParams;
import org.apache.http.params.HttpConnectionParams;
import org.apache.http.params.HttpParams;
import org.apache.http.util.EntityUtils;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import android.util.Log;

import com.interact.listen.android.voicemail.Voicemail;

public class DefaultController implements Controller
{
    private static final String TAG = DefaultController.class.getName();
    private static final String BASE_64_SUBSCRIBER = "U1VCU0NSSUJFUg==";
    private static final String AUTHENTICATION_TYPE_HEADER = "X-Listen-AuthenticationType";
    private static final String USERNAME_HEADER = "X-Listen-AuthenticationUsername";
    private static final String PASSWORD_HEADER = "X-Listen-AuthenticationPassword";

    private int connectionTimeout = 5000;
    private int socketTimeout = 3000;

    @Override
    public List<Voicemail> retrieveVoicemails(String api, Long subscriberId, String username, String password) throws ControllerException,
    							ConnectionException, AuthorizationException
    {
        Map<String, String> query = new HashMap<String, String>();
        query.put("subscriber", "/subscribers/" + subscriberId);
        query.put("_fields", "id,isNew,leftBy,description,dateCreated,duration,transcription,hasNotified");
        query.put("_sortBy", "dateCreated");
        query.put("_sortOrder", "DESCENDING");

        HttpGet httpGet = new HttpGet(api + "/voicemails" + buildQueryString(query));
        httpGet.addHeader("Accept", "application/json");
        addAuthorizationHeaders(httpGet, username, password);

        HttpClient httpClient = getHttpClient();
        List<Voicemail> voicemails = new ArrayList<Voicemail>();
        try
        {
            Log.d(TAG, "Sending GET request to " + httpGet.getURI().toString());
            ControllerResponse response = httpClient.execute(httpGet, new JsonObjectResponseHandler());
            
            if(response.statusCode == 401)
            {
            	throw new AuthorizationException(api);
            }
            
            JSONObject json = response.jsonObject;
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
    public void markVoicemailsNotified(String api, long[] ids, String username, String password) throws ControllerException, ConnectionException,
    				AuthorizationException
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
                addAuthorizationHeaders(httpPut, username, password);

                Log.d(TAG, "Sending PUT request to " + httpPut.getURI().toString());
                HttpResponse response = httpClient.execute(httpPut);
                checkResponse(response, api);
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
    public void markVoicemailsRead(String api, Long[] ids, String username, String password) throws ControllerException, ConnectionException,
    				AuthorizationException
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
                addAuthorizationHeaders(httpPut, username, password);

                Log.d(TAG, "Sending PUT request to " + httpPut.getURI().toString());
                HttpResponse response = httpClient.execute(httpPut);
                checkResponse(response, api);
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
    public Long getSubscriberIdFromUsername(String api, String username, String encodedUsername, String password) throws ControllerException, 
    				ConnectionException, UserNotFoundException,	AuthorizationException
    {
        HttpClient httpClient = getHttpClient();

        Map<String, String> query = new HashMap<String, String>();
        query.put("username", username);
        query.put("_fields", "id");

        HttpGet httpGet = new HttpGet(api + "/subscribers" + buildQueryString(query));
        httpGet.addHeader("Accept", "application/json");
        addAuthorizationHeaders(httpGet, encodedUsername, password);

        Log.d(TAG, "Sending GET request to " + httpGet.getURI().toString());
        try
        {
        	ControllerResponse response = httpClient.execute(httpGet, new JsonObjectResponseHandler());
            
            if(response.statusCode == 401)
            {
            	throw new AuthorizationException(api);
            }
            
            JSONObject json = response.jsonObject;
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
    public void deleteVoicemails(String api, Long[] ids, String username, String password) throws ConnectionException, ControllerException, 
    				AuthorizationException
    {
        HttpClient httpClient = getHttpClient();

        try
        {
            for(long id : ids)
            {
                HttpDelete httpDelete = new HttpDelete(api + "/voicemails/" + id);
                httpDelete.setHeader("Accept", "application/json");
                addAuthorizationHeaders(httpDelete, username, password);

                Log.d(TAG, "Sending DELETE request to " + httpDelete.getURI().toString());
                HttpResponse response = httpClient.execute(httpDelete);
                checkResponse(response, api);
            }
        }
        catch(IOException e)
        {
            throw new ConnectionException(e, api);
        }
        catch(ControllerException e)
        {
        	throw e;
        }
    }
    
    @Override
    public String downloadVoicemailToTempFile(String api, Long id, String username, String password) throws ConnectionException,
    		ControllerException, AuthorizationException
    {
    	HttpClient httpClient = getHttpClient();
    	// remove the /api from the passed in api since we are going to /meta/audio/file
    	String modifiedApi = api.substring(0, api.lastIndexOf("/"));
    	InputStream in = null;
		FileOutputStream out = null;
		HttpEntity entity = null;
    	
    	try
    	{
    		File recordingFile = File.createTempFile("voicemail" + String.valueOf(id), ".wav");
    		out = new FileOutputStream(recordingFile);
    		
    		HttpGet httpGet = new HttpGet(modifiedApi + "/meta/audio/file/" + id);
    		addAuthorizationHeaders(httpGet, username, password);
    		
    		HttpResponse response = httpClient.execute(httpGet);
    		checkResponse(response, modifiedApi);
    		
    		entity = response.getEntity();
    		
    		in = entity.getContent();
    		
    		IOUtils.copy(in, out);
    		
    		return recordingFile.getAbsolutePath();
    	}
    	catch(IOException e)
        {
            throw new ConnectionException(e, modifiedApi);
        }
    	catch(ControllerException e)
        {
        	throw e;
        }
    	finally
    	{
    		try
    		{
    			if(out != null)
    			{
    				out.close();
    			}
    		}
    		catch(IOException e)
    		{
    			Log.e(TAG, "Error closing file output stream. Setting to null: " + e);
    			out = null;
    		}
    		
    		try
    		{
    			if(in != null)
    			{
    				in.close();
    			}
    		}
    		catch(IOException e)
    		{
    			Log.e(TAG, "Error closing input stream. Setting to null: " + e);
    			in = null;
    		}
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
    
    private void addAuthorizationHeaders(HttpRequestBase requestObject, String username, String password)
    {
    	requestObject.addHeader(AUTHENTICATION_TYPE_HEADER, BASE_64_SUBSCRIBER);
		requestObject.addHeader(USERNAME_HEADER, username);
		requestObject.addHeader(PASSWORD_HEADER, password);
    }
    
    private void checkResponse(HttpResponse response, String api) throws AuthorizationException, ControllerException
    {
    	int status = response.getStatusLine().getStatusCode();
    	
    	if(status < 200 || status > 299)
		{
			if(status == 401)
			{
				throw new AuthorizationException(api);
			}
			
			throw new ControllerException(api+" code: "+status);
		}
    }
    
    /**
     * Handles {@link HttpClient} responses, converting them to a {@link JSONObject}.
     */
    private class JsonObjectResponseHandler implements ResponseHandler<ControllerResponse>
    {
        @Override
        public ControllerResponse handleResponse(HttpResponse response) throws IOException
        {
            int status = response.getStatusLine().getStatusCode();
            if(status < 200 || status > 299)
            {
            	return new ControllerResponse(status);
            }

            HttpEntity entity = response.getEntity();
            if(entity == null)
            {
                return null;
            }

            String s = EntityUtils.toString(entity);
            try
            {
                return new ControllerResponse(status, new JSONObject(s));
            }
            catch(JSONException e)
            {
                throw new RuntimeException(e);
            }
        }
    }
    
    private static class ControllerResponse
    {
    	public ControllerResponse(int statusCode, JSONObject jsonObject)
    	{
    		this.statusCode = statusCode;
    		this.jsonObject = jsonObject;
    	}
    	
    	public ControllerResponse(int statusCode)
    	{
    		this.statusCode = statusCode;
    	}
    	
    	private JSONObject jsonObject;
    	private int statusCode;
    }
}
