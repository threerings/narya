//
// $Id: MiCasaApp.java,v 1.2 2001/10/25 23:03:29 mdb Exp $

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
        _client = new MiCasaClient(_frame);
    }

    public void run ()
    {
        // position everything and show the frame
        _frame.setSize(800, 600);
        SwingUtil.centerWindow(_frame);
        _frame.show();

        // configure the client with some credentials and logon
        String username = System.getProperty("username");
        if (username == null) {
            username =
                "bob" + ((int)(Math.random() * Integer.MAX_VALUE) % 500);
        }
        Credentials creds = new UsernamePasswordCreds(username, "test");
        Client client = _client.getContext().getClient();
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
