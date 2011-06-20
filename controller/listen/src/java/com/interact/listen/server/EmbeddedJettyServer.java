package com.interact.listen.server;

import java.io.File;
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
        webapp.setTempDirectory(new File("/interact/listen/.webapp-" + new SimpleDateFormat("yyyyMMddHHmmss").format(new Date())));

        server.setHandler(webapp);
        server.start();
        server.join();
    }
}
