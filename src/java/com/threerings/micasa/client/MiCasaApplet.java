//
// $Id: MiCasaApplet.java,v 1.1 2001/10/25 23:03:48 mdb Exp $

package com.threerings.micasa.client;

import java.applet.Applet;
import java.io.IOException;
import com.samskivert.swing.util.SwingUtil;

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

            // create our credentials
            _client.getContext().getClient().setCredentials(
                new UsernamePasswordCreds(username, authkey));

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
            _client.getContext().getClient().logoff(false);
        }
    }

    protected MiCasaClient _client;
    protected MiCasaFrame _frame;
}
