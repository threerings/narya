//
// $Id$
//
// Narya library - tools for developing networked games
// Copyright (C) 2002-2011 Three Rings Design, Inc., All Rights Reserved
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

package com.threerings.nio;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.nio.channels.SocketChannel;

import com.google.inject.Inject;
import com.google.inject.Singleton;

import com.samskivert.util.Lifecycle;
import com.samskivert.util.Tuple;

import com.threerings.nio.conman.Connection;
import com.threerings.nio.conman.ConnectionManager;
import com.threerings.nio.conman.ServerSocketChannelAcceptor;

import static com.threerings.NaryaLog.log;

/**
 * Binds to a port and responds to "xmlsocket" requests on it with a policy file allowing access
 * to all ports from any host.
 */
@Singleton
public class PolicyServer extends ConnectionManager
{
    public static void main (String[] args)
    {
        int port = MASTER_PORT;
        if (args.length > 0) {
            port = Integer.parseInt(args[0]);
        }
        PolicyServer server = null;
        try {
            server = new PolicyServer(new Lifecycle());
        } catch (IOException e) {
            System.err.println("Failed to create policy server!");
            e.printStackTrace(System.err);
            System.exit(1);
        }
        server.init(port);
    }


    @Inject
    public PolicyServer (Lifecycle cycle)
        throws IOException
    {
        super(cycle, LATENCY_GRACE);
    }

    /**
     * Accepts xmlsocket requests on <code>socketPolicyPort</code> and responds any host may
     * connect to any port on this host.
     */
    public void init (int socketPolicyPort)
    {
        _acceptor =
            new ServerSocketChannelAcceptor(null, new int[] { socketPolicyPort }, this);

        // build the XML once and for all
        StringBuilder policy = new StringBuilder("<cross-domain-policy>\n");
        // if we're running on 843, serve a master policy file
        if (socketPolicyPort == MASTER_PORT) {
            policy.append("  <site-control permitted-cross-domain-policies=\"master-only\"/>\n");
        }

        policy.append("  <allow-access-from domain=\"*\" to-ports=\"*\"/>\n");
        policy.append("</cross-domain-policy>\n");

        try {
            _policy = policy.toString().getBytes("UTF-8");
        } catch (UnsupportedEncodingException e) {
            throw new IllegalStateException("UTF-8 encoding missing; this vm is broken", e);
        }
        start();
    }

    @Override
    protected void willStart ()
    {
        super.willStart();
        if (!_acceptor.bind()) {
            log.warning("Policy server failed to bind!");
            shutdown();
        }
    }

    @Override
    protected void didShutdown ()
    {
        super.didShutdown();
        _acceptor.shutdown();
    }

    @Override
    protected void handleAcceptedSocket (SocketChannel channel)
    {
        Connection conn = new Connection() {
            public int handleEvent (long when) {
                // Ignore incoming data.  Should just be "<policy-file-request/>"
                return 0;
            }
        };
        handleAcceptedSocket(channel, conn);
        _outq.append(Tuple.newTuple(conn, _policy));
        postAsyncClose(conn);
    }

    protected ServerSocketChannelAcceptor _acceptor;

    protected byte[] _policy;

    protected static final int MASTER_PORT = 843;
}
