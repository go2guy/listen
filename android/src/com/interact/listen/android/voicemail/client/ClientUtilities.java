package com.interact.listen.android.voicemail.client;

import android.accounts.AccountManager;
import android.net.Uri;
import android.os.Handler;
import android.util.Log;

import com.interact.listen.android.voicemail.Constants;
import com.interact.listen.android.voicemail.Voicemail;

import java.io.IOException;
import java.io.InputStream;
import java.io.UnsupportedEncodingException;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.List;

import org.apache.commons.codec.binary.Base64;
import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.ResponseHandler;
import org.apache.http.client.methods.HttpDelete;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPut;
import org.apache.http.client.methods.HttpRequestBase;
import org.apache.http.conn.params.ConnManagerParams;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.params.HttpConnectionParams;
import org.apache.http.params.HttpParams;
import org.apache.http.util.EntityUtils;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

public final class ClientUtilities
{
    private static final String TAG = Constants.TAG + "ClientUtilities";
    
    private static final String BASE_64_SUBSCRIBER = "U1VCU0NSSUJFUg==";
    private static final String AUTHENTICATION_TYPE_HEADER = "X-Listen-AuthenticationType";
    private static final String USERNAME_HEADER = "X-Listen-AuthenticationUsername";
    private static final String PASSWORD_HEADER = "X-Listen-AuthenticationPassword";
    private static final int REGISTRATION_TIMEOUT = 20 * 1000; // ms

    private static final String SUBSCRIBER_PATH = "api/subscribers";
    private static final String VOICEMAIL_PATH = "api/voicemails";
    private static final String AUDIO_PATH = "meta/audio/file";
    
    private static HttpClient mHttpClient;

    private ClientUtilities()
    {
    }

    /**
     * Executes the network requests on a separate thread.
     * 
     * @param runnable The runnable instance containing network operations to be executed.
     */
    public static Thread performOnBackgroundThread(final Runnable runnable)
    {
        final Thread t = new Thread()
        {
            @Override
            public void run()
            {
                try
                {
                    runnable.run();
                }
                finally
                {
                    Log.i(TAG, "background task complete");
                }
            }
        };
        t.start();
        return t;
    }

    public static String getBase64EncodedString(String stringToEncode)
    {
        return new String(Base64.encodeBase64(stringToEncode.getBytes()));
    }

    /**
     * Connects to the Listen server, authenticates the provided base Uri, username, and password.
     * 
     * handler and context should both be provided or both be null.
     * 
     * @param host The base Uri
     * @param username The user's username
     * @param password The user's password
     * @param handler The hander instance from the calling UI thread.
     * @param context The handler of the result
     * @return long User I.D., 0 on not found, or < 0 otherwise
     */
    public static long authenticate(Uri host, String username, String password, Handler handler, OnAuthenticateHandler context)
    {
        Uri apiUri = null;
        try
        {
            Uri.Builder builder = host.buildUpon();
            builder.appendEncodedPath(SUBSCRIBER_PATH);
            builder.appendQueryParameter("username", username);
            builder.appendQueryParameter("_fields", "id");
    
            apiUri = builder.build();
        }
        catch(Exception e)
        {
            Log.i(TAG, "error creating URI", e);
            return sendAuthResult(-2, AccountManager.ERROR_CODE_BAD_ARGUMENTS, handler, context);
        }
        
        HttpGet httpGet = new HttpGet(apiUri.toString());
        httpGet.addHeader("Accept", "application/json");
        addAuthorizationHeaders(httpGet, username, getBase64EncodedString(password));

        Log.i(TAG, "Authenticating to " + httpGet.getURI().toString());
        maybeCreateHttpClient();
        
        try
        {
            ControllerResponse response = mHttpClient.execute(httpGet, new JsonObjectResponseHandler());
            response.throwExceptions();

            JSONObject json = response.jsonObject;
            if(json == null)
            {
                Log.i(TAG, "JSON object is null", response.jsonException);
                return sendAuthResult(-2, AccountManager.ERROR_CODE_INVALID_RESPONSE, handler, context);
            }

            int total = json.getInt("total");
            if (total != 1)
            {
                Log.i(TAG, "User not found");
                return sendAuthResult(0, 0, handler, context);
            }

            JSONArray subscribers = json.getJSONArray("results");
            JSONObject subscriber = subscribers.getJSONObject(0);
            long id = subscriber.getLong("id");
            
            Log.i(TAG, "succesfull authentication: " + id);
            return sendAuthResult(id, 0, handler, context);
        }
        catch(IOException e)
        {
            Log.i(TAG, "IOException when authorizing", e);
            return sendAuthResult(-2, AccountManager.ERROR_CODE_NETWORK_ERROR, handler, context);
        }
        catch(JSONException e)
        {
            Log.i(TAG, "JSONException when authorizing", e);
            return sendAuthResult(-2, AccountManager.ERROR_CODE_INVALID_RESPONSE, handler, context);
        }
        catch(AuthorizationException e)
        {
            Log.i(TAG, "Unauthorized authentication");
            return sendAuthResult(-1, 0, handler, context);
        }
        catch(Exception e)
        {
            Log.i(TAG, "Exception when authorizing", e);
            return sendAuthResult(-2, AccountManager.ERROR_CODE_BAD_ARGUMENTS, handler, context);
        }
    }

