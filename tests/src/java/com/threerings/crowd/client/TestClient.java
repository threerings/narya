//
// $Id: TestClient.java,v 1.14 2004/08/27 02:20:55 mdb Exp $
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

package com.threerings.crowd.client;

import com.samskivert.util.Config;
import com.samskivert.util.Queue;

import com.threerings.presents.client.*;
import com.threerings.presents.dobj.DObjectManager;
import com.threerings.presents.net.*;

import com.threerings.crowd.Log;
import com.threerings.crowd.util.CrowdContext;

public class TestClient
    implements Client.Invoker, ClientObserver
{
    public TestClient (Credentials creds)
    {
        // create our context
        _ctx = new CrowdContextImpl();

        // create the handles on our various services
        _client = new Client(creds, this);
        _locdir = new LocationDirector(_ctx);
        _occdir = new OccupantDirector(_ctx);

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

        // request to move to a place
        _ctx.getLocationDirector().moveTo(15);
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

    public static void main (String[] args)
    {
        UsernamePasswordCreds creds =
            new UsernamePasswordCreds("test", "test");
        // create our test client
        TestClient tclient = new TestClient(creds);
        // start it running
        tclient.run();
    }

    protected class CrowdContextImpl implements CrowdContext
    {
        public Config getConfig ()
        {
            return null;
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
    }

    protected Client _client;
    protected LocationDirector _locdir;
    protected OccupantDirector _occdir;
    protected CrowdContext _ctx;

    protected Queue _queue = new Queue();
}
