//
// $Id: TestClient.java,v 1.10 2001/10/11 04:07:52 mdb Exp $

package com.threerings.presents.client.test;

import com.samskivert.util.Queue;

import com.threerings.presents.Log;
import com.threerings.presents.net.*;
import com.threerings.presents.client.*;
import com.threerings.presents.dobj.*;

import com.threerings.presents.server.test.TestObject;

/**
 * A standalone test client.
 */
public class TestClient
    implements Client.Invoker, ClientObserver, Subscriber
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
        // register our test notification receiver
        client.getInvocationDirector().registerReceiver(TestService.MODULE,
                                                        new TestReceiver());
        // get the test object id
        TestService.getTestOid(client, this);
    }

    public void clientFailedToLogon (Client client, Exception cause)
    {
        Log.info("Client failed to logon [client=" + client +
                 ", cause=" + cause + "].");
    }

    public void clientConnectionFailed (Client client, Exception cause)
    {
        Log.info("Client connection failed [client=" + client +
                 ", cause=" + cause + "].");
    }

    public boolean clientWillLogoff (Client client)
    {
        Log.info("Client will logoff [client=" + client + "].");
        return true;
    }

    public void clientDidLogoff (Client client)
    {
        Log.info("Client did logoff [client=" + client + "].");
        System.exit(0);
    }

    public void objectAvailable (DObject object)
    {
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

    public boolean handleEvent (DEvent event, DObject target)
    {
        Log.info("Got event [event=" + event + ", target=" + target + "].");
        if (event instanceof AttributeChangedEvent) {
            // request to destroy the object
            target.destroy();
        } else {
            // request that we log off
            _client.logoff(true);
        }
        return true;
    }

    public void handleTestSucceeded (int invid, String one, int two)
    {
        Log.info("Got test response [one=" + one + ", two=" + two + "].");
    }

    public void handleGotTestOid (int invid, int oid)
    {
        // subscribe to the test object
        _client.getDObjectManager().subscribeToObject(oid, this);
    }

    public static void main (String[] args)
    {
        TestClient tclient = new TestClient();
        UsernamePasswordCreds creds =
            new UsernamePasswordCreds("test", "test");
        Client client = new Client(creds, tclient);
        tclient.setClient(client);
        client.addObserver(tclient);
        client.setServer("localhost", 4007);
        client.logon();
        // start up our event processing loop
        tclient.run();
    }

    protected Queue _queue = new Queue();
    protected Client _client;
}