    /**
     * Sends the authentication response from server back to the caller main UI thread through its handler.
     * 
     * @param result The holding authentication result
     * @param errorCode AccountManager error code
     * @param handler The main UI thread's handler instance.
     * @param context The caller Activity's context.
     * @return the result value passed in
     */
    private static long sendAuthResult(final long result, final int errorCode, final Handler handler, final OnAuthenticateHandler context)
    {
        if(handler != null && context != null)
        {
            handler.post(new Runnable()
            {
                public void run()
                {
                    context.onAuthenticationResult(result, errorCode);
                }
            });
        }
        return result;
    }

    /**
     * Attempts to authenticate the user credentials on the server.
     * 
     * @param host The base URL
     * @param username The user's username
     * @param password The user's password to be authenticated
     * @param handler The main UI thread's handler instance.
     * @param context The caller Activity's context
     * @return Thread The thread on which the network mOperations are executed.
     */
    public static Thread attemptAuth(final Uri host, final String username, final String password, final Handler handler,
                                     final OnAuthenticateHandler context)
    {
        final Runnable runnable = new Runnable()
        {
            public void run()
            {
                authenticate(host, username, password, handler, context);
            }
        };
        // run on background thread.
        return ClientUtilities.performOnBackgroundThread(runnable);
    }

    private static void addAuthorizationHeaders(HttpRequestBase requestObject, String username, String authToken)
    {
        requestObject.addHeader(AUTHENTICATION_TYPE_HEADER, BASE_64_SUBSCRIBER);
        requestObject.addHeader(USERNAME_HEADER, getBase64EncodedString(username));
        requestObject.addHeader(PASSWORD_HEADER, authToken);
    }

