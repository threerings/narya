//
// $Id: MiCasaApp.java,v 1.5 2002/01/19 04:02:11 mdb Exp $

package com.threerings.micasa.client;

import java.io.IOException;
import com.samskivert.swing.util.SwingUtil;

import com.threerings.presents.client.Client;
import com.threerings.presents.client.ClientAdapter;
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
        String cclass = System.getProperty("client");
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
        _frame.show();

        Client client = _client.getContext().getClient();

        String server = System.getProperty("server");
        if (server != null) {
            // indicate which server to which we should connect
            client.setServer(server, Client.DEFAULT_SERVER_PORT);
        }

        // we want to exit when we logged off or failed to log on
        client.addObserver(new ClientAdapter() {
            public void clientFailedToLogon (Client c, Exception cause) {
                System.exit(0);
            }
            public void clientDidLogoff (Client c) {
                System.exit(0);
            }
        });

        // configure the client with some credentials and logon
        String username = System.getProperty("username");
        if (username == null) {
            username =
                "bob" + ((int)(Math.random() * Integer.MAX_VALUE) % 500);
        }

        // create and set our credentials
        Credentials creds = new UsernamePasswordCreds(username, "test");
        client.setCredentials(creds);
        client.logon();
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
