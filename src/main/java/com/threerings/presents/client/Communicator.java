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

package com.threerings.presents.client;

import com.samskivert.util.RunAnywhere;

import com.threerings.presents.net.AuthResponse;
import com.threerings.presents.net.AuthResponseData;
import com.threerings.presents.net.Message;
import com.threerings.presents.net.UpstreamMessage;

import static com.threerings.presents.Log.log;

/**
 * Handles sending and receiving messages for the client.
 */
public abstract class Communicator
{
    /**
     * Creates a new communicator instance which is associated with the supplied client.
     */
    public Communicator (Client client)
    {
        _client = client;
    }

    /**
     * Logs on to the server and initiates our full-duplex message exchange.
     */
    public abstract void logon ();

    /**
     * Delivers a logoff notification to the server and shuts down the network connection. Also
     * causes all communication threads to terminate.
     */
    public abstract void logoff ();

    /**
     * Notifies the communicator that the client has received its bootstrap data.
     */
    public abstract void gotBootstrap ();

    /**
     * Queues up the specified message for delivery upstream.
     */
    public abstract void postMessage (UpstreamMessage msg);

    /**
     * Configures this communicator with a custom class loader to be used when reading and writing
     * objects over the network.
     */
    public abstract void setClassLoader (ClassLoader loader);

    /**
     * Returns the time at which we last sent a packet to the server.
     */
    public long getLastWrite ()
    {
        return _lastWrite;
    }

    /**
     * Checks whether we should transmit datagrams.
     */
    public boolean getTransmitDatagrams ()
    {
        return false;
    }

    /**
     * Makes a note of the time at which we last communicated with the server.
     */
    protected synchronized void updateWriteStamp ()
    {
        _lastWrite = RunAnywhere.currentTimeMillis();
    }

    /**
     * Subclasses must call this method when they receive the authentication response.
     */
    protected void gotAuthResponse (AuthResponse rsp)
        throws LogonException
    {
        AuthResponseData data = rsp.getData();
        if (!data.code.equals(AuthResponseData.SUCCESS)) {
            throw new LogonException(data.code);
        }
        logonSucceeded(data);
    }

    /**
     * Called when the authentication process completes successfully. Derived classes can override
     * this method and complete any initialization that need wait for authentication success.
     */
    protected synchronized void logonSucceeded (AuthResponseData data)
    {
        // create our distributed object manager
        _omgr = new ClientDObjectMgr(this, _client);

        // fill the auth data into the client's local field so that it can be requested by external
        // entities
        _client._authData = data;

        // wait for the bootstrap notification before we claim that we're actually logged on
    }

    /**
     * Callback called by the reader thread when it has parsed a new message from the socket and
     * wishes to have it processed.
     */
    protected void processMessage (Message msg)
    {
        // post this message to the dobjmgr queue
        _omgr.processMessage(msg);
    }

    protected void notifyClientObservers (ObserverOps.Session op)
    {
        if (_client != null) {
            _client.notifyObservers(op);
        } else {
            log.warning("Dropping client observer notification.",  "op", op);
        }
    }

    protected void clientCleanup (Exception logonError)
    {
        if (_client != null) {
            _client.cleanup(logonError);
            _client = null; // prevent any post-cleanup tomfoolery
        }
    }

    protected Client _client;
    protected ClientDObjectMgr _omgr;
    protected long _lastWrite;
}
