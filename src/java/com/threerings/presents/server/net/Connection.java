//
// $Id: Connection.java,v 1.8 2002/07/23 05:52:49 mdb Exp $

package com.threerings.presents.server.net;

import java.io.EOFException;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

import ninja2.core.io_core.nbio.*;

import com.samskivert.io.NestableIOException;

import com.threerings.io.FramedInputStream;
import com.threerings.io.FramingOutputStream;
import com.threerings.io.ObjectInputStream;
import com.threerings.io.ObjectOutputStream;

import com.threerings.presents.Log;
import com.threerings.presents.net.DownstreamMessage;
import com.threerings.presents.net.UpstreamMessage;

/**
 * The base connection class implements the net event handler interface
 * and processes raw incoming network data into a stream of parsed
 * <code>UpstreamMessage</code> objects. It also provides the means to
 * send messages to the client and facilities for checking delinquency.
 */
public abstract class Connection implements NetEventHandler
{
    /**
     * Constructs a connection object that is associated with the supplied
     * socket.
     *
     * @param cmgr The connection manager with which this connection is
     * associated.
     * @param socket The socket from which we'll be reading messages.
     * @param fout The framing output stream via which we'll write
     * serialized messages to the client.
     */
    public Connection (ConnectionManager cmgr, NonblockingSocket socket)
        throws IOException
    {
        _cmgr = cmgr;
        _socket = socket;

        // create a select item that will allow us to be incorporated into
        // the main event polling loop
        _selitem = new SelectItem(_socket, this, Selectable.READ_READY);

        // get a handle on our socket streams
        _in = _socket.getInputStream();
        _out = _socket.getOutputStream();
    }

    /**
     * Instructs the connection to pass parsed messages on to this handler
     * for processing. This should be done before the connection is turned
     * loose to process messages.
     */
    public void setMessageHandler (MessageHandler handler)
    {
        _handler = handler;
    }

    /**
     * Returns the select item associated with this connection.
     */
    public SelectItem getSelectItem ()
    {
        return _selitem;
    }

    /**
     * Returns the non-blocking socket object used to construct this
     * connection.
     */
    public NonblockingSocket getSocket ()
    {
        return _socket;
    }

    /**
     * Returns true if this connection is closed.
     */
    public boolean isClosed ()
    {
        return (_socket == null);
    }

    /**
     * Closes this connection and unregisters it from the connection
     * manager. This should only be called from the conmgr thread.
     */
    public void close ()
    {
        // we shouldn't be closed twice
        if (isClosed()) {
            Log.warning("Attempted to re-close connection " + this + ".");
            Thread.dumpStack();
            return;
        }

        // unregister from the select set
        _cmgr.connectionClosed(this);

        // close our socket
        closeSocket();
    }

    /**
     * Called when there is a failure reading or writing on this
     * connection. We notify the connection manager and close ourselves
     * down.
     */
    public void handleFailure (IOException ioe)
    {
        // if we're already closed, then something is seriously funny
        if (isClosed()) {
            Log.warning("Failure reported on closed connection " + this + ".");
            Thread.dumpStack();
            return;
        }

        // let the connection manager know we're hosed
        _cmgr.connectionFailed(this, ioe);

        // and close our socket
        closeSocket();
    }

    /**
     * Returns the output stream associated with this connection. This
     * should only be used by the connection manager.
     */
    protected OutputStream getOutputStream ()
    {
        return _out;
    }

    /**
     * Returns the object input stream associated with this connection.
     * This should only be used by the connection manager.
     */
    protected ObjectInputStream getObjectInputStream ()
    {
        return _oin;
    }

    /**
     * Instructs this connection to inherit its streams from the supplied
     * connection object. This is called by the connection manager when
     * the time comes to pass streams from the authing connection to the
     * running connection.
     */
    protected void inheritStreams (Connection other)
    {
        _fin = other._fin;
        _oin = other._oin;
        _oout = other._oout;
    }

    /**
     * Returns the object output stream associated with this connection
     * (creating it if necessary). This should only be used by the
     * connection manager.
     */
    protected ObjectOutputStream getObjectOutputStream (
        FramingOutputStream fout)
    {
        // we're lazy about creating our output stream because we may be
        // inheriting it from our authing connection and we don't want to
        // unnecessarily create it in that case
        if (_oout == null) {
            _oout = new ObjectOutputStream(fout);
        }
        return _oout;
    }

    /**
     * Sets the object output stream used by this connection. This should
     * obly be called by the connection manager.
     */
    protected void setObjectOutputStream (ObjectOutputStream oout)
    {
        _oout = oout;
    }

    /**
     * Closes the socket associated with this connection. This happens
     * when we receive EOF, are requested to close down or when our
     * connection fails.
     */
    protected void closeSocket ()
    {
        if (_socket != null) {
            try {
                _socket.close();
            } catch (IOException ioe) {
                Log.warning("Error closing connection [conn=" + this +
                            ", error=" + ioe + "].");
            }

            // clear out our socket reference to prevent repeat closings
            _socket = null;
        }
    }

    /**
     * Called when our client socket has data available for reading.
     */
    public void handleEvent (Selectable source, short events)
    {
        try {
            // we're lazy about creating our input streams because we may
            // be inheriting them from our authing connection and we don't
            // want to unnecessarily create them in that case
            if (_fin == null) {
                _fin = new FramedInputStream();
                _oin = new ObjectInputStream(_fin);
            }

            // read the available data and see if we have a whole frame
            if (_fin.readFrame(_in)) {
                // parse the message and pass it on
                UpstreamMessage msg = (UpstreamMessage)_oin.readObject();
//                 Log.info("Read message " + msg + ".");
                _handler.handleMessage(msg);
            }

        } catch (EOFException eofe) {
            // close down the socket gracefully
            close();

        } catch (ClassNotFoundException cnfe) {
            Log.warning("Error reading message from socket " +
                        "[socket=" + _socket + ", error=" + cnfe + "].");
            // deal with the failure
            handleFailure(new NestableIOException(
                              "Unable to decode incoming message.", cnfe));

        } catch (IOException ioe) {
            Log.warning("Error reading message from socket " +
                        "[socket=" + _socket + ", error=" + ioe + "].");
            // deal with the failure
            handleFailure(ioe);
        }
    }

    /**
     * Posts a downstream message for delivery to this connection. The
     * message will be delivered by the conmgr thread as soon as it gets
     * to it.
     */
    public void postMessage (DownstreamMessage msg)
    {
        // pass this along to the connection manager
        _cmgr.postMessage(this, msg);
    }

    protected ConnectionManager _cmgr;
    protected NonblockingSocket _socket;
    protected SelectItem _selitem;

    protected InputStream _in;
    protected FramedInputStream _fin;
    protected ObjectInputStream _oin;

    protected OutputStream _out;
    protected ObjectOutputStream _oout;

    protected MessageHandler _handler;
}
