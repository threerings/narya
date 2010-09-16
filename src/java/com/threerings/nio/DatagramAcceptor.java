package com.threerings.nio;

import java.util.List;
import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.channels.DatagramChannel;
import java.nio.channels.SelectionKey;
import com.google.common.collect.Lists;
import com.threerings.nio.SelectorIterable.SelectFailureHandler;

import static com.threerings.presents.Log.log;

/**
 * Listens for datagrams arriving on a set of ports for a hostname and passes those datagrams on
 * to a listener when they're ready to read. {@link #listen()} must be called after creating the
 * acceptor to open the channels, and then tick must be called periodically to process new
 * datagrams.
 */
public class DatagramAcceptor extends SelectAcceptor
{
    /**
     * Callback when datagrams are ready to be read.
     */
    public interface DatagramHandler
    {
        /**
         * Reads the message currentnly in the channel that was selected at the given time.
         */
        void handleDatagram (DatagramChannel channel, long when);
    }

    /**
     * Creates an acceptor that passes datagrams on to the given handler.
     * @param failureHandler - called when the selector is irredemably broken.
     * @param hostname - the hostname to bind to or null for all interfaces
     * @param ports - the ports to bind to, or an empty array to skip binding
     */
    public DatagramAcceptor (DatagramHandler dgramHandler, SelectFailureHandler failureHandler,
            String hostname, int[] ports)
        throws IOException
    {
        super(failureHandler, hostname, ports);
        _dgramHandler = dgramHandler;
    }

    /**
     * Opens datagram channels on the configured addresses.
     */
    public boolean listen ()
    {
        // open up the datagram ports as well
        for (int port : _ports) {
            try {
                // create a channel and add it to the select set
                final DatagramChannel channel = DatagramChannel.open();
                channel.socket().setTrafficClass(0x10); // IPTOS_LOWDELAY
                channel.configureBlocking(false);
                InetSocketAddress isa = getAddress(port);
                channel.socket().bind(isa);
                SelectionKey sk = channel.register(_selector, SelectionKey.OP_READ);
                _handlers.put(sk, new SelectionHandler() {
                    public void handle (long when) {
                        _dgramHandler.handleDatagram(channel, when);
                    }
                });
                _datagramChannels.add(channel);
                log.info("Server accepting datagrams on " + isa + ".");

            } catch (IOException ioe) {
                log.warning("Failure opening datagram channel", "hostname", _bindHostname,
                            "port", port, ioe);
            }
        }
        return true;
    }

    /**
     * Closes any open channels.
     */
    public void shutdown()
    {
        // unbind sockets, if any
        for (DatagramChannel datagramChannel : _datagramChannels) {
            datagramChannel.socket().close();
        }

    }

    protected final List<DatagramChannel> _datagramChannels = Lists.newArrayList();
    protected final DatagramHandler _dgramHandler;
}
