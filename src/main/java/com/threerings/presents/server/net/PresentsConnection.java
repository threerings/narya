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

package com.threerings.presents.server.net;

import java.io.EOFException;
import java.io.IOException;
import java.io.InputStream;
import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.DatagramChannel;
import java.nio.channels.SocketChannel;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

import com.google.common.base.Preconditions;

import com.threerings.io.FramedInputStream;
import com.threerings.io.FramingOutputStream;
import com.threerings.io.ObjectInputStream;
import com.threerings.io.ObjectOutputStream;

import com.threerings.presents.net.Message;
import com.threerings.presents.util.DatagramSequencer;

import com.threerings.nio.conman.Connection;
import com.threerings.nio.conman.ConnectionManager;

import static com.threerings.presents.Log.log;

/**
 * Parses incoming network data into a stream of {@link Message} objects, sends messages to the
 * client and adds datagram support to a connection.
 */
public class PresentsConnection extends Connection
{
    /** Used with {@link #setMessageHandler}. */
    public static interface MessageHandler {
        /** Called when a complete message has been parsed from incoming network data. */
        void handleMessage (Message message);
    }

    /**
     * Initializes the connection with its channel. Must be called with a
     * {@link PresentsConnectionManager} as <code>cmgr</code>.
     */
    @Override
    public void init (ConnectionManager cmgr, SocketChannel channel, long createStamp)
        throws IOException
    {
        super.init(cmgr, channel, createStamp);
        _pcmgr = (PresentsConnectionManager)cmgr;
    }

    /**
     * Instructs the connection to pass parsed messages on to this handler for processing. This
     * should be done before the connection is turned loose to process messages.
     */
    public void setMessageHandler (MessageHandler handler)
    {
        _handler = Preconditions.checkNotNull(handler);
    }

    /**
     * Clears out our message handler, causing any subsequent messages to be dropped on arrival. A
     * log message is recorded for the dropped messages.
     */
    public void clearMessageHandler ()
    {
        _handler = new MessageHandler() {
            public void handleMessage (Message message) {
                log.info("Dropping message from cleared connection", "conn", this, "msg", message);
            }
        };
    }

    /**
     * Configures this connection with a custom class loader.
     */
    public void setClassLoader (ClassLoader loader)
    {
        _loader = loader;
        if (_oin != null) {
            _oin.setClassLoader(loader);
        }
    }

    /**
     * Sets whether we should transmit datagrams.
     */
    public void setTransmitDatagrams (boolean transmit)
    {
        _transmitDatagrams = transmit;
    }

    /**
     * Checks whether we should transmit datagrams.
     */
    public boolean getTransmitDatagrams ()
    {
        return _transmitDatagrams;
    }

    /**
     * Returns the address to which datagrams should be sent or null if no datagram address has
     * been established.
     */
    public InetSocketAddress getDatagramAddress ()
    {
        return _datagramAddress;
    }

    /**
     * Returns the channel through which datagrams should be sent or null if no datagram channel
     * has been established.
     */
    public DatagramChannel getDatagramChannel ()
    {
        return _datagramChannel;
    }

    /**
     * Sets the secret string used to authenticate datagrams from the client.
     */
    public void setDatagramSecret (String secret)
    {
        try {
            _datagramSecret = secret.getBytes("UTF-8");
        } catch (Exception e) {
            _datagramSecret = new byte[0]; // shouldn't happen
        }
    }

    /**
     * Posts a message for delivery to this connection. The message will be delivered by the conmgr
     * thread as soon as it gets to it.
     */
    public void postMessage (Message msg)
    {
        // pass this along to the connection manager
        _pcmgr.postMessage(this, msg);
    }

    /**
     * Processes a datagram sent to this connection.
     */
    public void handleDatagram (InetSocketAddress source, DatagramChannel channel,
        ByteBuffer buf, long when)
    {
        // lazily create our various bits and bobs
        if (_digest == null) {
            try {
                _digest = MessageDigest.getInstance("MD5");
            } catch (NoSuchAlgorithmException nsae) {
                log.warning("Missing MD5 algorithm.");
                return;
            }
            _sequencer = _pcmgr.createDatagramSequencer();
        }

        // verify the hash
        buf.position(12);
        _digest.update(buf);
        byte[] hash = _digest.digest(_datagramSecret);
        buf.position(4);
        for (int ii = 0; ii < 8; ii++) {
            if (hash[ii] != buf.get()) {
                log.warning("Datagram failed hash check", "id", _connectionId, "source", source);
                return;
            }
        }

        // update our target address
        _datagramAddress = source;
        _datagramChannel = channel;

        // read the contents through the sequencer
        try {
            Message msg = _sequencer.readDatagram();
            if (msg == null) {
                return; // received out of order
            }
            msg.received = when;
            _handler.handleMessage(msg);

        } catch (ClassNotFoundException cnfe) {
            log.warning("Error reading datagram", "error", cnfe);

        } catch (IOException ioe) {
            log.warning("Error reading datagram", "error", ioe);
        }
    }

