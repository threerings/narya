//
// $Id: MiCasaApplet.java,v 1.5 2002/01/19 04:16:02 mdb Exp $

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
            _client = new MiCasaClient();
            _client.init(_frame);

            String username = requireParameter("username");
            String authkey = requireParameter("authkey");
            String server = getCodeBase().getHost();

            Client client = _client.getContext().getClient();

            // indicate which server to which we should connect
            client.setServer(server, Client.DEFAULT_SERVER_PORT);

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

    protected String requireParameter (String name)
        throws IOException
    {
        String value = getParameter(name);
        if (value == null) {
            throw new IOException("Applet missing '" + name + "' parameter.");
        }
        return value;
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
