//
// $Id: TestClient.java,v 1.14 2002/08/14 19:07:59 mdb Exp $

package com.threerings.presents.client;

import com.samskivert.util.Queue;

import com.threerings.presents.Log;
import com.threerings.presents.dobj.*;
import com.threerings.presents.net.*;
import com.threerings.presents.server.TestObject;

/**
 * A standalone test client.
 */
public class TestClient
    implements Client.Invoker, SessionObserver, Subscriber, EventListener,
               TestService.TestFuncListener, TestService.TestOidListener,
               TestReceiver
{
    public void setClient (Client client)
    {
        _client = client;
    }

    public void invokeLater (Runnable run)
    {
        // queue it on up
        _queue.append(run);
    }

    public void run ()
    {
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
            new UsernamePasswordCreds("test", "test");
        Client client = new Client(creds, tclient);
        tclient.setClient(client);
        client.addClientObserver(tclient);
        client.setServer("localhost", 4007);
        client.logon();
        // start up our event processing loop
        tclient.run();
    }

    protected Queue _queue = new Queue();
    protected Client _client;
}
