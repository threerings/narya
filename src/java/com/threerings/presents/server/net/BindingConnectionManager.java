package com.threerings.presents.server.net;

import java.io.IOException;

import com.google.common.base.Preconditions;
import com.google.inject.Inject;
import com.google.inject.Singleton;

import com.samskivert.util.Lifecycle;

import com.threerings.presents.server.ReportManager;

import com.threerings.nio.SocketChannelAcceptor;
import com.threerings.nio.DatagramAcceptor;

import static com.threerings.presents.Log.log;

/**
 * Binds tcp sockets and listens for datagrams in addition to ConnectionManager's normal duties.
 */
@Singleton
public class BindingConnectionManager extends ConnectionManager
{

    @Inject public BindingConnectionManager (Lifecycle cycle, ReportManager repmgr)
        throws IOException
    {
        super(cycle, repmgr);
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

        _socketAcceptor = new SocketChannelAcceptor(this, _failureHandler, socketHostname,
            socketPorts);
        _dgramAcceptor = new DatagramAcceptor(this, _failureHandler, datagramHostname,
            datagramPorts);
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
