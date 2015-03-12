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

package com.threerings.presents.peer.server;

import java.util.List;
import java.util.Properties;
import java.util.logging.Level;
import java.util.logging.Logger;

import com.google.common.base.Preconditions;
import com.google.common.collect.Lists;
import com.google.inject.Guice;
import com.google.inject.Inject;
import com.google.inject.Injector;

import com.samskivert.depot.PersistenceContext;
import com.samskivert.depot.StaticConnectionProvider;

import com.threerings.presents.server.PresentsServer;

/**
 * Manages a collection of peer servers.
 */
public class PeerTestGroup
{
    public final List<PresentsServer> servers = Lists.newArrayList();

    public final List<Injector> injectors = Lists.newArrayList();

    public PeerTestGroup (int count, boolean suppressInfoLogging)
        throws Exception
    {
        if (suppressInfoLogging) {
            Logger.getLogger("").setLevel(Level.WARNING);
        }

        for (int ii = 0; ii < count; ii++) {
            final String nodename = "node" + ii;
            final int port = BASE_PORT + ii;

            Injector inj = Guice.createInjector(new PresentsServer.PresentsModule() {
                @Override protected void configure () {
                    super.configure();
                    bind(PresentsServer.class).toInstance(new PeerTestServer(port));
                    bind(PeerManager.class).to(TestPeerManager.class);
                    bind(PersistenceContext.class).toInstance(new PersistenceContext());
                }
            });
            this.injectors.add(inj);

            PersistenceContext pctx = inj .getInstance(PersistenceContext.class);
            Properties props = new Properties();
            props.put("default.driver", "org.hsqldb.jdbcDriver");
            props.put("default.url", "jdbc:hsqldb:mem:testdb");
            props.put("default.username", "sa");
            props.put("default.password", "");
            pctx.init("testdb", new StaticConnectionProvider(props), null);
            pctx.initializeRepositories(true);

            PresentsServer server = inj.getInstance(PresentsServer.class);
            server.init(inj);
            this.servers.add(server);

            PeerManager peermgr = inj.getInstance(PeerManager.class);
            peermgr.init(nodename, "I has a s3cr3t!", "localhost", "localhost", port);
        }
    }

    public void start ()
    {
        Preconditions.checkState(_threads.isEmpty(), "Group already started.");
        for (final PresentsServer server : this.servers) {
            Thread thread = new Thread() {
                @Override
                public void run () {
                    server.run();
                }
            };
            _threads.add(thread);
            thread.start();
        }
    }

    public void shutdown ()
    {
        for (PresentsServer server : this.servers) {
            server.queueShutdown();
        }
        try {
            // shutdown our servers
            for (Thread t : _threads) {
                t.join(1000);
            }
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }
    }

    protected static class PeerTestServer extends PresentsServer {
        public PeerTestServer (int port) {
            _ports = new int[] { port };
        }
        @Override protected int[] getListenPorts () {
            return _ports;
        }
        protected int[] _ports;
        @Inject protected PeerManager _pmgr; // trigger pmgr resolution
    }

    protected final List<Thread> _threads = Lists.newArrayList();

    protected static final int BASE_PORT = 1234;
}
