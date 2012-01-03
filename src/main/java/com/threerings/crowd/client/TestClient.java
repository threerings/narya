//
// $Id$
//
// Narya library - tools for developing networked games
// Copyright (C) 2002-2012 Three Rings Design, Inc., All Rights Reserved
// http://code.google.com/p/narya/
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

import com.samskivert.util.BasicRunQueue;
import com.samskivert.util.Config;

import com.threerings.util.MessageManager;
import com.threerings.util.Name;

import com.threerings.presents.client.Client;
import com.threerings.presents.client.ClientObserver;
import com.threerings.presents.dobj.DObjectManager;
import com.threerings.presents.net.UsernamePasswordCreds;

import com.threerings.crowd.chat.client.ChatDirector;
import com.threerings.crowd.util.CrowdContext;

import static com.threerings.crowd.Log.log;

/**
 * A client that is useful when doing unit testing.
 */
public class TestClient
    implements ClientObserver
{
    public TestClient (String username)
    {
        // create our context
        _ctx = createContext();

        // create the handles on our various services
        _client = new Client(new UsernamePasswordCreds(new Name(username), "test"), _rqueue);
        _locdir = new LocationDirector(_ctx);
        _occdir = new OccupantDirector(_ctx);
        _msgmgr = new MessageManager("rsrc");
        _chatdir = new ChatDirector(_ctx, "global");

        // we want to know about logon/logoff
        _client.addClientObserver(this);

        // for test purposes, hardcode the server info
        _client.setServer("localhost", Client.DEFAULT_SERVER_PORTS);
    }

    public void run ()
    {
        // log on
        _client.logon();

        // loop over our queue, running the runnables
        _rqueue.run();
    }

    public void clientWillLogon (Client client)
    {
    }

    public void clientDidLogon (Client client)
    {
        log.info("Client did logon [client=" + client + "].");

        // request to move to a place
        _ctx.getLocationDirector().moveTo(15);
    }

    public void clientObjectDidChange (Client client)
    {
        log.info("Client object did change [client=" + client + "].");
    }

    public void clientFailedToLogon (Client client, Exception cause)
    {
        log.info("Client failed to logon [client=" + client +
                 ", cause=" + cause + "].");
    }

    public void clientConnectionFailed (Client client, Exception cause)
    {
        log.info("Client connection failed [client=" + client +
                 ", cause=" + cause + "].");
    }

    public boolean clientWillLogoff (Client client)
    {
        log.info("Client will logoff [client=" + client + "].");
        return true;
    }

    public void clientDidLogoff (Client client)
    {
        log.info("Client did logoff [client=" + client + "].");
        System.exit(0);
    }

    public void clientDidClear (Client client)
    {
    }

    protected CrowdContext createContext ()
    {
        return new CrowdContextImpl();
    }

    public static void main (String[] args)
    {
        // create our test client
        TestClient tclient = new TestClient("test");
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

        public ChatDirector getChatDirector ()
        {
            return _chatdir;
        }

        public MessageManager getMessageManager ()
        {
            return _msgmgr;
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
    protected CrowdContext _ctx;

    protected LocationDirector _locdir;
    protected OccupantDirector _occdir;
    protected MessageManager _msgmgr;
    protected ChatDirector _chatdir;

    protected BasicRunQueue _rqueue = new BasicRunQueue();
}
