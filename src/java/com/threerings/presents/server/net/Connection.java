//
// $Id: Connection.java,v 1.3 2001/06/01 22:12:03 mdb Exp $

package com.threerings.cocktail.cher.server.net;

import java.io.*;
import ninja2.core.io_core.nbio.*;

import com.threerings.cocktail.cher.Log;
import com.threerings.cocktail.cher.io.FramedInputStream;
import com.threerings.cocktail.cher.io.TypedObjectFactory;
import com.threerings.cocktail.cher.net.UpstreamMessage;
import com.threerings.cocktail.cher.net.DownstreamMessage;

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
     * Called when our client socket has data available for reading.
     */
    public void handleEvent (Selectable source, short events)
    {
        try {
            // read the available data and see if we have a whole frame
            if (_fin.readFrame(_in)) {
                // parse the message and pass it on
                handleMessage((UpstreamMessage)
                              TypedObjectFactory.readFrom(_din));
            }

        } catch (EOFException eofe) {
            // let the connection manager know that we done went away
            _cmgr.connectionClosed(this);

        } catch (IOException ioe) {
            Log.warning("Error reading message from socket " +
                        "[socket=" + _socket + ", error=" + ioe + "].");
            // let the connection manager know that something when awry
            _cmgr.connectionFailed(this, ioe);
        }
    }

    /**
     * Called when a complete message has been parsed from incoming
     * network data.
     */
    public abstract void handleMessage (UpstreamMessage msg);

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
}
