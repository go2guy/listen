package com.interact.listen.android

class GoogleAuthService {
    private static final String GOOGLE_LOGIN = "https://www.google.com/accounts/ClientLogin"
    private static final String GOOGLE_DATA = "accountType=HOSTED_OR_GOOGLE&service=ac2dm&source=interact-listen-voicemail"
    private static final String PARAM_EMAIL = "Email"
    private static final String PARAM_PASSWORD = "Passwd"
    private static final String UTF8 = "UTF-8"
    
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
        ServiceUnavailable ("The service is not available at the moment.")

        private String description

        Error(String description) {
            this.description = description
        }

        public boolean isRetryableError()
        {
            return this == ServiceUnavailable
        }
    }
    
    synchronized void invalidateCachedToken(String token)
    {
        if(token == null)
        {
            invalidateCachedToken();
        }
    }
    
    synchronized void updateToken(String token)
    {
        if(token != null)
        {
            def config = config()
            config.authToken = token
            config.save(flush: true)
        }
    }

    public synchronized void setEnabled(boolean enabled) {
        def config = config()
        config.isEnabled = enabled
        config.save(flush: true)
    }

    public synchronized boolean isEnabled() {
        def config = config()
        return config.isEnabled
    }

    synchronized void setByPassword(String user, String password)
    {
        invalidateCachedToken();
        
        try
        {
            def config = config()
            config.authUser = user
            config.authToken = queryAuthToken(user, password) ?: ''
            config.save(flush: true)
        }
        catch(IOException e)
        {
            log.error("exception querying for auth token", e);
        }
    }
    
    public synchronized void setByToken(String user, String authToken)
    {
        invalidateCachedToken();
        
        def config = config()
        config.authUser = user
        config.authToken = authToken ?: ''
        config.save(flush: true)
    }
    
    public String getUsername()
    {
        return config().authUser
    }
    
    public synchronized String getToken()
    {
        return config().authToken
    }
    
    public synchronized Error getLastError()
    {
        return lastError ?: Error.Unknown
    }

    private void invalidateCachedToken()
    {
        def config = config()
        config.authToken = ''
        config.lastError = ''
        config.nextRetry = null
        config.retryTimeout = 1000
        config.save(flush: true)
    }
    
    private String queryAuthToken(final String user, final String pass) throws IOException
    {
        if(user == null || pass == null || user.length() == 0 || pass.length() == 0)
        {
            log.error("C2DM user and password not set [user: " + user + ", password length: " + (pass == null ? 0 : pass.length()) + "]");
            return null;
        }
        if(!isOKToTry())
        {
            log.debug("C2DM permanent failure already using the same information");
            return null;
        }
        if(!isTimeToRetry())
        {
            log.debug("C2DM haven't gone long enough since last try");
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
        
        BufferedReader reader = null;
        try
        {
            if(responseCode == HttpServletResponse.SC_FORBIDDEN)
            {
                reader = new BufferedReader(new InputStreamReader(conn.getErrorStream()));
                error = readProperty(reader, "Error");
                if(error == null)
                {
                    log.error("received forbidden but no error");
                    error = Error.Unknown.name();
                }
                else
                {
                    log.error("received forbidden when requesting auth token: " + error);
                }
            }
            else if(responseCode != HttpServletResponse.SC_OK)
            {
                log.error("received error response: " + responseCode);
                error = Error.ServiceUnavailable.name();
            }
            else
            {
                reader = new BufferedReader(new InputStreamReader(conn.getInputStream()));
                token = readProperty(reader, "Auth");
                if(token == null)
                {
                    log.error("succesful response but auth token not set");
                    error = Error.Unknown.name();
                }
                else
                {
                    log.info("logged in succesfully with auth token [" + token + "]");
                }
            }
        }
        finally
        {
            IOUtils.closeQuietly(reader);
        }
        
        setResult(error);

        return token;
    }

    private String readProperty(BufferedReader reader, String prop) throws IOException
    {
        final String search = prop + '=';
        
        String value = null;
        String resLine = null;
        while ((resLine = reader.readLine()) != null)
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
                log.warn("unknown error return from google: " + error);
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

    private GoogleAuthConfiguration config() {
        def configs = GoogleAuthConfiguration.list()
        return configs.size() > 0 ? configs[0] : new GoogleAuthConfiguration()
    }
}
