//
// $Id: MiCasaApplet.java,v 1.3 2001/10/25 23:29:00 mdb Exp $

package com.threerings.micasa.client;

import java.applet.Applet;
import java.io.IOException;
import com.samskivert.swing.util.SwingUtil;

import com.threerings.presents.client.Client;
import com.threerings.presents.client.ClientAdapter;
import com.threerings.presents.net.UsernamePasswordCreds;

import com.threerings.micasa.Log;

/**
 * The MiCasa applet is used to make MiCasa games available via the web
 * browser.
 */
public class MiCasaApplet extends Applet
{
    /**
     * Create the client instance and set things up.
     */
    public void init ()
    {
        try {
            // create a frame
            _frame = new MiCasaFrame();

            // create our client instance
            _client = new MiCasaClient(_frame);

            String username = getParameter("username");
            if (username == null) {
                throw new IOException("Missing username parameter.");
            }
            String authkey = getParameter("authkey");
            if (authkey == null) {
                throw new IOException("Missing authkey parameter.");
            }

            Client client = _client.getContext().getClient();

            // create and set our credentials
            client.setCredentials(
                new UsernamePasswordCreds(username, authkey));

            // we want to hide the client frame when we logoff
            client.addObserver(new ClientAdapter() {
                public void clientDidLogoff (Client c)
                {
                    _frame.setVisible(false);
                }
            });

        } catch (IOException ioe) {
            Log.warning("Unable to create client.");
            Log.logStackTrace(ioe);
        }
    }

    /**
     * Display the client frame and really get things going.
     */
    public void start ()
    {
        if (_client != null) {
            // show the frame
            _frame.setSize(800, 600);
            SwingUtil.centerWindow(_frame);
            _frame.setVisible(true);
            // and log on
            _client.getContext().getClient().logon();
        }
    }

    /**
     * Log off and shut on down.
     */
    public void stop ()
    {
        if (_client != null) {
            // hide the frame and log off
            _frame.setVisible(false);
            Client client = _client.getContext().getClient();
            if (client.loggedOn()) {
                client.logoff(false);
            }
        }
    }

    protected MiCasaClient _client;
    protected MiCasaFrame _frame;
}