    public static List<Voicemail> retrieveVoicemails(Uri host, long subscriberID, String username, String authToken,
                                                     boolean forView) throws IOException, AuthorizationException
    {
        Uri apiUri = null;
        try
        {
            Uri.Builder builder = host.buildUpon();
            builder.appendEncodedPath(VOICEMAIL_PATH);
            builder.appendQueryParameter("subscriber", "/subscribers/" + subscriberID);
            builder.appendQueryParameter("_fields", "id,isNew,leftBy,description,dateCreated,duration,transcription,hasNotified");
            if(forView)
            {
                builder.appendQueryParameter("_sortBy", "dateCreated");
                builder.appendQueryParameter("_sortOrder", "DESCENDING");
            }
            else
            {
                builder.appendQueryParameter("_sortBy", "id");
                builder.appendQueryParameter("_sortOrder", "ASCENDING");
            }
            apiUri = builder.build();
        }
        catch(Exception e)
        {
            Log.i(TAG, "error creating URI", e);
            return null;
        }
        
        HttpGet httpGet = new HttpGet(apiUri.toString());
        httpGet.addHeader("Accept", "application/json");
        addAuthorizationHeaders(httpGet, username, authToken);

        Log.i(TAG, "Retrieving voicemails " + httpGet.getURI().toString());
        maybeCreateHttpClient();

        List<Voicemail> voicemails = new ArrayList<Voicemail>();
        try
        {
            ControllerResponse response = mHttpClient.execute(httpGet, new JsonObjectResponseHandler());
            response.throwExceptions();
            
            JSONObject json = response.jsonObject;
            JSONArray results = (JSONArray)json.get("results");
            for(int i = 0, total = json.getInt("total"); i < total; i++)
            {
                JSONObject result = results.getJSONObject(i);
                try
                {
                    Voicemail v = Voicemail.parse(username, result);
                    voicemails.add(v);
                }
                catch(JSONException je)
                {
                    Log.e(TAG, "JSONException on index " + i, je);
                }
                catch(ParseException pe)
                {
                    Log.e(TAG, "ParseException on index " + i, pe);
                }
            }
            Log.d(TAG, "Retrieved " + voicemails.size() + " voicemails from controller");
        }
        catch(JSONException e)
        {
            Log.e(TAG, "JSONException overall", e);
            return null;
        }
        return voicemails;
    }

    private static boolean putUpdate(Uri host, long voicemailID, String username, String authToken, JSONObject json)
        throws IOException, AuthorizationException
    {
        Uri apiUri = null;
        try
        {
            Uri.Builder builder = host.buildUpon();
            builder.appendEncodedPath(VOICEMAIL_PATH);
            builder.appendEncodedPath(Long.toString(voicemailID));
            apiUri = builder.build();
        }
        catch(Exception e)
        {
            Log.i(TAG, "error creating URI", e);
            return false;
        }
    
        try
        {
            HttpEntity entity = new StringEntity(json.toString(), "UTF-8");
    
            HttpPut httpPut = new HttpPut(apiUri.toString());
            httpPut.setEntity(entity);
            httpPut.setHeader("Accept", "application/json");
            httpPut.setHeader("Content-Type", "application/json");
            addAuthorizationHeaders(httpPut, username, authToken);
    
            Log.d(TAG, "Sending PUT request " + httpPut.getURI().toString());
            maybeCreateHttpClient();
    
            HttpResponse response = mHttpClient.execute(httpPut);

            int status = response.getStatusLine().getStatusCode();
            if(status < 200 || status > 299)
            {
                if(status == 401)
                {
                    throw new AuthorizationException();
                }
                Log.e(TAG, "error sending voicemail update to server " + voicemailID);
                return false;
            }
            return true;
        }
        catch(UnsupportedEncodingException e)
        {
            Log.e(TAG, "unsupported encoding exception", e);
            return false;
        }
    }

    public static boolean markVoicemailNotified(Uri host, long voicemailID, String username, String authToken, boolean isNotified)
        throws IOException, AuthorizationException
    {
        try
        {
            JSONObject json = new JSONObject();
            json.put("hasNotified", isNotified);
            
            return putUpdate(host, voicemailID, username, authToken, json);
        }
        catch(JSONException e)
        {
            Log.e(TAG, "has notified json exception", e);
            return false;
        }
    }

    public static boolean markVoicemailRead(Uri host, long voicemailID, String username, String authToken, boolean isRead)
        throws IOException, AuthorizationException
    {
        try
        {
            JSONObject json = new JSONObject();
            json.put("isNew", !isRead);
            
            return putUpdate(host, voicemailID, username, authToken, json);
        }
        catch(JSONException e)
        {
            Log.e(TAG, "has notified json exception", e);
            return false;
        }
    }

