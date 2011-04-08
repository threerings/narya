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

import com.threerings.presents.peer.data.NodeObject;
import com.threerings.presents.peer.server.TestPeerManager.Callback;
import com.threerings.presents.server.ServerTestUtil;

import org.junit.Test;
import static org.junit.Assert.*;

/**
 * Tests basic peer network setup and peer communication.
 */
public class PeerTest
{
    @Test
    public void testPeerConnect ()
        throws Exception
    {
        PeerTestGroup group = new PeerTestGroup(2, true);

        TestPeerManager p1 = group.injectors.get(0).getInstance(TestPeerManager.class);
        TestPeerManager p2 = group.injectors.get(1).getInstance(TestPeerManager.class);

        // wire up callbacks for when we're connected to a peer
        final CountDownLatch done = new CountDownLatch(group.servers.size());
        Callback<NodeObject> onConnected = new Callback<NodeObject>() {
            public void apply (NodeObject nobj) {
                done.countDown();
            }
        };
        p1.setOnConnected(onConnected);
        p2.setOnConnected(onConnected);

        // start up all of our servers
        group.start();

        // trigger the immediate establishment of the peer network (normally this is delayed for 5
        // seconds to allow slack for differing peer startup times)
        p1.refreshPeers();

        // wait for the peers to connect to one another
        ServerTestUtil.await(done, 5);

        group.shutdown();
    }
}
