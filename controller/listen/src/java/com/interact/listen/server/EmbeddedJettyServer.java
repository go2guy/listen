package com.interact.listen.server;

import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.security.ProtectionDomain;
import java.text.SimpleDateFormat;
import java.util.Date;

import org.mortbay.jetty.Connector;
import org.mortbay.jetty.Server;
import org.mortbay.jetty.nio.SelectChannelConnector;
import org.mortbay.jetty.security.SslSocketConnector;
import org.mortbay.jetty.webapp.WebAppContext;

/**
 * Provides a {@link #main(String[])} method for running this application within an embedded Jetty server.
 */
public final class EmbeddedJettyServer
{
    private static final String EXTRACT_DIR = "/interact/listen/.jetty-webapp";

    private EmbeddedJettyServer()
    {
        throw new AssertionError("Cannot instantiate main() class EmbeddedJettyServer");
    }

    public static void main(String[] args) throws Exception
    {
        // some server-specific properties
        boolean ssl = Boolean.valueOf(System.getProperty("ssl"));
        int port = Integer.parseInt(System.getProperty("port", ssl ? "8443" : "8080"));
        String ext = System.getProperty("ext", "");

        Connector connector = new SelectChannelConnector();
        if(ssl)
        {
            connector = new SslSocketConnector();
            ((SslSocketConnector)connector).setKeyPassword(System.getProperty("keyPassword"));
        }

        connector.setPort(port);

        Server server = new Server();
        server.addConnector(connector);

        ProtectionDomain domain = EmbeddedJettyServer.class.getProtectionDomain();
        URL location = domain.getCodeSource().getLocation();

        WebAppContext webapp = new WebAppContext();
        webapp.setContextPath("/");
        webapp.setDescriptor(location.toExternalForm() + "/WEB-INF/web.xml");
        if(!ext.trim().equals(""))
        {
            String replaced = ext.replaceAll(":", ","); // setExtraClasspath requires comma or semicolon delimiters
            System.out.println("Using ext classpath [" + replaced + "]");
            webapp.setExtraClasspath(replaced);
        }
        webapp.setServer(server);
        webapp.setWar(location.toExternalForm());

        File dir = createWebappExtractDirectory(EXTRACT_DIR);
        webapp.setTempDirectory(dir);

        server.setHandler(webapp);
        server.start();
        server.join();
    }

    private static File createWebappExtractDirectory(String path)
    {
        File dir = new File(path);
        if(dir.exists() && !deleteRecursively(dir))
        {
            System.out.println("Unable to delete pre-existing extraction directory [" + path + "]");
            System.exit(1);
        }
        else if(!dir.exists() && !dir.mkdirs())
        {
            System.out.println("Unable to create extraction directory [" + path +"]");
            System.exit(1);
        }
        return dir;
    }

    private static boolean deleteRecursively(File dir)
    {
        boolean result = true;
        if(dir.isDirectory())
        {
            for(File file : dir.listFiles())
            {
                result &= deleteRecursively(file);
            }
        }
        return result && dir.delete();
    }
}