    public static boolean deleteVoicemail(Uri host, long voicemailID, String username, String authToken)
        throws IOException, AuthorizationException
    {
        Uri apiUri = null;
        try
        {
            Uri.Builder builder = host.buildUpon();
            builder.appendEncodedPath(VOICEMAIL_PATH);
            builder.appendEncodedPath(Long.toString(voicemailID));
            apiUri = builder.build();
        }
        catch(Exception e)
        {
            Log.i(TAG, "error creating URI", e);
            return false;
        }

        maybeCreateHttpClient();

        HttpDelete httpDelete = new HttpDelete(apiUri.toString());
        httpDelete.setHeader("Accept", "application/json");
        addAuthorizationHeaders(httpDelete, username, authToken);

        Log.d(TAG, "Sending DELETE request to " + httpDelete.getURI().toString());
        
        HttpResponse response = mHttpClient.execute(httpDelete);
        
        int status = response.getStatusLine().getStatusCode();
        if(status < 200 || status > 299)
        {
            if(status == 401)
            {
                throw new AuthorizationException();
            }
            Log.e(TAG, "error sending voicemail delete to server " + voicemailID);
            return false;
        }
        return true;
    }

    public static HttpEntity getVoicemailInput(Uri host, long voicemailID, String username, String authToken)
        throws IOException, AuthorizationException
    {
        Uri apiUri = null;
        try
        {
            Uri.Builder builder = host.buildUpon();
            builder.appendEncodedPath(AUDIO_PATH);
            builder.appendEncodedPath(Long.toString(voicemailID));
            apiUri = builder.build();
        }
        catch(Exception e)
        {
            Log.i(TAG, "error creating URI", e);
            return null;
        }

        maybeCreateHttpClient();

        Log.i(TAG, "getting audio file " + apiUri + " " + username);
        HttpGet httpGet = new HttpGet(apiUri.toString());
        addAuthorizationHeaders(httpGet, username, authToken);

        HttpResponse response = mHttpClient.execute(httpGet);

        int status = response.getStatusLine().getStatusCode();
        if(status < 200 || status > 299)
        {
            if(status == 401)
            {
                throw new AuthorizationException(apiUri + " [" + username + "]");
            }
            Log.e(TAG, "error getting audio " + voicemailID);
            return null;
        }

        return response.getEntity();
    }
    
    private static void maybeCreateHttpClient()
    {
        if(mHttpClient == null)
        {
            mHttpClient = new DefaultHttpClient();
            final HttpParams params = mHttpClient.getParams();
            HttpConnectionParams.setConnectionTimeout(params, REGISTRATION_TIMEOUT);
            HttpConnectionParams.setSoTimeout(params, REGISTRATION_TIMEOUT);
            ConnManagerParams.setTimeout(params, REGISTRATION_TIMEOUT);
        }
    }

    /**
     * Handles {@link HttpClient} responses, converting them to a {@link JSONObject}.
     */
    private static class JsonObjectResponseHandler implements ResponseHandler<ControllerResponse>
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

            String entityString = EntityUtils.toString(entity);
            return new ControllerResponse(status, entityString);
        }
    }
    
    private static final class ControllerResponse
    {
        private JSONObject jsonObject;
        private JSONException jsonException;
        private int statusCode;

        private ControllerResponse(int statusCode, String entityString)
        {
            this.statusCode = statusCode;
            try
            {
                this.jsonObject = new JSONObject(entityString);
                this.jsonException = null;
            }
            catch(JSONException e)
            {
                this.jsonException = e;
                this.jsonObject = null;
            }
        }
        
        private ControllerResponse(int statusCode)
        {
            this.statusCode = statusCode;
            this.jsonObject = null;
            this.jsonException = null;
        }
        
        private void throwExceptions() throws JSONException, AuthorizationException
        {
            if (jsonException != null)
            {
                throw jsonException;
            }
            if(statusCode == 401)
            {
                throw new AuthorizationException();
            }
        }
    }

}
