//
// $Id: MiCasaApp.java,v 1.12 2004/02/22 18:55:26 ray Exp $

package com.threerings.micasa.client;

import java.io.IOException;
import com.samskivert.swing.util.SwingUtil;

import com.threerings.presents.client.Client;
import com.threerings.presents.net.Credentials;
import com.threerings.presents.net.UsernamePasswordCreds;

import com.threerings.micasa.Log;

/**
 * The micasa app is the main point of entry for the MiCasa client
 * application. It creates and initializes the myriad components of the
 * client and sets all the proper wheels in motion.
 */
public class MiCasaApp
{
    public void init ()
        throws IOException
    {
        // create a frame
        _frame = new MiCasaFrame();

        // create our client instance
        String cclass = getProperty("client");
        if (cclass == null) {
            cclass = MiCasaClient.class.getName();
        }

        try {
            _client = (MiCasaClient)Class.forName(cclass).newInstance();
        } catch (Exception e) {
            Log.warning("Unable to instantiate client class " +
                        "[cclass=" + cclass + "].");
            Log.logStackTrace(e);
        }

        // initialize our client instance
        _client.init(_frame);
    }

    public void run ()
    {
        // position everything and show the frame
        _frame.setSize(800, 600);
        SwingUtil.centerWindow(_frame);
        _frame.setVisible(true);

        Client client = _client.getContext().getClient();

        // read our server and port settings
        String server = getProperty("server");
        if (server == null) {
            server = "localhost";
        }
        int port = Client.DEFAULT_SERVER_PORT;
        String portstr = getProperty("port");
        if (portstr != null) {
            try {
                port = Integer.parseInt(portstr);
            } catch (NumberFormatException nfe) {
                Log.warning("Invalid port specification '" + portstr + "'.");
            }
        }

        // pass them on to the client
        client.setServer(server, port);

        // configure the client with some credentials and logon
        String username = getProperty("username");
        String password = getProperty("password");
        if (username != null && password != null) {
            // create and set our credentials
            Credentials creds = new UsernamePasswordCreds(username, password);
            client.setCredentials(creds);
            client.logon();
        }
    }

    /**
     * Attempts to get a property but doesn't freak out if we fail
     * security check.
     */
    protected String getProperty (String key)
    {
        try {
            return System.getProperty(key);
        } catch (Throwable t) {
            Log.info("Can't fetch '" + key + "' system property.");
            return null;
        }
    }

    public static void main (String[] args)
    {
        MiCasaApp app = new MiCasaApp();
        try {
            // initialize the app
            app.init();
        } catch (IOException ioe) {
            Log.warning("Error initializing application.");
            Log.logStackTrace(ioe);
        }
        // and run it
        app.run();
    }

    protected MiCasaClient _client;
    protected MiCasaFrame _frame;
}
