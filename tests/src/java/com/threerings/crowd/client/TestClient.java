//
// $Id: TestClient.java,v 1.4 2001/10/01 22:14:55 mdb Exp $

package com.threerings.cocktail.party.client.test;

import com.samskivert.util.Config;
import com.samskivert.util.Queue;

import com.threerings.cocktail.cher.client.*;
import com.threerings.cocktail.cher.dobj.DObjectManager;
import com.threerings.cocktail.cher.net.*;

import com.threerings.cocktail.party.Log;
import com.threerings.cocktail.party.client.*;
import com.threerings.cocktail.party.util.PartyContext;

public class TestClient
    implements Client.Invoker, ClientObserver
{
    public TestClient (Credentials creds)
    {
        // create our context
        _ctx = new PartyContextImpl();

        // create the handles on our various services
        _config = new Config();
        _client = new Client(creds, this);
        _locdir = new LocationDirector(_ctx);
        _occmgr = new OccupantManager(_ctx);

        // we want to know about logon/logoff
        _client.addObserver(this);

        // for test purposes, hardcode the server info
        _client.setServer("localhost", 4007);
    }

    public void run ()
    {
        // log on
        _client.logon();

        // loop over our queue, running the runnables
        while (true) {
            Runnable run = (Runnable)_queue.get();
            run.run();
        }
    }

    public void invokeLater (Runnable run)
    {
        // queue it on up
        _queue.append(run);
    }

    public void clientDidLogon (Client client)
    {
        Log.info("Client did logon [client=" + client + "].");

        // request to move to a place
        _ctx.getLocationDirector().moveTo(15);
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

    public static void main (String[] args)
    {
        UsernamePasswordCreds creds =
            new UsernamePasswordCreds("test", "test");
        // create our test client
        TestClient tclient = new TestClient(creds);
        // start it running
        tclient.run();
    }

    protected class PartyContextImpl implements PartyContext
    {
        public Config getConfig ()
        {
            return _config;
        }

        public Client getClient ()
        {
            return _client;
        }

        public DObjectManager getDObjectManager ()
        {
            return _client.getDObjectManager();
        }

        public LocationDirector getLocationDirector ()
        {
            return _locdir;
        }

        public OccupantManager getOccupantManager ()
        {
            return _occmgr;
        }
    }

    protected Config _config;
    protected Client _client;
    protected LocationDirector _locdir;
    protected OccupantManager _occmgr;
    protected PartyContext _ctx;

    protected Queue _queue = new Queue();
}
