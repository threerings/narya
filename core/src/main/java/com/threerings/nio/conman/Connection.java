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

package com.threerings.nio.conman;

import static com.threerings.NaryaLog.log;

import java.io.IOException;
import java.net.InetAddress;
import java.nio.channels.SelectionKey;
import java.nio.channels.SocketChannel;

/**
 * Implements the net event handler interface to check for delinquency and manages a client
 * connection. Subclasses must handle incoming data in {@link #handleEvent}.
 */
public abstract class Connection implements NetEventHandler
{
    /** The key used by the NIO code to track this connection. */
    public SelectionKey selkey;

    /**
     * Initializes a connection object with a socket and related info.
     *
     * @param cmgr The connection manager with which this connection is associated.
     * @param channel The socket channel from which we'll be reading messages.
     * @param createStamp The time at which this connection was created.
     */
    public void init (ConnectionManager cmgr, SocketChannel channel, long createStamp)
        throws IOException
    {
        _cmgr = cmgr;
        _channel = channel;
        _lastEvent = createStamp;
        _connectionId = ++_lastConnectionId;
    }

    /**
     * Returns the connection's unique identifier.
     */
    public int getConnectionId ()
    {
        return _connectionId;
    }

    /**
     * Returns the non-blocking socket object used to construct this connection.
     */
    public SocketChannel getChannel ()
    {
        return _channel;
    }

    /**
     * Returns the address associated with this connection or null if it has no underlying socket
     * channel.
     */
    public InetAddress getInetAddress ()
    {
        return (_channel == null) ? null : _channel.socket().getInetAddress();
    }

    /**
     * Returns true if this connection is closed.
     */
    public boolean isClosed ()
    {
        return (_channel == null);
    }

    /**
     * Closes this connection and unregisters it from the connection manager. This should only be
     * called from the conmgr thread.
     */
    public void close ()
    {
        // we shouldn't be closed twice
        if (isClosed()) {
            log.warning("Attempted to re-close connection " + this + ".", new Exception());
            return;
        }

        // unregister from the select set
        _cmgr.connectionClosed(this);

        // close our socket
        closeSocket();
    }

    /**
     * Queues up a request to have this connection closed by the connection manager once all
     * messages in its queue have been written to its target.
     */
    public void asyncClose ()
    {
        _cmgr.postAsyncClose(this);
    }

    /**
     * Called when an outgoing socket experiences a connect failure. The connection manager will
     * have cleaned up the partial registration needed during the connect process, so we are only
     * responsible for closing our socket.
     */
    public void connectFailure (IOException ioe)
    {
        closeSocket();
    }

    /**
     * Called when there is a failure reading or writing to this connection. We notify the
     * connection manager and close ourselves down.
     */
    public void networkFailure (IOException ioe)
    {
        // if we're already closed, then something is seriously funny
        if (isClosed()) {
            log.warning("Failure reported on closed connection " + this + ".", new Exception());
            return;
        }

        // let the connection manager know we're hosed
        _cmgr.connectionFailed(this, ioe);

        // and close our socket
        closeSocket();
    }

    // from interface NetEventHandler
    public boolean checkIdle (long idleStamp)
    {
        if (_lastEvent > idleStamp) {
            return false;
        }
        if (!isClosed()) {
            log.info("Disconnecting non-communicative client",
                     "conn", this, "idle", (System.currentTimeMillis() - _lastEvent) + "ms");
        }
        return true;
    }

    // from interface NetEventHandler
    public void becameIdle ()
    {
        _cmgr.closeConnection(this);
    }

    @Override // from Object
    public String toString ()
    {
        return "[id=" + _connectionId + ", addr=" + getInetAddress() + "]";
    }

    /**
     * Closes the socket associated with this connection. This happens when we receive EOF, are
     * requested to close down or when our connection fails.
     */
    protected void closeSocket ()
    {
        if (_channel == null) {
            return;
        }

        log.debug("Closing channel " + this + ".");
        try {
            _channel.close();
        } catch (IOException ioe) {
            log.warning("Error closing connection", "conn", this, "error", ioe);
        }

        // clear out our references to prevent repeat closings
        _channel = null;
    }

    protected ConnectionManager _cmgr;
    protected SocketChannel _channel;

    protected long _lastEvent;

    protected int _connectionId;

    /** The last connection id assigned. */
    protected static int _lastConnectionId;
}
