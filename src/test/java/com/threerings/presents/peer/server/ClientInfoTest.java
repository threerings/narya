//
// $Id$
//
// Narya library - tools for developing networked games
// Copyright (C) 2002-2011 Three Rings Design, Inc., All Rights Reserved
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

package com.threerings.presents.peer.server;

import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

import com.threerings.presents.client.Client;
import com.threerings.presents.client.ClientAdapter;
import com.threerings.presents.peer.data.ClientInfo;
import com.threerings.presents.peer.data.NodeObject;
import com.threerings.presents.peer.server.TestPeerManager.Callback;
import com.threerings.presents.server.ServerTestUtil;

import org.junit.Test;
import static org.junit.Assert.*;

/**
 * Tests that client info is successfully published to all peers after a client logs in.
 */
public class ClientInfoTest
{
    @Test
    public void testPeerConnect ()
        throws Exception
    {
        PeerTestGroup group = new PeerTestGroup(2, true);

        TestPeerManager p1 = group.injectors.get(0).getInstance(TestPeerManager.class);
        TestPeerManager p2 = group.injectors.get(1).getInstance(TestPeerManager.class);

        // wire up callbacks for when we're connected to a peer
        final CountDownLatch init = new CountDownLatch(group.servers.size());
        Callback<NodeObject> onConnected = new Callback<NodeObject>() {
            public void apply (NodeObject nobj) {
                init.countDown();
            }
        };
        p1.setOnConnected(onConnected);
        p2.setOnConnected(onConnected);

        // wire up a callback for when we see that a client logged onto the network (from p2, since
        // the client will talk directly to p1); we do (servers-1) to count the servers that will
        // get a logged on notification and +1 to note that the client needs also to report when it
        // is finished logging on
        final CountDownLatch logonSeen = new CountDownLatch(group.servers.size()-1+1);
        Callback<ClientInfo> onClientLoggedOn = new Callback<ClientInfo>() {
            public void apply (ClientInfo info) {
                logonSeen.countDown();
            }
        };
        p2.setOnClientLoggedOn(onClientLoggedOn);

        // wire up a callback for when we see that a client logged off of the network (from p2,
        // since the client will talk directly to p1)
        final CountDownLatch logoffSeen = new CountDownLatch(group.servers.size()-1);
        Callback<ClientInfo> onClientLoggedOff = new Callback<ClientInfo>() {
            public void apply (ClientInfo info) {
                logoffSeen.countDown();
            }
        };
        p2.setOnClientLoggedOff(onClientLoggedOff);

        // start up all of our servers
        group.start();

        // trigger the immediate establishment of the peer network (normally this is delayed for 5
        // seconds to allow slack for differing peer startup times)
        p1.refreshPeers();

        // wait for the peers to connect to one another
        ServerTestUtil.await(init, 2);

        // connect to a peer with a client
        Client client = ServerTestUtil.createClient("test");
        client.setServer("localhost", new int[] { PeerTestGroup.BASE_PORT });
        client.addClientObserver(new ClientAdapter() {
            public void clientDidLogon (Client client) {
                logonSeen.countDown();
            }
        });
        client.logon();

        // wait for the client to log onto p1 and for p1 to notify p2 of the logon; and wait for
        // the client to finish its own logon fiddling around
        ServerTestUtil.await(logonSeen, 4);

        // now logoff
        client.logoff(false);

        // now wait for the client to log off of p1 and for p1 to notify p2 of the logoff
        ServerTestUtil.await(logoffSeen, 4);

        group.shutdown();
    }
}
