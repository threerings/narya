//
// Narya library - tools for developing networked games
// Copyright (C) 2002-2025 Three Rings Design, Inc., All Rights Reserved
// https://github.com/threerings/narya/blob/master/LICENSE

package com.threerings.crowd.client;

import java.io.IOException;

import javax.swing.JFrame;

import com.samskivert.swing.util.SwingUtil;

import com.threerings.util.Name;

import com.threerings.presents.client.Client;
import com.threerings.presents.net.UsernamePasswordCreds;

import static com.threerings.crowd.Log.log;

/**
 * The main point of entry for the Jabber client application. It creates
 * and initializes the myriad components of the client and sets all the
 * proper wheels in motion.
 */
public class JabberApp
{
    public void init ()
        throws IOException
    {
        // create a frame
        _frame = new JFrame("Jabber Client");

        // initialize our client instance
        _client = new JabberClient();
        _client.init(_frame);
    }

    public void run (
        String server, String username, String password, int roomId)
    {
        // position everything and show the frame
        _frame.setSize(800, 600);
        SwingUtil.centerWindow(_frame);
        _frame.setVisible(true);

        _client.setRoom(roomId);
        Client client = _client.getContext().getClient();

        // pass them on to the client
        log.info("Using [server=" + server + "].");
        client.setServer(server, Client.DEFAULT_SERVER_PORTS);

        // configure the client with some credentials and logon
        if (username != null && password != null) {
            // create and set our credentials
            client.setCredentials(
                new UsernamePasswordCreds(new Name(username), password));
            client.logon();
        }
    }

    public static void main (String[] args)
    {
        String server = "localhost";
        if (args.length > 0) {
            server = args[0];
        }
        String username = (args.length > 1) ? args[1] : null;
        String password = (args.length > 2) ? args[2] : null;
        int roomId = 2;
        if (args.length > 4) {
            try {
               roomId = Integer.parseInt(args[4]);
            } catch (NumberFormatException e) {
            // so what?
            }
        }

        JabberApp app = new JabberApp();
        try {
            // initialize the app
            app.init();
        } catch (IOException ioe) {
            log.warning("Error initializing application.", ioe);
        }

        // and run it
        app.run(server, username, password, roomId);
    }

    protected JabberClient _client;
    protected JFrame _frame;
}
