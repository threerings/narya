//
// $Id: Connection.java,v 1.7 2002/07/10 01:23:45 mdb Exp $

package com.threerings.presents.server.net;

import java.io.*;
import ninja2.core.io_core.nbio.*;

import com.threerings.presents.Log;
import com.threerings.presents.io.FramedInputStream;
import com.threerings.presents.io.TypedObjectFactory;
import com.threerings.presents.net.UpstreamMessage;
import com.threerings.presents.net.DownstreamMessage;

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
     */
    public Connection (ConnectionManager cmgr, NonblockingSocket socket)
        throws IOException
    {
        _cmgr = cmgr;
        _socket = socket;

        // create a select item that will allow us to be incorporated into
        // the main event polling loop
        _selitem = new SelectItem(_socket, this, Selectable.READ_READY);

        // create our input streams
        _in = _socket.getInputStream();
        _out = _socket.getOutputStream();
        _fin = new FramedInputStream();
        _din = new DataInputStream(_fin);
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
     * Returns the output stream associated with this connection. This
     * should only be used by the connection manager.
     */
    public OutputStream getOutputStream ()
    {
        return _out;
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
            // read the available data and see if we have a whole frame
            if (_fin.readFrame(_in)) {
                // parse the message and pass it on
                _handler.handleMessage((UpstreamMessage)
                                       TypedObjectFactory.readFrom(_din));
            }

        } catch (EOFException eofe) {
            // close down the socket gracefully
            close();

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
    protected OutputStream _out;
    protected FramedInputStream _fin;
    protected DataInputStream _din;

    protected MessageHandler _handler;
}
