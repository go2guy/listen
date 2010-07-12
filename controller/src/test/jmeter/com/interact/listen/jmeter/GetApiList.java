package com.interact.listen.jmeter;

import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.List;

import org.apache.commons.io.IOUtils;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;

public class GetApiList
{
    public static void main(String[] args) throws MalformedURLException, IOException, ParseException
    {
        if(args.length != 3)
        {
            throw new IllegalArgumentException("Usage: java GetApiList <base-url> <resource-url> <fields>");
        }

        String base = args[0];
        String part = args[1];
        String[] fields = args[2].split(",");

        boolean isSubscriberQuery = part.contains("subscribers");
        
        do
        {
            JSONObject json = getJson(new URL(base + part));
            part = (String)json.get("next");

            List<JSONObject> results = (List<JSONObject>)json.get("results");
            for(JSONObject result : results)
            {
                if(isSubscriberQuery && result.get("username") != null && result.get("username").equals("Admin"))
                {
                    continue;
                }
                String output = "";
                for(String field : fields)
                {
                    output += result.get(field) + ",";
                }
                output = output.substring(0, output.length() - 1);
                System.out.println(output);
            }
        }
        while(part != null);
    }

    private static JSONObject getJson(URL url) throws ParseException, IOException
    {
        JSONParser parser = new JSONParser();
        return (JSONObject)parser.parse(getUrl(url));
    }

    private static String getUrl(URL url) throws IOException
    {
        HttpURLConnection conn = (HttpURLConnection)url.openConnection();
        conn.setRequestMethod("GET");
        conn.setRequestProperty("Accept", "application/json");
        conn.connect();

        int status = conn.getResponseCode();
        String content = IOUtils.toString(conn.getInputStream());
        if(status < 200 || status > 299)
        {
            throw new IOException("Error status [" + status + "] returned: [" + content + "]");
        }

        return content;
    }
}
