package com.interact.listen.config;

import java.io.*;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLEncoder;
import java.util.Date;

import javax.servlet.http.HttpServletResponse;

import org.apache.commons.io.IOUtils;
import org.apache.log4j.Logger;

public class GoogleAuth
{
    private static final Logger LOG = Logger.getLogger(GoogleAuth.class);

    private static final String GOOGLE_LOGIN = "https://www.google.com/accounts/ClientLogin";

    private static final String GOOGLE_DATA = "accountType=HOSTED_OR_GOOGLE&service=ac2dm&source=interact-listen-voicemail";
    
    private static final String PARAM_EMAIL = "Email";
    private static final String PARAM_PASSWORD = "Passwd";
    
    private static final String UTF8 = "UTF-8";
    
    public static enum Error
    {
        BadAuthentication  ("The login request used a username or password that is not recognized."),
        NotVerified        ("The account email address has not been verified."),
        TermsNotAgreed     ("The user has not agreed to terms."),
        CaptchaRequired    ("A CAPTCHA is required."),
        Unknown            ("Unknown error."),
        AccountDeleted     ("The user account has been deleted."),
        AccountDisabled    ("The user account has been disabled."),
        ServiceDisabled    ("The user's access to the specified service has been disabled."),
        ServiceUnavailable ("The service is not available at the moment."),
        ;
        
        private String description;
        
        private Error(String description)
        {
            this.description = description;
        }
        
        public String getDescription()
        {
            return description;
        }
        
        public boolean isRetryableError()
        {
            return this == ServiceUnavailable;
        }
    }
    
    private static final GoogleAuth ga = new GoogleAuth();
    
    public static GoogleAuth getInstance()
    {
        return ga;
    }
    
    private String currentToken = null;

    private Error lastError = null;
    private Date nextRetry = null;
    private long retryTimeout = 1000;

    public synchronized void invalidateCachedToken(String token)
    {
        if(token == null || token.equals(currentToken))
        {
            invalidateCachedToken();
        }
    }
    
    public synchronized void updateToken(String token)
    {
        if(token != null)
        {
            currentToken = token;
            Configuration.set(Property.Key.GOOGLE_AUTH_TOKEN, currentToken);
        }
    }

    public synchronized void setByPassword(String user, String password)
    {
        invalidateCachedToken();
        
        try
        {
            currentToken = queryAuthToken(user, password);
        }
        catch(IOException e)
        {
            LOG.error("exception querying for auth token", e);
        }
        if(currentToken == null)
        {
            currentToken = "";
        }
        Configuration.set(Property.Key.GOOGLE_AUTH_USER, user);
        Configuration.set(Property.Key.GOOGLE_AUTH_TOKEN, currentToken);
    }
    
    public synchronized void setByToken(String user, String authToken)
    {
        invalidateCachedToken();
        currentToken = authToken == null ? "" : authToken;
        Configuration.set(Property.Key.GOOGLE_AUTH_TOKEN, currentToken);
        Configuration.set(Property.Key.GOOGLE_AUTH_USER, user);
    }
    
    public String getUsername()
    {
        String user = Configuration.get(Property.Key.GOOGLE_AUTH_USER);
        return user == null ? "" : user;
    }
    
    public synchronized String getToken()
    {
        if(currentToken == null || currentToken.length() == 0)
        {
            currentToken = Configuration.get(Property.Key.GOOGLE_AUTH_TOKEN);
        }
        return currentToken == null ? "" : currentToken;
    }
    
    public synchronized Error getLastError()
    {
        return lastError == null ? Error.Unknown : lastError;
    }
    
    private void invalidateCachedToken()
    {
        currentToken = null;
        lastError = null;
        nextRetry = null;
        retryTimeout = 1000;
    }
    
    private String queryAuthToken(final String user, final String pass) throws IOException
    {
        if(user == null || pass == null || user.length() == 0 || pass.length() == 0)
        {
            LOG.error("C2DM user and password not set [user: " + user + ", password length: " + (pass == null ? 0 : pass.length()) + "]");
            return null;
        }
        if(!isOKToTry())
        {
            LOG.debug("C2DM perminant failure already using the same information");
            return null;
        }
        if(!isTimeToRetry())
        {
            LOG.debug("C2DM haven't gone long enough since last try");
            return null;
        }
        
        StringBuilder postDataBuilder = new StringBuilder();
        postDataBuilder.append(GOOGLE_DATA);
        postDataBuilder.append('&').append(PARAM_EMAIL).append('=').append(URLEncoder.encode(user, UTF8));
        postDataBuilder.append('&').append(PARAM_PASSWORD).append('=').append(URLEncoder.encode(pass, UTF8));
        
        byte[] postData = postDataBuilder.toString().getBytes(UTF8);

        URL url = new URL(GOOGLE_LOGIN);

        HttpURLConnection conn = (HttpURLConnection)url.openConnection();
        conn.setDoOutput(true);
        conn.setUseCaches(false);
        conn.setRequestMethod("POST");
        conn.setRequestProperty("Content-Type", "application/x-www-form-urlencoded");
        conn.setRequestProperty("Content-Length", Integer.toString(postData.length));

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

        String error = null;
        String token = null;
        
        BufferedReader in = null;
        try
        {
            if(responseCode == HttpServletResponse.SC_FORBIDDEN)
            {
                in = new BufferedReader(new InputStreamReader(conn.getErrorStream()));
                error = readProperty(in, "Error");
                if(error == null)
                {
                    LOG.error("received forbidden but no error");
                    error = Error.Unknown.name();
                }
                else
                {
                    LOG.error("received forbidden when requesting auth token: " + error);
                }
            }
            else if(responseCode != HttpServletResponse.SC_OK)
            {
                LOG.error("received error response: " + responseCode);
                error = Error.ServiceUnavailable.name();
            }
            else
            {
                in = new BufferedReader(new InputStreamReader(conn.getInputStream()));
                token = readProperty(in, "Auth");
                if(token == null)
                {
                    LOG.error("succesful response but auth token not set");
                    error = Error.Unknown.name();
                }
                else
                {
                    LOG.info("logged in succesfully with auth token [" + token + "]");
                }
            }
        }
        finally
        {
            if(in != null)
            {
                IOUtils.closeQuietly(in);
            }
        }
        
        setResult(error);

        return token;
    }

    private String readProperty(BufferedReader in, String prop) throws IOException
    {
        final String search = prop + '=';
        
        String value = null;
        String resLine = null;
        while ((resLine = in.readLine()) != null)
        {
            if(resLine.startsWith(search))
            {
                value = resLine.substring(search.length());
                break;
            }
        }
        return value;
    }
    
    private void setResult(String error)
    {
        if(error == null)
        {
            lastError = null;
        }
        else
        {
            try
            {
                lastError = Error.valueOf(error);
            }
            catch(IllegalArgumentException e)
            {
                LOG.warn("unknown error return from google: " + error);
                lastError = Error.Unknown;
            }
            
            if(lastError.isRetryableError())
            {
                nextRetry = new Date(System.currentTimeMillis() + retryTimeout);
                if(retryTimeout >= 43200000) // half a day or more
                {
                    retryTimeout = 86400000; // max out at a day
                }
                else
                {
                    retryTimeout *= 2;
                }
            }
        }
    }

    private boolean isOKToTry()
    {
        return lastError == null || lastError.isRetryableError();
    }

    private boolean isTimeToRetry()
    {
        return lastError == null || nextRetry == null || nextRetry.before(new Date());
    }
    
    private GoogleAuth()
    {
    }

}
