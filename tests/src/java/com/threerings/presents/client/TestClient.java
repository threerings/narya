//
// $Id$
//
// Narya library - tools for developing networked games
// Copyright (C) 2002-2004 Three Rings Design, Inc., All Rights Reserved
// http://www.threerings.net/code/narya/
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

import com.samskivert.util.Queue;
import com.samskivert.util.RunQueue;

import com.threerings.util.Name;

import com.threerings.presents.Log;
import com.threerings.presents.data.TestObject;
import com.threerings.presents.dobj.*;
import com.threerings.presents.net.*;

/**
 * A standalone test client.
 */
public class TestClient
    implements RunQueue, SessionObserver, Subscriber, EventListener,
               TestService.TestFuncListener, TestService.TestOidListener,
               TestReceiver
{
    public void setClient (Client client)
    {
        _client = client;
    }

    public void postRunnable (Runnable run)
    {
        // queue it on up
        _queue.append(run);
    }

    public boolean isDispatchThread ()
    {
        return _main == Thread.currentThread();
    }

    public void run ()
    {
        _main = Thread.currentThread();

        // loop over our queue, running the runnables
        while (true) {
            Runnable run = (Runnable)_queue.get();
            run.run();
        }
    }

    public void clientDidLogon (Client client)
    {
        Log.info("Client did logon [client=" + client + "].");

        // register ourselves as a test notification receiver
        client.getInvocationDirector().registerReceiver(
            new TestDecoder(this));

        // get the test object id
        TestService service = (TestService)
            client.requireService(TestService.class);
        service.getTestOid(client, this);
    }

    public void clientObjectDidChange (Client client)
    {
        Log.info("Client object did change [client=" + client + "].");
    }

    public void clientDidLogoff (Client client)
    {
        Log.info("Client did logoff [client=" + client + "].");
        System.exit(0);
    }

    public void objectAvailable (DObject object)
    {
        object.addListener(this);
        Log.info("Object available: " + object);
        ((TestObject)object).setBar("lawl!");
    }

    public void requestFailed (int oid, ObjectAccessException cause)
    {
        Log.info("Object unavailable [oid=" + oid +
                 ", reason=" + cause + "].");
        // nothing to do, so might as well logoff
        _client.logoff(true);
    }

    public void eventReceived (DEvent event)
    {
        Log.info("Got event [event=" + event + "].");

        if (event instanceof AttributeChangedEvent) {
            // request to destroy the object
            _client.getDObjectManager().destroyObject(event.getTargetOid());

        } else {
            // request that we log off
            _client.logoff(true);
        }
    }

    // documentation inherited from interface
    public void testSucceeded (String one, int two)
    {
        Log.info("Got test response [one=" + one + ", two=" + two + "].");
    }

    // documentation inherited from interface
    public void gotTestOid (int testOid)
    {
        // subscribe to the test object
        _client.getDObjectManager().subscribeToObject(testOid, this);
    }

    // documentation inherited from interface
    public void requestFailed (String reason)
    {
        Log.info("Urk! Request failed [reason=" + reason + "].");
    }

    // documentation inherited from interface
    public void receivedTest (int one, String two)
    {
        Log.info("Received test notification [one=" + one +
                 ", two=" + two + "].");
    }

    public static void main (String[] args)
    {
        TestClient tclient = new TestClient();
        UsernamePasswordCreds creds =
            new UsernamePasswordCreds(new Name("test"), "test");
        Client client = new Client(creds, tclient);
        tclient.setClient(client);
        client.addClientObserver(tclient);
        client.setServer("localhost", Client.DEFAULT_SERVER_PORT);
        client.logon();
        // start up our event processing loop
        tclient.run();
    }

    protected Thread _main;
    protected Queue _queue = new Queue();
    protected Client _client;
}
