//
// $Id: TestClient.java,v 1.4 2001/06/13 05:17:55 mdb Exp $

package com.threerings.cocktail.cher.client.test;

import com.samskivert.util.Queue;

import com.threerings.cocktail.cher.Log;
import com.threerings.cocktail.cher.net.*;
import com.threerings.cocktail.cher.client.*;
import com.threerings.cocktail.cher.dobj.*;

import com.threerings.cocktail.cher.server.test.TestObject;

/**
 * A standalone test client.
 */
public class TestClient
    implements Client.Invoker, ClientObserver, Subscriber
{
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
        // try subscribing to a test object
        client.getDObjectManager().subscribeToObject(1, this);
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
    }

    public boolean handleEvent (DEvent event, DObject target)
    {
        Log.info("Got event [event=" + event + ", target=" + target + "].");
        // dispatch a second event
        ((TestObject)target).setBar("rofl!");
        // unsubscribe to the object to make sure we don't get the event
        return false;
    }

    public static void main (String[] args)
    {
        TestClient tclient = new TestClient();
        UsernamePasswordCreds creds =
            new UsernamePasswordCreds("test", "test");
        Client client = new Client(creds, tclient);
        client.addObserver(tclient);
        client.setServer("localhost", 4007);
        client.logon();
        // start up our event processing loop
        tclient.run();
    }

    protected Queue _queue = new Queue();
}
