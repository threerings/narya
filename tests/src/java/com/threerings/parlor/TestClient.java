//
// $Id: TestClient.java,v 1.9 2002/08/14 19:07:59 mdb Exp $

package com.threerings.parlor;

import com.samskivert.util.Queue;

import com.threerings.presents.client.*;
import com.threerings.presents.dobj.DObjectManager;
import com.threerings.presents.net.*;

import com.threerings.crowd.data.BodyObject;
import com.threerings.crowd.client.*;

import com.threerings.parlor.Log;
import com.threerings.parlor.client.*;
import com.threerings.parlor.game.*;
import com.threerings.parlor.util.ParlorContext;

public class TestClient
    implements Client.Invoker, ClientObserver, InvitationHandler,
               InvitationResponseObserver
{
    public TestClient (Credentials creds)
    {
        // create our context
        _ctx = new ParlorContextImpl();

        // create the handles on our various services
        _client = new Client(creds, this);
        _locdir = new LocationDirector(_ctx);
        _occdir = new OccupantDirector(_ctx);
        _pardtr = new ParlorDirector(_ctx);

        // register ourselves as the invitation handler
        _pardtr.setInvitationHandler(this);

        // we want to know about logon/logoff
        _client.addClientObserver(this);

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

        // get a casted reference to our body object
        _body = (BodyObject)client.getClientObject();

        // if we're the inviter, do some inviting
        if (_body.username.equals("inviter")) {
            // send the invitation
            TestConfig config = new TestConfig();
            _pardtr.invite("invitee", config, this);
        }
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

    public void invitationReceived (Invitation invite)
    {
        Log.info("Invitation received [invite=" + invite + "].");

        // accept the invitation. we're game...
        invite.accept();
    }

    public void invitationCancelled (Invitation invite)
    {
        Log.info("Invitation cancelled [invite=" + invite + "].");
    }

    public void invitationAccepted (Invitation invite)
    {
        Log.info("Invitation accepted [invite=" + invite + "].");
    }

    public void invitationRefused (Invitation invite, String message)
    {
        Log.info("Invitation refused [invite=" + invite +
                 ", message=" + message + "].");
    }

    public void invitationCountered (Invitation invite, GameConfig config)
    {
        Log.info("Invitation countered [invite=" + invite +
                 ", config=" + config + "].");
    }

    public static void main (String[] args)
    {
        String username = System.getProperty("username", "test");
        UsernamePasswordCreds creds =
            new UsernamePasswordCreds(username, "test");
        // create our test client
        TestClient tclient = new TestClient(creds);
        // start it running
        tclient.run();
    }

    protected class ParlorContextImpl implements ParlorContext
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

        public ParlorDirector getParlorDirector ()
        {
            return _pardtr;
        }
    }

    protected Client _client;
    protected LocationDirector _locdir;
    protected OccupantDirector _occdir;
    protected ParlorDirector _pardtr;
    protected ParlorContext _ctx;

    protected BodyObject _body;

    protected Queue _queue = new Queue();
}
