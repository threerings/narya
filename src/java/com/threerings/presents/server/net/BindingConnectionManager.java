package com.threerings.presents.server.net;

import java.io.IOException;

import com.google.common.base.Preconditions;
import com.google.inject.Inject;
import com.google.inject.Singleton;
import com.samskivert.util.Lifecycle;

import com.threerings.presents.server.ReportManager;
import com.threerings.nio.DatagramAcceptor;
import com.threerings.nio.SocketChannelAcceptor;

import static com.threerings.presents.Log.log;

/**
 * Binds tcp sockets and listens for datagrams in addition to ConnectionManager's normal duties.
 */
@Singleton
public class BindingConnectionManager extends ConnectionManager
{
    @Inject public BindingConnectionManager (Lifecycle cycle, ReportManager repmgr,
        IncomingEventWaitHolder incomingEventWait)
        throws IOException
    {
        super(cycle, repmgr, incomingEventWait);
    }

    @Override
    protected void willStart ()
    {
        if (!_socketAcceptor.listen()) {
            log.warning("ConnectionManager failed to bind to any ports. Shutting down.");
            _server.queueShutdown();
        } else {
            _dgramAcceptor.listen();
        }
    }

    @Override
    protected void processIncomingEvents (long iterStamp)
    {
        _socketAcceptor.tick(iterStamp);
        _dgramAcceptor.tick(iterStamp);
        super.processIncomingEvents(iterStamp);
    }

    /**
     * Configures the connection manager with the hostname and ports on which it will listen for
     * socket connections and datagram packets. This must be called before the connection manager
     * is started (via {@via #start}) as the sockets will be bound at that time.
     *
     * @param socketHostname the hostname to which we bind our sockets or null to bind to all
     * interfaces.
     * @param datagramHostname the hostname to which we bind our datagram socket or null to bind
     * to all interfaces.
     * @param socketPorts the ports on which to listen for TCP connection.
     * @param datagramPorts the ports on which to listen for datagram packets.
     */
    public void init (String socketHostname, String datagramHostname, int[] socketPorts,
        int[] datagramPorts)
        throws IOException
    {
        Preconditions.checkNotNull(socketPorts, "Ports must be non-null.");
        Preconditions.checkNotNull(datagramPorts, "Datagram ports must be non-null. " +
                                    "Pass a zero-length array to bind no datagram ports.");

        // Listen for socket connections and datagram connections, but don't wait for anything to
        // show up since that check is occurring as part of ConnectionManager's incoming event loop
        // that already has a wait.
        _socketAcceptor = new SocketChannelAcceptor(this, _failureHandler, socketHostname,
            socketPorts, 0);
        _dgramAcceptor = new DatagramAcceptor(this, _failureHandler, datagramHostname,
            datagramPorts, 0);
    }

    @Override
    protected void didShutdown ()
    {
        super.didShutdown();

        // TODO: consider closing the listen sockets earlier, like in the shutdown method
        _socketAcceptor.shutdown();
        _dgramAcceptor.shutdown();
    }


    protected SocketChannelAcceptor _socketAcceptor;
    protected DatagramAcceptor _dgramAcceptor;
}
