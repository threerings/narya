//
// $Id$
//
// Narya library - tools for developing networked games
// Copyright (C) 2002-2010 Three Rings Design, Inc., All Rights Reserved
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

import java.util.ArrayList;

import com.samskivert.util.BasicRunQueue;
import com.threerings.util.Name;

import com.threerings.presents.data.TestObject;
import com.threerings.presents.dobj.DEvent;
import com.threerings.presents.dobj.EventListener;
import com.threerings.presents.dobj.MessageEvent;
import com.threerings.presents.dobj.ObjectAccessException;
import com.threerings.presents.dobj.Subscriber;
import com.threerings.presents.net.UsernamePasswordCreds;

import static com.threerings.presents.Log.log;

/**
 * A standalone test client.
 */
public class TestClient
    implements SessionObserver, Subscriber<TestObject>, EventListener,
               TestService.TestOidListener, TestReceiver
{
    public void setClient (Client client)
    {
        _client = client;
    }

    // from interface SessionObserver
    public void clientWillLogon (Client client)
    {
        client.addServiceGroup("test");
    }

    // from interface SessionObserver
    public void clientDidLogon (Client client)
    {
        log.info("Client did logon [client=" + client + "].");

        // register ourselves as a test notification receiver
        client.getInvocationDirector().registerReceiver(new TestDecoder(this));

        TestService service = client.requireService(TestService.class);

        // send a test request
        ArrayList<Integer> three = new ArrayList<Integer>();
        three.add(3);
        three.add(4);
        three.add(5);
        service.test("one", 2, three, new TestService.TestFuncListener() {
            public void testSucceeded (String one, int two) {
                log.info("Got test response [one=" + one + ", two=" + two + "].");
            }
            public void requestFailed (String reason) {
                log.info("Urk! Request failed [reason=" + reason + "].");
            }
        });

        // get the test object id
        service.getTestOid(this);
    }

    // from interface SessionObserver
    public void clientObjectDidChange (Client client)
    {
        log.info("Client object did change [client=" + client + "].");
    }

    // from interface SessionObserver
    public void clientDidLogoff (Client client)
    {
        log.info("Client did logoff [client=" + client + "].");
        System.exit(0);
    }

    // from interface Subscriber
    public void objectAvailable (final TestObject object)
    {
        object.addListener(this);
        log.info("Object available: " + object);
        object.postMessage("lawl!");

        // try blowing through our message limit
        for (int tt = 0; tt < 15; tt++) {
            log.info("Go speed messages, go! " + tt);
            for (int ii = 0; ii < 2*Client.DEFAULT_MSGS_PER_SECOND; ii++) {
                object.postMessage("ZOMG!", new Integer(ii));
            }
            try {
                Thread.sleep(1000L);
            } catch (Exception e) {
            }
        }

        // ask for the power
        TestService service = _client.requireService(TestService.class);
        service.giveMeThePower(new TestService.ConfirmListener() {
            public void requestProcessed () {
                log.info("We have the power!");
                // now try blowing through our message limit again
                for (int ii = 0; ii < 8*Client.DEFAULT_MSGS_PER_SECOND; ii++) {
                    object.postMessage("ZOMG!", new Integer(ii));
                }
                // and finally shutdown
                object.postMessage("shutdown");
            }
            public void requestFailed (String cause) {
                log.warning("Dang, no power! " + cause);
            }
        });
    }

    // from interface Subscriber
    public void requestFailed (int oid, ObjectAccessException cause)
    {
        log.info("Object unavailable [oid=" + oid +
                 ", reason=" + cause + "].");
        // nothing to do, so might as well logoff
        _client.logoff(true);
    }

    // from interface EventListener
    public void eventReceived (DEvent event)
    {
        log.info("Got event [event=" + event + "].");

        if (event instanceof MessageEvent && ((MessageEvent)event).getName().equals("shutdown")) {
            // request that we log off
            _client.logoff(true);
        }
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
        log.info("Urk! Request failed [reason=" + reason + "].");
    }

    // documentation inherited from interface
    public void receivedTest (int one, String two)
    {
        log.info("Received test notification [one=" + one +
                 ", two=" + two + "].");
    }

    public static void main (String[] args)
    {
        TestClient tclient = new TestClient();
        UsernamePasswordCreds creds =
            new UsernamePasswordCreds(new Name("test"), "test");
        BasicRunQueue rqueue = new BasicRunQueue();
        Client client = new Client(creds, rqueue);
        tclient.setClient(client);
        client.addClientObserver(tclient);
        client.setServer("localhost", Client.DEFAULT_SERVER_PORTS);
        client.logon();
        // start up our event processing loop
        rqueue.run();
    }

    protected Client _client;
}
