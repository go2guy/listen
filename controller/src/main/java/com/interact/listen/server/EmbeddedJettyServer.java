package com.interact.listen.server;

import java.net.URL;
import java.security.ProtectionDomain;

import org.mortbay.jetty.Server;
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
        int port = Integer.parseInt(System.getProperty("port", "8080"));
        Server server = new Server(port);

        ProtectionDomain domain = EmbeddedJettyServer.class.getProtectionDomain();
        URL location = domain.getCodeSource().getLocation();

        WebAppContext webapp = new WebAppContext();
        webapp.setContextPath("/");
        webapp.setDescriptor(location.toExternalForm() + "/WEB-INF/web.xml");
        webapp.setServer(server);
        webapp.setWar(location.toExternalForm());

        server.setHandler(webapp);
        server.start();
        server.join();
    }
}