    public int handleEvent (long when)
    {
        // make a note that we received an event as of this time
        _lastEvent = when;

        int bytesIn = 0;
        try {
            // we're lazy about creating our input streams because we may be inheriting them from
            // our authing connection and we don't want to unnecessarily create them in that case
            if (_fin == null) {
                _oin = createObjectInputStream(_fin = new FramedInputStream());
                if (_loader != null) {
                    _oin.setClassLoader(_loader);
                }
            }

            // there may be more than one frame in the buffer, so we keep reading them until we run
            // out of data
            while (_fin.readFrame(_channel)) {
                // make a note of how many bytes are in this frame (including the frame length
                // bytes which aren't reported in available())
                bytesIn = _fin.available() + 4;
                // parse the message and pass it on
                Message msg = (Message)_oin.readObject();
                msg.received = when;
//                 Log.info("Read message " + msg + ".");
                _handler.handleMessage(msg);
            }

        } catch (EOFException eofe) {
            // close down the socket gracefully
            close();

        } catch (ClassNotFoundException cnfe) {
            log.warning("Error reading message from socket", "channel", _channel, "error", cnfe);
            // deal with the failure
            String errmsg = "Unable to decode incoming message.";
            networkFailure((IOException) new IOException(errmsg).initCause(cnfe));

        } catch (IOException ioe) {
            // don't log a warning for the ever-popular "the client dropped the connection" failure
            String msg = ioe.getMessage();
            if (msg == null || msg.indexOf("reset by peer") == -1) {
                log.warning("Error reading message from socket", "channel", _channel, ioe);
            }
            // deal with the failure
            networkFailure(ioe);
        }

        return bytesIn;
    }

    /**
     * Returns the object input stream associated with this connection.  This should only be used
     * by the connection manager.
     */
    protected ObjectInputStream getObjectInputStream ()
    {
        return _oin;
    }

    /**
     * Creates the object input stream used by this connection to communicate. This may be
     * overridden by subclasses to create custom streams.
     */
    protected ObjectInputStream createObjectInputStream (InputStream src)
    {
        return new ObjectInputStream(src);
    }

    /**
     * Instructs this connection to inherit its streams from the supplied connection object. This
     * is called by the connection manager when the time comes to pass streams from the authing
     * connection to the running connection.
     */
    protected void inheritStreams (PresentsConnection other)
    {
        _fin = other._fin;
        _oin = other._oin;
        _oout = other._oout;
        if (_loader != null) {
            _oin.setClassLoader(_loader);
        }
    }

    /**
     * Returns the object output stream associated with this connection (creating it if
     * necessary). This should only be used by the connection manager.
     */
    protected ObjectOutputStream getObjectOutputStream (FramingOutputStream fout)
    {
        // we're lazy about creating our output stream because we may be inheriting it from our
        // authing connection and we don't want to unnecessarily create it in that case
        if (_oout == null) {
            _oout = new ObjectOutputStream(fout);
        }
        return _oout;
    }

    /**
     * Sets the object output stream used by this connection. This should only be called by the
     * connection manager.
     */
    protected void setObjectOutputStream (ObjectOutputStream oout)
    {
        _oout = oout;
    }

    /**
     * Returns a reference to the connection's datagram sequencer.  This should only be called by
     * the connection manager.
     */
    protected DatagramSequencer getDatagramSequencer ()
    {
        return _sequencer;
    }

    protected FramedInputStream _fin;
    protected ObjectInputStream _oin;
    protected ObjectOutputStream _oout;

    protected InetSocketAddress _datagramAddress;
    protected DatagramChannel _datagramChannel;
    protected byte[] _datagramSecret;
    protected boolean _transmitDatagrams;

    protected MessageDigest _digest;
    protected DatagramSequencer _sequencer;

    protected MessageHandler _handler;
    protected ClassLoader _loader;

    protected PresentsConnectionManager _pcmgr;
}
