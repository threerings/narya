//
// $Id: TestClient.java,v 1.1 2001/08/14 06:51:07 mdb Exp $

package com.threerings.whirled.client.test;

import com.samskivert.util.Config;
import com.samskivert.util.Queue;

import com.threerings.cocktail.cher.client.*;
import com.threerings.cocktail.cher.dobj.DObjectManager;
import com.threerings.cocktail.cher.net.*;

import com.threerings.cocktail.party.Log;
import com.threerings.cocktail.party.client.LocationManager;
import com.threerings.cocktail.party.client.LocationObserver;
import com.threerings.cocktail.party.data.PlaceObject;

import com.threerings.whirled.client.SceneManager;
import com.threerings.whirled.client.persist.SceneRepository;
import com.threerings.whirled.util.WhirledContext;

public class TestClient
    implements Client.Invoker, ClientObserver, LocationObserver
{
    public TestClient (Credentials creds)
    {
        // create our context
        _ctx = new WhirledContextImpl();

        // create the handles for our various services
        _config = new Config();
        _client = new Client(creds, this);
        _screp = new DummySceneRepository();
        _scmgr = new SceneManager(_ctx, _screp);

        // we want to know about logon/logoff
        _client.addObserver(this);

        // we want to know about location changes
        _scmgr.addLocationObserver(this);

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

        // request to move to scene 0
        _ctx.getSceneManager().moveTo(0);
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

    public boolean locationMayChange (int placeId)
    {
        // we're easy
        return true;
    }

    public void locationDidChange (PlaceObject place)
    {
        Log.info("At new location [plobj=" + place +
                 ", scene=" + _scmgr.getScene() + "].");
    }

    public void locationChangeFailed (int placeId, String reason)
    {
        Log.warning("Location change failed [plid=" + placeId +
                    ", reason=" + reason + "].");
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

    protected class WhirledContextImpl implements WhirledContext
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

        public LocationManager getLocationManager ()
        {
            return _scmgr;
        }

        public SceneManager getSceneManager ()
        {
            return _scmgr;
        }
    }

    protected Config _config;
    protected Client _client;
    protected SceneManager _scmgr;
    protected SceneRepository _screp;
    protected WhirledContext _ctx;

    protected Queue _queue = new Queue();
}
