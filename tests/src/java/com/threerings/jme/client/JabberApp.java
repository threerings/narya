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

package com.threerings.jme.client;

import com.jme.util.LoggingSystem;

import com.threerings.util.Name;

import com.threerings.presents.client.Client;
import com.threerings.presents.net.UsernamePasswordCreds;

import com.threerings.jme.Log;
import com.threerings.jme.JmeApp;

/**
 * The main point of entry for the Jabber client application. It creates
 * and initializes the myriad components of the client and sets all the
 * proper wheels in motion.
 */
public class JabberApp extends JmeApp
{
    // documentation inherited
    public void init ()
    {
        super.init();

        // initialize our client instance
        _client = new JabberClient();
        _client.init(this);
    }

    public void run (String server, int port, String username, String password)
    {
        Client client = _client.getContext().getClient();

        // pass them on to the client
        Log.info("Using [server=" + server + ", port=" + port + "].");
        client.setServer(server, port);

        // configure the client with some credentials and logon
        if (username != null && password != null) {
            // create and set our credentials
            client.setCredentials(
                new UsernamePasswordCreds(new Name(username), password));
            client.logon();
        }

        // now start up the main event loop
        run();
    }

    // documentation inherited
    public void stop ()
    {
        // log off before we shutdown
        Client client = _client.getContext().getClient();
        if (client.isLoggedOn()) {
            client.logoff(false);
        }
        Log.info("Stopping.");
        super.stop();
    }

    public static void main (String[] args)
    {
        LoggingSystem.getLogger().setLevel(java.util.logging.Level.OFF);

        String server = "localhost";
        if (args.length > 0) {
            server = args[0];
        }

        int port = Client.DEFAULT_SERVER_PORT;
        if (args.length > 1) {
            try {
                port = Integer.parseInt(args[1]);
            } catch (NumberFormatException nfe) {
                Log.warning("Invalid port specification '" + args[1] + "'.");
            }
        }

        String username = (args.length > 2) ? args[2] : null;
        String password = (args.length > 3) ? args[3] : null;

        JabberApp app = new JabberApp();
        app.init();
        app.run(server, port, username, password);
    }

    protected JabberClient _client;
}
