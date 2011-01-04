package com.interact.listen.android.voicemail.client;

import android.accounts.AccountManager;
import android.net.Uri;
import android.net.http.AndroidHttpClient;
import android.os.Handler;
import android.text.TextUtils;
import android.util.Log;

import com.interact.listen.android.voicemail.Constants;
import com.interact.listen.android.voicemail.Voicemail;
import com.interact.listen.android.voicemail.contact.ContactMIME;
import com.interact.listen.android.voicemail.contact.ListenContacts;
import com.interact.listen.android.voicemail.sync.Authority;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.io.UnsupportedEncodingException;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.List;
import java.util.zip.GZIPInputStream;

import org.apache.commons.codec.binary.Base64;
import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.ResponseHandler;
import org.apache.http.client.methods.HttpDelete;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPut;
import org.apache.http.client.methods.HttpRequestBase;
import org.apache.http.entity.StringEntity;
import org.apache.http.protocol.HTTP;
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
    private static final String DEVICEID_HEADER = "X-Listen-DeviceID";
    
    private static final String SUBSCRIBER_PATH = "api/subscribers";
    private static final String VOICEMAIL_PATH = "api/voicemails";
    private static final String NEXT_API_PREPEND_PATH = "api";
    private static final String REGISTER_PATH = "meta/registerDevice";
    private static final String AUDIO_PATH = "meta/audio/file";
    private static final String EMAILS_PATH = "meta/contacts/emailContacts";
    private static final String NUMBERS_PATH = "meta/contacts/numberContacts";
    private static final String NEXT_CONTACT_PREPEND_PATH = "meta/contacts";

    private static final int MAX_PAGED_GET = 100;

    private static final String DEVICE_TYPE = "ANDROID";

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

    private static void addDeviceIDHeader(HttpRequestBase requestObject, String deviceId)
    {
        requestObject.addHeader(DEVICEID_HEADER, deviceId);
    }
    
    private static void addAuthorizationHeaders(HttpRequestBase requestObject, String username, String authToken)
    {
        requestObject.addHeader(AUTHENTICATION_TYPE_HEADER, BASE_64_SUBSCRIBER);
        requestObject.addHeader(USERNAME_HEADER, getBase64EncodedString(username));
        requestObject.addHeader(PASSWORD_HEADER, authToken);
    }

    public static Uri getPagedVoicemails(AccountInfo aInfo, boolean forView, Uri nextRef,
                                         List<Voicemail> voicemails, Long[] syncTime)
        throws IOException, AuthorizationException
    {
        Uri apiUri = null;
        if(nextRef == null)
        {
            try
            {
                Uri.Builder builder = aInfo.getHost().buildUpon();
                builder.appendEncodedPath(VOICEMAIL_PATH);
                builder.appendQueryParameter("subscriber", "/subscribers/" + aInfo.getUserID());
                builder.appendQueryParameter("_first", Integer.toString(0));
                builder.appendQueryParameter("_max", Integer.toString(MAX_PAGED_GET));
                builder.appendQueryParameter("_fields", "id,isNew,leftBy,leftByName,description,dateCreated,duration,transcription,hasNotified");
                
                if(syncTime != null && syncTime[0] > 0)
                {
                    builder.appendQueryParameter("_after", Long.toString(syncTime[0]));
                }
                
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
        }
        else
        {
            apiUri = nextRef;
        }
        
        HttpGet httpGet = new HttpGet(apiUri.toString());
        httpGet.addHeader("Accept", "application/json");
        AndroidHttpClient.modifyRequestToAcceptGzipResponse(httpGet);

        addAuthorizationHeaders(httpGet, aInfo.getName(), aInfo.getAuthToken());

        Log.i(TAG, "Retrieving voicemails " + httpGet.getURI().toString());
        maybeCreateHttpClient();

        String next = null;
        
        try
        {
            ControllerResponse response = mHttpClient.execute(httpGet, new JsonObjectResponseHandler());
            response.throwExceptions();
            
            JSONObject json = response.jsonObject;
            if(json == null)
            {
                throw new JSONException("JSON object not set");
            }
            
            next = json.has("next") ? json.getString("next") : null;
            int total = json.getInt("total");
            int count = json.getInt("count");
            JSONArray results = (JSONArray)json.get("results");
            
            Log.i(TAG, "Results array total: " + total + " array: " + results.length() + " next: " + next + " count: " + count);
            
            for(int i = 0; i < count; i++)
            {
                JSONObject result = results.getJSONObject(i);
                try
                {
                    Voicemail v = Voicemail.parse(aInfo.getName(), result);
                    voicemails.add(v);
                    if(syncTime != null)
                    {
                        syncTime[1] = Math.max(syncTime[1], v.getDateCreatedMS());
                    }
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
        
        return getNextUri(aInfo, next, NEXT_API_PREPEND_PATH);
    }

    public static List<Voicemail> retrieveVoicemails(AccountInfo aInfo, Long[] syncTime)
        throws IOException, AuthorizationException
    {
        List<Voicemail> voicemails = new ArrayList<Voicemail>();

        Uri next = null;
        do
        {
            if(Thread.currentThread().isInterrupted())
            {
                Log.i(TAG, "interrupted retrieval of voicemails");
                return null;
            }

            next = getPagedVoicemails(aInfo, false, next, voicemails, syncTime);
            if(next == null)
            {
                Log.e(TAG, "error getting voicemails");
                return null;
            }
            Log.i(TAG, "got first page: " + next);
        }
        while(next != Uri.EMPTY);
        
        return voicemails;
    }

    private static boolean putUpdate(String deviceId, AccountInfo aInfo, long voicemailID, JSONObject json)
        throws IOException, AuthorizationException
    {
        Uri apiUri = null;
        try
        {
            Uri.Builder builder = aInfo.getHost().buildUpon();
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
            HttpResponse response = sendJsonPut(apiUri, aInfo, json, deviceId);
            return checkStatus("voicemail update", response, true);
        }
        catch(UnsupportedEncodingException e)
        {
            Log.e(TAG, "unsupported encoding exception", e);
            return false;
        }
    }

    public static boolean markVoicemailRead(String deviceId, AccountInfo aInfo, long voicemailID, boolean isRead)
        throws IOException, AuthorizationException
    {
        try
        {
            JSONObject json = new JSONObject();
            json.put("isNew", !isRead);
            
            return putUpdate(deviceId, aInfo, voicemailID, json);
        }
        catch(JSONException e)
        {
            Log.e(TAG, "has notified json exception", e);
            return false;
        }
    }

    public static boolean deleteVoicemail(String deviceId, AccountInfo aInfo, long voicemailID)
        throws IOException, AuthorizationException
    {
        Uri apiUri = null;
        try
        {
            Uri.Builder builder = aInfo.getHost().buildUpon();
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
        addDeviceIDHeader(httpDelete, deviceId);
        addAuthorizationHeaders(httpDelete, aInfo.getName(), aInfo.getAuthToken());

        Log.d(TAG, "Sending DELETE request to " + httpDelete.getURI().toString());
        
        HttpResponse response = mHttpClient.execute(httpDelete);
        return checkStatus("deleting voicemail", response, true);
    }

    public static HttpEntity getVoicemailInput(Uri host, String accountName, String authToken, long voicemailID)
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

        Log.i(TAG, "getting audio file " + apiUri + " " + accountName);
        HttpGet httpGet = new HttpGet(apiUri.toString());
        addAuthorizationHeaders(httpGet, accountName, authToken);

        httpGet.setHeader("Accept", "audio/mpeg");

        HttpResponse response = mHttpClient.execute(httpGet);

        return checkStatus("getting audio", response, false) ? response.getEntity() : null;
    }

    public static ServerRegistrationInfo getServerRegistrationInfo(AccountInfo aInfo, String deviceId)
        throws AuthorizationException
    {
        Uri apiUri = null;
        try
        {
            Uri.Builder builder = aInfo.getHost().buildUpon();
            builder.appendEncodedPath(REGISTER_PATH);
            builder.appendQueryParameter("deviceType", DEVICE_TYPE);
            builder.appendQueryParameter("deviceId", deviceId);
            apiUri = builder.build();
        }
        catch(Exception e)
        {
            Log.i(TAG, "error creating URI", e);
            return null;
        }

        HttpGet httpGet = new HttpGet(apiUri.toString());
        httpGet.addHeader("Accept", "application/json");
        addAuthorizationHeaders(httpGet, aInfo.getName(), aInfo.getAuthToken());

        Log.i(TAG, "Retrieving server registration info " + httpGet.getURI().toString());
        maybeCreateHttpClient();

        try
        {
            ControllerResponse response = mHttpClient.execute(httpGet, new JsonObjectResponseHandler());
            response.throwExceptions();
            
            ServerRegistrationInfo info = new ServerRegistrationInfo(response.jsonObject);

            Log.d(TAG, "Retrieved " + info);
            
            return info;
        }
        catch(JSONException e)
        {
            Log.e(TAG, "JSONException overall", e);
            return null;
        }
        catch(IOException e)
        {
            Log.e(TAG, "unable to get registration info", e);
            return null;
        }
    }

    private static void addAuthoritiesToJSON(JSONObject json, String key, Authority[] authorities)
        throws JSONException
    {
        JSONArray jArray = new JSONArray();
        if(authorities != null)
        {
            for(Authority auth : authorities)
            {
                jArray.put(auth.name());
            }
        }
        json.put(key, jArray);
    }
    
    /**
     * Register/Unregister a device with the Listen server.
     * 
     * @param aInfo
     * @param registrationId null to not change, "" to unregister, otherwise registration ID to register
     * @param clientDeviceId
     * @param enable authorities to enable or null
     * @param disable authorities to disable or null
     * @return
     * @throws IOException
     * @throws AuthorizationException
     */
    public static boolean registerDevice(String clientDeviceId, AccountInfo aInfo, String registrationId,
                                         Authority[] enable, Authority[] disable)
        throws IOException, AuthorizationException
    {
        Uri apiUri = null;
        try
        {
            Uri.Builder builder = aInfo.getHost().buildUpon();
            builder.appendEncodedPath(REGISTER_PATH);
            apiUri = builder.build();
        }
        catch(Exception e)
        {
            Log.i(TAG, "error creating URI", e);
            return false;
        }

        JSONObject json = new JSONObject();
        try
        {
            json.put("deviceId", clientDeviceId);
            json.put("deviceType", DEVICE_TYPE);

            if(registrationId != null)
            {
                json.put("registrationToken", registrationId);
            }

            addAuthoritiesToJSON(json, "registerTypes", enable);
            addAuthoritiesToJSON(json, "unregisterTypes", disable);
        }
        catch(JSONException e)
        {
            Log.e(TAG, "error creating registration object", e);
            return false;
        }
   
        HttpResponse response = sendJsonPut(apiUri, aInfo, json, null);
        return checkStatus("device registration", response, true);
    }
    
    public static boolean getEmailContacts(AccountInfo aInfo, ListenContacts contacts) throws AuthorizationException, IOException
    {
        return getAllContacts(aInfo, contacts, ContactMIME.EMAIL, EMAILS_PATH);
    }

    public static boolean getNumberContacts(AccountInfo aInfo, ListenContacts contacts) throws AuthorizationException, IOException
    {
        return getAllContacts(aInfo, contacts, ContactMIME.PHONE, NUMBERS_PATH);
    }
    
    private static boolean getAllContacts(AccountInfo aInfo, ListenContacts contacts, ContactMIME mime, String path)
        throws AuthorizationException, IOException
    {
        Uri next = null;
        do
        {
            if(Thread.currentThread().isInterrupted())
            {
                Log.i(TAG, "interrupted retrieval of " + mime.name());
                return false;
            }
    
            next = getPagedResults(aInfo, next, contacts, mime, path);
            if(next == null)
            {
                Log.e(TAG, "error getting " + mime.name());
                return false;
            }
            Log.i(TAG, "got first page: " + next);
        }
        while(next != Uri.EMPTY);
        
        return true;
    }

    private static Uri getPagedResults(AccountInfo aInfo, Uri nextRef, ListenContacts contacts,
                                       ContactMIME mime, String path)
        throws IOException, AuthorizationException
    {
        Uri apiUri = null;
        if(nextRef == null)
        {
            try
            {
                Uri.Builder builder = aInfo.getHost().buildUpon();
                builder.appendEncodedPath(path);
                builder.appendQueryParameter("_first", Integer.toString(0));
                builder.appendQueryParameter("_max", Integer.toString(MAX_PAGED_GET));
                
                apiUri = builder.build();
            }
            catch(Exception e)
            {
                Log.i(TAG, "error creating URI", e);
                return null;
            }
        }
        else
        {
            apiUri = nextRef;
        }
        
        HttpGet httpGet = new HttpGet(apiUri.toString());
        httpGet.addHeader("Accept", "application/json");
        AndroidHttpClient.modifyRequestToAcceptGzipResponse(httpGet);
        addAuthorizationHeaders(httpGet, aInfo.getName(), aInfo.getAuthToken());

        Log.i(TAG, "Retrieving contacts: " + httpGet.getURI().toString());
        maybeCreateHttpClient();

        String next = null;
        
        try
        {
            ControllerResponse response = mHttpClient.execute(httpGet, new JsonObjectResponseHandler());
            response.throwExceptions();
            
            JSONObject json = response.jsonObject;
            if(json == null)
            {
                throw new JSONException("JSON object not set");
            }
            
            next = json.has("next") ? json.getString("next") : null;
            int total = json.getInt("total");
            int count = json.getInt("count");
            JSONArray results = (JSONArray)json.get("results");
            
            Log.i(TAG, "Results array total: " + total + " array: " + results.length() + " next: " + next + " count: " + count);

            int n = 0;
            for(int i = 0; i < count; i++)
            {
                JSONObject result = results.getJSONObject(i);
                if(contacts.add(result, mime))
                {
                    ++n;
                }
            }
            Log.d(TAG, "inserted " + n + " addresses out of " + count);
        }
        catch(JSONException e)
        {
            Log.e(TAG, "JSONException overall", e);
            return null;
        }
        
        return getNextUri(aInfo, next, NEXT_CONTACT_PREPEND_PATH);
    }

    private static Uri getNextUri(AccountInfo aInfo, String next, String prePath)
    {
        Uri nextUri = null;
        
        if(TextUtils.isEmpty(next))
        {
            nextUri = Uri.EMPTY;
        }
        else
        {
            try
            {
                Uri.Builder builder = aInfo.getHost().buildUpon();
                builder.appendEncodedPath(prePath);
                if(next.charAt(0) == '/')
                {
                    builder.appendEncodedPath(next.substring(1));
                }
                else
                {
                    builder.appendEncodedPath(next);
                }
                nextUri = builder.build();
            }
            catch(Exception e)
            {
                Log.i(TAG, "error creating next URI", e);
                return null;
            }

        }
        
        return nextUri;
    }

    private static HttpResponse sendJsonPut(Uri apiUri, AccountInfo aInfo, JSONObject json, String deviceId) throws IOException
    {
        maybeCreateHttpClient();

        HttpEntity entity = new StringEntity(json.toString(), "UTF-8");
        
        HttpPut httpPut = new HttpPut(apiUri.toString());
        
        httpPut.setEntity(entity);
        
        httpPut.setHeader("Accept", "application/json");
        httpPut.setHeader("Content-Type", "application/json");
        
        if(deviceId != null)
        {
            addDeviceIDHeader(httpPut, deviceId);
        }

        addAuthorizationHeaders(httpPut, aInfo.getName(), aInfo.getAuthToken());
   
        Log.d(TAG, "Sending PUT request " + httpPut.getURI().toString());

        return mHttpClient.execute(httpPut);
    }
    
    private static boolean checkStatus(String description, HttpResponse response, boolean consume)
        throws AuthorizationException
    {
        int status = response.getStatusLine().getStatusCode();
        if(status < 200 || status > 299)
        {
            if(status == 401)
            {
                throw new AuthorizationException();
            }
            Log.e(TAG, description + ": " + status);
            return false;
        }
        Log.i(TAG, description + ": " + status);
        
        if(consume)
        {
            HttpEntity entity = response.getEntity();
            if(entity != null)
            {
                try
                {
                    entity.consumeContent();
                }
                catch(IOException e)
                {
                    Log.e(TAG, "error consuming content for " + description, e);
                }
            }
        }
        return true;
    }
    
    private static void maybeCreateHttpClient()
    {
        if(mHttpClient == null)
        {
            mHttpClient = AndroidHttpClient.newInstance("listen/1.0");
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

            String entityString = entityToString(entity);
            return new ControllerResponse(status, entityString);
        }
    }
    
    private static String entityToString(HttpEntity entity) throws IOException
    {
        InputStream in = AndroidHttpClient.getUngzippedContent(entity);
        if(in == null)
        {
            return "";
        }
        long contentLength = entity.getContentLength();
        if(contentLength > Integer.MAX_VALUE)
        {
            throw new IllegalArgumentException("HTTP data to large to buffer");
        }
        
        int initCapacity = contentLength < 0 ? 4096 : (int)contentLength;
        if(in instanceof GZIPInputStream)
        {
            Log.v(TAG, "GZIP content initial size: " + contentLength);
            if(contentLength > 0 && initCapacity < (Integer.MAX_VALUE / 2))
            {
                // figure about 50% gzip compression ratio
                initCapacity *= 2;
            }
        }
        
        String charSet = EntityUtils.getContentCharSet(entity);
        if(charSet == null)
        {
            charSet = HTTP.DEFAULT_CONTENT_CHARSET;
        }
        
        Reader reader = new InputStreamReader(in, charSet);
        StringBuilder sb = new StringBuilder(initCapacity);
        try
        {
            char[] tmp = new char[1024];
            int read;
            while((read = reader.read(tmp)) != -1)
            {
                sb.append(tmp, 0, read);
            }
        }
        finally
        {
            reader.close();
        }

        if(sb.length() != contentLength)
        {
            Log.i(TAG, "content length of " + contentLength + " bytes: " + sb.length() + " characters");
        }
        
        return sb.toString();
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
