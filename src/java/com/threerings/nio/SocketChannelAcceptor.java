package com.threerings.nio;

import java.util.List;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.channels.SelectionKey;
import java.nio.channels.ServerSocketChannel;
import java.nio.channels.SocketChannel;

import com.google.common.collect.Lists;
import com.threerings.nio.SelectorIterable.SelectFailureHandler;

import static com.threerings.presents.Log.log;


/**
 * Listens for socket connections on a set of ports for a hostname and passes those connected
 * sockets on to a listener when they're ready. {@link #listen()} must be called after
 * creating the acceptor to open the channels, and then tick must be called periodically to process
 * new connections.
 */
public class SocketChannelAcceptor extends SelectAcceptor
{
    public interface SocketChannelHandler
    {
        void handleSocketChannel (SocketChannel channel, long when);
    }

    /**
     * Creates an acceptor that passes socket connections on to the given handler.
     *
     * @param failureHandler - called when the selector is irredemably broken.
     * @param bindHostname - the hostname to bind to or null for all interfaces
     * @param ports - the ports to bind to, or an empty array to skip binding
     * @param selectLoopTime - the amount of time to wait in select, or 0 to skip the wait at all.
     */
    public SocketChannelAcceptor (SocketChannelHandler connectionHandler,
        SelectFailureHandler failureHandler, String bindHostname, int[] ports, int selectLoopTime)
        throws IOException
    {
        super(failureHandler, bindHostname, ports, selectLoopTime);

        _connHandler = connectionHandler;
    }

    protected void acceptConnection (ServerSocketChannel listener, long when)
    {
        SocketChannel channel = null;
        try {
            channel = listener.accept();
        } catch (IOException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
        if (channel == null) {
            // in theory this shouldn't happen because we got an ACCEPT_READY event...
            log.info("Psych! Got ACCEPT_READY, but no connection.");
            return;
        }

//         log.debug("Accepted connection " + channel + ".");

        _connHandler.handleSocketChannel(channel, when);
    }


    /**
     * Listens for socket connections on the configured addresses.
     */
    public boolean listen ()
    {
        int successes = 0;
        for (int port : _ports) {
            try {
                // create a listening socket and add it to the select set
                final ServerSocketChannel ssocket = ServerSocketChannel.open();
                ssocket.configureBlocking(false);

                InetSocketAddress isa = getAddress(port);
                ssocket.socket().bind(isa);
                SelectionKey sk = ssocket.register(_selector, SelectionKey.OP_ACCEPT);
                _handlers.put(sk, new SelectionHandler() {
                    public void handle (long when) {
                        acceptConnection(ssocket, when);
                    }
                });
                successes++;
                log.info("Server listening on " + isa + ".");
                _ssockets.add(ssocket);

            } catch (IOException ioe) {
                log.warning("Failure listening to socket", "hostname", _bindHostname,
                            "port", port, ioe);
            }
        }

        // NOTE: this is not currently working; it works but for whatever inscrutable reason the
        // inherited channel claims to be readable immediately every time through the select() loop
        // which causes the server to consume 100% of the CPU repeatedly ignoring the inherited
        // channel (except when an actual connection comes in in which case it does the right
        // thing)

//         // now look to see if we were passed a socket inetd style by a
//         // privileged parent process
//         try {
//             Channel inherited = System.inheritedChannel();
//             if (inherited instanceof ServerSocketChannel) {
//                 _ssocket = (ServerSocketChannel)inherited;
//                 _ssocket.configureBlocking(false);
//                 registerChannel(_ssocket);
//                 successes++;
//                 log.info("Server listening on " +
//                          _ssocket.socket().getInetAddress() + ":" +
//                          _ssocket.socket().getLocalPort() + ".");

//             } else if (inherited != null) {
//                 log.warning("Inherited non-server-socket channel " + inherited + ".");
//             }
//         } catch (IOException ioe) {
//             log.warning("Failed to check for inherited channel.");
//         }

        // if we failed to listen on at least one port, give up the ghost
        return successes > 0;
    }

    public void shutdown()
    {
        // unbind our listening socket
        // Note: because we wait for the object manager to exit before we do, we will still be
        // accepting connections as long as there are events pending.
        for (ServerSocketChannel ssocket : _ssockets) {
            try {
                ssocket.socket().close();
            } catch (IOException ioe) {
                log.warning("Failed to close listening socket: " + ssocket, ioe);
            }
        }
    }

    protected final List<ServerSocketChannel> _ssockets = Lists.newArrayList();
    protected final SocketChannelHandler _connHandler;
}
