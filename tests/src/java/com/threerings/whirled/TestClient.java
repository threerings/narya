//
// $Id: TestClient.java,v 1.14 2002/11/12 22:55:15 shaper Exp $

package com.threerings.whirled;

import com.samskivert.util.Queue;

import com.threerings.presents.client.*;
import com.threerings.presents.dobj.DObjectManager;
import com.threerings.presents.net.*;

import com.threerings.crowd.Log;
import com.threerings.crowd.client.*;
import com.threerings.crowd.data.PlaceObject;

import com.threerings.whirled.client.SceneDirector;
import com.threerings.whirled.client.DefaultDisplaySceneFactory;
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
        _client = new Client(creds, this);
        _screp = new DummyClientSceneRepository();
        _occdir = new OccupantDirector(_ctx);
        _locdir = new LocationDirector(_ctx);
        _scdir = new SceneDirector(
            _ctx, _locdir, _screp, new DefaultDisplaySceneFactory());

        // we want to know about logon/logoff
        _client.addClientObserver(this);

        // we want to know about location changes
        _locdir.addLocationObserver(this);

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
        _ctx.getSceneDirector().moveTo(0);
    }

    public void clientObjectDidChange (Client client)
    {
        Log.info("Client object did change [client=" + client + "].");
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
                 ", scene=" + _scdir.getScene() + "].");
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

        public OccupantDirector getOccupantDirector ()
        {
            return _occdir;
        }

        public void setPlaceView (PlaceView view)
        {
            // nothing to do because we don't create views
        }

        public void clearPlaceView (PlaceView view)
        {
            // nothing to do because we don't create views
        }

        public SceneDirector getSceneDirector ()
        {
            return _scdir;
        }
    }

    protected Client _client;
    protected LocationDirector _locdir;
    protected SceneDirector _scdir;
    protected OccupantDirector _occdir;
    protected SceneRepository _screp;
    protected WhirledContext _ctx;

    protected Queue _queue = new Queue();
}
