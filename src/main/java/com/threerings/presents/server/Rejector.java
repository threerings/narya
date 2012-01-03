//
// $Id$
//
// Narya library - tools for developing networked games
// Copyright (C) 2002-2012 Three Rings Design, Inc., All Rights Reserved
// http://code.google.com/p/narya/
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

package com.threerings.presents.server;

import com.samskivert.util.StringUtil;
import com.threerings.util.MessageBundle;

import com.threerings.presents.net.AuthResponse;
import com.threerings.presents.server.net.AuthingConnection;

import static com.threerings.presents.Log.log;

/**
 * A simple server that does nothing more than spit out a canned error response to everyone who
 * logs in.
 */
public class Rejector extends PresentsServer
{
    /** Configures dependencies needed by the Rejector. */
    public static class RejectorModule extends PresentsModule
    {
        @Override protected void configure () {
            super.configure();
            bind(Authenticator.class).to(RejectingAuthenticator.class);
        }
    }

    @Override
    protected int[] getListenPorts ()
    {
        return _ports;
    }

    public static void main (String[] args)
    {
        if (args.length < 2) {
            System.err.println("Usage: Rejector ports error_msg [args]");
            System.exit(-1);
        }

        _ports = StringUtil.parseIntArray(args[0]);
        _errmsg = args[1];
        if (args.length > 2) {
            String[] eargs = new String[args.length-2];
            System.arraycopy(args, 2, eargs, 0, eargs.length);
            _errmsg = MessageBundle.tcompose(_errmsg, eargs);
        }

        runServer(new RejectorModule(), new PresentsServerModule(Rejector.class));
    }

    /**
     * An authenticator implementation that refuses all authentication requests.
     */
    protected class RejectingAuthenticator extends Authenticator
    {
        @Override
        protected void processAuthentication (AuthingConnection conn, AuthResponse rsp)
            throws Exception
        {
            log.info("Rejecting request: " + conn.getAuthRequest());
            throw new AuthException(_errmsg);
        }
    }

    protected static String _errmsg;
    protected static int[] _ports;
}
