//
// $Id: JabberApp.java 3098 2004-08-27 02:12:55Z mdb $
//
// Narya library - tools for developing networked games
// Copyright (C) 2002-2004 Three Rings Design, Inc., All Rights Reserved
// http://www.threerings.net/code/narya/
//
// This library is free software; you can redistribute it and/or modify it
// under the terms of the GNU Lesser General Public License as published
// by the Free Software Foundation; either version 2.1 of the License, or
// (at your option) any later version.
//
// This library is distributed in the hope that it will be useful,
// but WITHOUT ANY WARRANTY; without even the implied warranty of
// MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
// Lesser General Public License for more details.
//
// You should have received a copy of the GNU Lesser General Public
// License along with this library; if not, write to the Free Software
// Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA 02111-1307 USA

package com.threerings.crowd.client;

import java.io.IOException;
import javax.swing.JFrame;

import com.samskivert.swing.util.SwingUtil;
import com.threerings.util.Name;

import com.threerings.presents.client.Client;
import com.threerings.presents.net.UsernamePasswordCreds;

import com.threerings.crowd.Log;

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
        Log.info("Using [server=" + server + "].");
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
            Log.warning("Error initializing application.");
            Log.logStackTrace(ioe);
        }

        // and run it
        app.run(server, username, password, roomId);
    }

    protected JabberClient _client;
    protected JFrame _frame;
}
