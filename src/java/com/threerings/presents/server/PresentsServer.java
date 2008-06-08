//
// $Id$
//
// Narya library - tools for developing networked games
// Copyright (C) 2002-2007 Three Rings Design, Inc., All Rights Reserved
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

package com.threerings.presents.server;

import com.google.inject.AbstractModule;
import com.google.inject.Guice;
import com.google.inject.Inject;
import com.google.inject.Injector;
import com.google.inject.Singleton;

import com.samskivert.util.Invoker;
import com.samskivert.util.RunQueue;
import com.samskivert.util.SystemInfo;

import com.threerings.presents.annotation.AuthInvoker;
import com.threerings.presents.annotation.EventQueue;
import com.threerings.presents.annotation.MainInvoker;
import com.threerings.presents.client.Client;
import com.threerings.presents.dobj.AccessController;
import com.threerings.presents.dobj.DObjectManager;
import com.threerings.presents.dobj.RootDObjectManager;
import com.threerings.presents.server.net.ConnectionManager;

import static com.threerings.presents.Log.log;

/**
 * The presents server provides a central point of access to the various facilities that make up
 * the presents framework. To facilitate extension and customization, a single instance of the
 * presents server should be created and initialized in a process. To facilitate easy access to the
 * services provided by the presents server, static references to the various managers are made
 * available in the <code>PresentsServer</code> class. These will be configured when the singleton
 * instance is initialized.
 */
@Singleton
public class PresentsServer
{
    /** Configures dependencies needed by the Presents services. */
    public static class Module extends AbstractModule
    {
        @Override protected void configure () {
            bind(Invoker.class).annotatedWith(MainInvoker.class).to(PresentsInvoker.class);
            bind(Invoker.class).annotatedWith(AuthInvoker.class).to(PresentsAuthInvoker.class);
            bind(RunQueue.class).annotatedWith(EventQueue.class).to(PresentsDObjectMgr.class);
            bind(DObjectManager.class).to(PresentsDObjectMgr.class);
            bind(RootDObjectManager.class).to(PresentsDObjectMgr.class);
        }
    }

    /** OBSOLETE! Don't use me. */
    public static ConnectionManager conmgr;

    /** OBSOLETE! Don't use me. */
    public static ClientManager clmgr;

    /** OBSOLETE! Don't use me. */
    public static PresentsDObjectMgr omgr;

    /** OBSOLETE! Don't use me. */
    public static InvocationManager invmgr;

    /** OBSOLETE! Don't use me. */
    public static Invoker invoker;

    /**
     * The default entry point for the server.
     */
    public static void main (String[] args)
    {
        Injector injector = Guice.createInjector(new Module());
        PresentsServer server = injector.getInstance(PresentsServer.class);
        try {
            // initialize the server
            server.init(injector);

            // check to see if we should load and invoke a test module before running the server
            String testmod = System.getProperty("test_module");
            if (testmod != null) {
                try {
                    log.info("Invoking test module [mod=" + testmod + "].");
                    Class tmclass = Class.forName(testmod);
                    Runnable trun = (Runnable)tmclass.newInstance();
                    trun.run();
                } catch (Exception e) {
                    log.warning("Unable to invoke test module '" + testmod + "'.", e);
                }
            }

            // start the server to running (this method call won't return until the server is shut
            // down)
            server.run();

        } catch (Exception e) {
            log.warning("Unable to initialize server.", e);
            System.exit(-1);
        }
    }

    /**
     * Initializes all of the server services and prepares for operation.
     */
    public void init (Injector injector)
        throws Exception
    {
        // populate our legacy statics
        conmgr = _conmgr;
        clmgr = _clmgr;
        omgr = _omgr;
        invmgr = _invmgr;
        invoker = _invoker;

        // output general system information
        SystemInfo si = new SystemInfo();
        log.info("Starting up server [os=" + si.osToString() + ", jvm=" + si.jvmToString() +
                 ", mem=" + si.memoryToString() + "].");

        // register SIGTERM, SIGINT (ctrl-c) and a SIGHUP handlers
        boolean registered = false;
        try {
            registered = injector.getInstance(SunSignalHandler.class).init();
        } catch (Throwable t) {
            log.warning("Unable to register Sun signal handlers [error=" + t + "].");
        }
        if (!registered) {
            injector.getInstance(NativeSignalHandler.class).init();
        }

        // configure the dobject manager with our access controller
        _omgr.setDefaultAccessController(createDefaultObjectAccessController());

        // start the main and auth invoker threads
        _invoker.start();
        _authInvoker.start();

        // configure our connection manager
        _conmgr.init(getListenPorts(), getDatagramPorts());

        // initialize the time base services
        TimeBaseProvider.init(invmgr, omgr);
    }

    /**
     * Defines the default object access policy for all {@link DObject} instances. The default
     * default policy is to allow all subscribers but reject all modifications by the client.
     */
    protected AccessController createDefaultObjectAccessController ()
    {
        return PresentsObjectAccess.DEFAULT;
    }

    /**
     * Returns the port on which the connection manager will listen for client connections.
     */
    protected int[] getListenPorts ()
    {
        return Client.DEFAULT_SERVER_PORTS;
    }

    /**
     * Returns the ports on which the connection manager will listen for datagrams.
     */
    protected int[] getDatagramPorts ()
    {
        return Client.DEFAULT_DATAGRAM_PORTS;
    }

    /**
     * Starts up all of the server services and enters the main server event loop.
     */
    public void run ()
    {
        // post a unit that will start up the connection manager when everything else in the
        // dobjmgr queue is processed
        omgr.postRunnable(new Runnable() {
            public void run () {
                // start up the connection manager
                _conmgr.start();
            }
        });
        // invoke the dobjmgr event loop
        omgr.run();
    }

    /**
     * Called once the invoker and distributed object manager have both completed processing all
     * remaining events and are fully shutdown. <em>Note:</em> this is called as the last act of
     * the invoker <em>on the invoker thread</em>. In theory no other (important) threads are
     * running, so thread safety should not be an issue, but be careful!
     */
    protected void invokerDidShutdown ()
    {
    }

    /** The manager of distributed objects. */
    @Inject protected PresentsDObjectMgr _omgr;

    /** The manager of network connections. */
    @Inject protected ConnectionManager _conmgr;

    /** The manager of clients. */
    @Inject protected ClientManager _clmgr;

    /** The manager of invocation services. */
    @Inject protected InvocationManager _invmgr;

    /** Handles orderly shutdown of our managers, etc. */
    @Inject protected ShutdownManager _shutmgr;

    /** Handles generation of state of the server reports. */
    @Inject protected ReportManager _repmgr;

    /** Used to invoke background tasks that should not be allowed to tie up the distributed object
     * manager thread (generally talking to databases and other relatively slow entities). */
    @Inject @MainInvoker protected Invoker _invoker;

    /** Used to invoke authentication tasks. */
    @Inject @AuthInvoker protected Invoker _authInvoker;
}
