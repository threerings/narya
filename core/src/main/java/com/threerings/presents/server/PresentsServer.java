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

import com.google.inject.AbstractModule;
import com.google.inject.Guice;
import com.google.inject.Inject;
import com.google.inject.Injector;
import com.google.inject.Key;
import com.google.inject.Module;
import com.google.inject.Singleton;

import com.samskivert.util.Invoker;
import com.samskivert.util.Lifecycle;
import com.samskivert.util.RunQueue;
import com.samskivert.util.SystemInfo;

import com.threerings.presents.annotation.AuthInvoker;
import com.threerings.presents.annotation.EventQueue;
import com.threerings.presents.annotation.MainInvoker;
import com.threerings.presents.annotation.PeerInvoker;
import com.threerings.presents.client.Client;
import com.threerings.presents.dobj.AccessController;
import com.threerings.presents.dobj.DObject;
import com.threerings.presents.dobj.DObjectManager;
import com.threerings.presents.dobj.RootDObjectManager;
import com.threerings.presents.server.PresentsDObjectMgr.LongRunnable;
import com.threerings.presents.server.net.DatagramChannelReader;
import com.threerings.presents.server.net.PresentsConnectionManager;

import com.threerings.crowd.server.PlaceManager;

import com.threerings.nio.conman.ConnectionManager;
import com.threerings.nio.conman.ServerSocketChannelAcceptor;

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
    public static class PresentsModule extends AbstractModule
    {
        @Override protected void configure () {
            bindInvokers();
            bindPeerInvoker();
            bind(ConnectionManager.class).to(PresentsConnectionManager.class);
            bind(RunQueue.class).annotatedWith(EventQueue.class).to(PresentsDObjectMgr.class);
            bind(DObjectManager.class).to(PresentsDObjectMgr.class);
            bind(RootDObjectManager.class).to(PresentsDObjectMgr.class);
            bind(Lifecycle.class).toInstance(new Lifecycle());
        }

        /**
         * Binds just the invokers so this can be overridden if desired.
         */
        protected void bindInvokers () {
            bind(Invoker.class).annotatedWith(MainInvoker.class).to(PresentsInvoker.class);
            bind(Invoker.class).annotatedWith(AuthInvoker.class).to(PresentsAuthInvoker.class);
        }

        /**
         * Binds just the peer invoker - we separate this from the others for legacy reasons.
         */
        protected void bindPeerInvoker () {
            bind(Invoker.class).annotatedWith(PeerInvoker.class).to(
                Key.get(Invoker.class, MainInvoker.class));
        }
    }

    /**
     * Binds PresentsServer to a particular class.
     */
    public static class PresentsServerModule extends AbstractModule
    {
        public PresentsServerModule (Class<? extends PresentsServer> serverType) {
            _serverType = serverType;
        }

        @Override protected void configure () {
            bind(PresentsServer.class).to(_serverType);
        }

        protected final Class<? extends PresentsServer> _serverType;
    }

    /**
     * Inits and runs the PresentsServer bound in the given modules.  This blocks until the server
     * is shut down.
     */
    public static void runServer (Module... modules)
    {
        Injector injector = Guice.createInjector(modules);
        PresentsServer server = injector.getInstance(PresentsServer.class);
        try {
            // initialize the server
            server.init(injector);

            // check to see if we should load and invoke a test module before running the server
            String testmod = System.getProperty("test_module");
            if (testmod != null) {
                try {
                    log.info("Invoking test module", "mod", testmod);
                    Class<?> tmclass = Class.forName(testmod);
                    Runnable trun = (Runnable)tmclass.newInstance();
                    trun.run();
                } catch (Exception e) {
                    log.warning("Unable to invoke test module '" + testmod + "'.", e);
                }
            }

            // start the server to running (this method call won't return until the server is shut
            // down)
            server.run();

        } catch (Throwable t) {
            log.warning("Unable to initialize server.", t);
            System.exit(-1);
        }
    }

    /**
     * The default entry point for the server.
     */
    public static void main (String[] args)
    {
        runServer(new PresentsModule());
    }

    /** Legacy static reference to the main distributed object manager. Don't use this. If you're
     * writing a game, use {@link PlaceManager#_omgr}. */
    @Deprecated public static PresentsDObjectMgr omgr;

    /** Legacy static reference to the invocation manager. Don't use this. If you're
     * writing a game, use {@link PlaceManager#_invmgr}. */
    @Deprecated public static InvocationManager invmgr;

    /**
     * Initializes all of the server services and prepares for operation.
     */
    public void init (Injector injector)
        throws Exception
    {
        // output general system information
        SystemInfo si = new SystemInfo();
        log.info("Starting up", "server", getClass().getSimpleName(), "os", si.osToString(),
            "jvm", si.jvmToString());

        registerSignalHandlers(injector);

        // initialize our deprecated legacy static references
        omgr = _omgr;
        invmgr = _invmgr;

        // configure the dobject manager with our access controller
        _omgr.setDefaultAccessController(createDefaultObjectAccessController());

        // start the main and auth invoker threads
        ((PresentsInvoker)_invoker).start(this);
        _authInvoker.start();
        ((PresentsInvoker)_invoker).addInterdependentInvoker(_authInvoker);

        // provide our client manager with the injector it needs
        _clmgr.setInjector(injector);

        // Create our socket opening objects now that we know what ports we're using.
        _lifecycle.addComponent(_socketAcceptor = createSocketAcceptor());
        _datagramReader =
            new DatagramChannelReader(getDatagramHostname(), getDatagramPorts(), _conmgr);
        _lifecycle.addComponent(_datagramReader);

        // initialize the time base services
        TimeBaseProvider.init(_invmgr, _omgr);

        // make the client manager shut down before the invoker and dobj threads; this helps
        // application code to avoid long chains of shutdown constraints
        _lifecycle.addShutdownConstraint(
            _clmgr, Lifecycle.Constraint.RUNS_BEFORE, (PresentsInvoker)_invoker);

        // initialize all of our registered components
        _lifecycle.init();
    }

    protected void registerSignalHandlers (Injector injector)
    {
        // register SIGTERM, SIGINT (ctrl-c) and a SIGHUP handlers
        try {
            injector.getInstance(SunSignalHandler.class).init();
        } catch (Throwable t) {
            log.warning("Unable to register Sun signal handlers", "error", t);
        }
    }

    protected ServerSocketChannelAcceptor createSocketAcceptor ()
    {
        return new ServerSocketChannelAcceptor(getBindHostname(), getListenPorts(), _conmgr);
    }

    /**
     * Starts up all of the server services and enters the main server event loop.
     */
    public void run ()
    {
        // wait till everything in the dobjmgr queue and invokers are processed and then start the
        // connection manager
        ((PresentsInvoker)_invoker).postRunnableWhenEmpty(new Runnable() {
            public void run () {
                // Take as long as you like opening to the public
                _omgr.postRunnable(new LongRunnable() {
                    public void run () {
                        openToThePublic();
                    }
                });
            }
        });
        // invoke the dobjmgr event loop
        _omgr.run();
    }

    /**
     * Opens the server for connections after the event thread is running and all the invokers are
     * clear after starting up.
     */
    protected void openToThePublic ()
    {
        if (getListenPorts().length != 0 && !_socketAcceptor.bind()) {
            queueShutdown();
        } else {
            _datagramReader.bind();
            _conmgr.start();
        }
    }

    /**
     * Queues up a request to shutdown on the event thread. This method may be safely called from
     * any thread.
     */
    public void queueShutdown ()
    {
        _omgr.postRunnable(new PresentsDObjectMgr.LongRunnable() {
            public void run () {
                _lifecycle.shutdown();
            }
        });
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
     * Returns the hostname on which the connection manager will listen for TCP traffic, or
     * <code>null</code> to bind to the wildcard address.
     */
    protected String getBindHostname ()
    {
        return null;
    }

    /**
     * Returns the hostname on which the connection will listen for datagram traffic, or
     * <code>null</code> to bind to the wildcard address.
     */
    protected String getDatagramHostname ()
    {
        return null;
    }

    /**
     * Returns the port on which the connection manager will listen for client connections or an
     * empty array to skip binding and have an external entity transfer accepted sockets into
     * the connection manager.
     */
    protected int[] getListenPorts ()
    {
        return Client.DEFAULT_SERVER_PORTS;
    }

    /**
     * Returns the ports on which the connection manager will listen for datagrams or an empty
     * array if no datagrams are desired.
     */
    protected int[] getDatagramPorts ()
    {
        return Client.DEFAULT_DATAGRAM_PORTS;
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

    protected ServerSocketChannelAcceptor _socketAcceptor;

    protected DatagramChannelReader _datagramReader;

    /** The manager of distributed objects. */
    @Inject protected PresentsDObjectMgr _omgr;

    /** The manager of network connections. */
    @Inject protected PresentsConnectionManager _conmgr;

    /** The manager of clients. */
    @Inject protected ClientManager _clmgr;

    /** The manager of invocation services. */
    @Inject protected InvocationManager _invmgr;

    /** Handles orderly initialization and shutdown of our managers, etc. */
    @Inject protected Lifecycle _lifecycle;

    /** Used to invoke background tasks that should not be allowed to tie up the distributed object
     * manager thread (generally talking to databases and other relatively slow entities). */
    @Inject @MainInvoker protected Invoker _invoker;

    /** Used to invoke authentication tasks. */
    @Inject @AuthInvoker protected Invoker _authInvoker;
}
