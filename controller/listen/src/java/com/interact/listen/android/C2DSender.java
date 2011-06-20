package com.interact.listen.android;

import com.interact.listen.android.GoogleAuthService;

import java.io.*;
import java.net.URL;

import javax.net.ssl.HostnameVerifier;
import javax.net.ssl.HttpsURLConnection;
import javax.net.ssl.SSLSession;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.io.IOUtils;
import org.apache.log4j.Logger;

public final class C2DSender
{
    private static final Logger LOG = Logger.getLogger(C2DSender.class);

    private static final String C2DM_HOST = "android.apis.google.com";
    private static final String C2DM_SEND_ENDPOINT = "https://" + C2DM_HOST + "/c2dm/send";
    private static final String UPDATE_CLIENT_AUTH = "Update-Client-Auth";
    
    private static final HostnameVerifier HOST_NAME_VERIFIER = new HostnameVerifier()
    {
        @Override
        public boolean verify(String hostname, SSLSession session)
        {
            if(!C2DM_HOST.equals(hostname))
            {
                LOG.error("Host '" + hostname + "' does not match expected '" + C2DM_HOST + "'");
                return false;
            }
            return true;
        }
    };

    private GoogleAuthService googleAuthService;
    
    public void setGoogleAuthService(GoogleAuthService googleAuthService) {
        this.googleAuthService = googleAuthService;
    }

    public C2DError send(C2DMessage message, String token) throws IOException
    {
        String authToken = token == null ? googleAuthService.getToken() : token;
        if(authToken == null)
        {
            LOG.error("Auth token not set");
            throw new IOException("authorization token not set");
        }
        
        byte[] postData = message.createPostData();

        URL url = new URL(C2DM_SEND_ENDPOINT);

        HttpsURLConnection conn = (HttpsURLConnection)url.openConnection();
        conn.setHostnameVerifier(HOST_NAME_VERIFIER);
        conn.setDoOutput(true);
        conn.setUseCaches(false);
        conn.setRequestMethod("POST");
        conn.setRequestProperty("Content-Type", "application/x-www-form-urlencoded");
        conn.setRequestProperty("Content-Length", Integer.toString(postData.length));
        conn.setRequestProperty("Authorization", "GoogleLogin auth=" + authToken);

        OutputStream out = conn.getOutputStream();
        try
        {
            out.write(postData);
        }
        finally
        {
            IOUtils.closeQuietly(out);
        }
        
        int responseCode = conn.getResponseCode();

        if(responseCode == HttpServletResponse.SC_UNAUTHORIZED || responseCode == HttpServletResponse.SC_FORBIDDEN)
        {
            LOG.warn("unauthorized / forbidden - need new token");
            googleAuthService.invalidateCachedToken(authToken);
            return C2DError.InvalidAuthToken;
        }

        String updatedAuthToken = conn.getHeaderField(UPDATE_CLIENT_AUTH);
        if(updatedAuthToken != null && !authToken.equals(updatedAuthToken))
        {
            LOG.info("Got updated auth token from C2DM servers: " + updatedAuthToken);
            googleAuthService.updateToken(updatedAuthToken);
        }
        
        if(responseCode == HttpServletResponse.SC_SERVICE_UNAVAILABLE)
        {
            LOG.info("server is unavailable");
            // Retry-After SHOULD be used
            return C2DError.ServiceUnavailable;
        }

        BufferedReader in = null;
        String responseLine = null;
        try
        {
            in = new BufferedReader(new InputStreamReader(conn.getInputStream()));
            responseLine = in.readLine();
        }
        finally
        {
            if(in != null)
            {
                IOUtils.closeQuietly(in);
            }
        }

        LOG.info("Got " + responseCode + " response from Google C2DM endpoint.");

        if(responseLine == null || responseLine.length() == 0)
        {
            throw new IOException("Got empty response from Google C2DM endpoint.");
        }

        String[] responseParts = responseLine.split("=", 2);
        if(responseParts.length == 2)
        {
            if(responseParts[0].equals("id"))
            {
                LOG.info("Successfully sent data message to device: " + responseLine);
                return null;
            }
    
            if(responseParts[0].equals("Error"))
            {
                String err = responseParts[1];
                LOG.warn("Got error response from Google C2DM endpoint: " + err);
                return C2DError.getError(err);
            }
        }
        LOG.warn("Invalid response from google " + responseLine + " " + responseCode);
        throw new IOException("Invalid response from Google " + responseCode + " " + responseLine);
    }
}
