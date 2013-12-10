package com.interact.listen.utils;

import org.apache.commons.configuration.ConfigurationException;
import org.apache.commons.configuration.PropertiesConfiguration;

/**
 * Created with IntelliJ IDEA.
 * User: knovak
 * Date: 12/10/13
 * Time: 9:35 AM
 * To change this template use File | Settings | File Templates.
 */
public class ListenControllerConfig
{
    private static PropertiesConfiguration pc;
    private static final String propsFile = "listen-controller.properties";

    static
    {
        try
        {
            pc = new PropertiesConfiguration();
            pc.load(propsFile);
        }
        catch (ConfigurationException e)
        {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
    }

    private ListenControllerConfig()
    {
    }

    public static String get(String prop)
    {
        String returnVal = null;

        returnVal = pc.getString(prop);

        return returnVal;
    }

}
