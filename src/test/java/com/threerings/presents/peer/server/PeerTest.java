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

        // wire up callbacks for when we're connected to a peer
        final CountDownLatch done = new CountDownLatch(group.servers.size());
        TestPeerManager p1 = group.injectors.get(0).getInstance(TestPeerManager.class);
        p1.setOnConnected(new TestPeerManager.Callback<String>() {
            public void apply (String nodeName) {
                done.countDown();
            }
        });
        TestPeerManager p2 = group.injectors.get(1).getInstance(TestPeerManager.class);
        p2.setOnConnected(new TestPeerManager.Callback<String>() {
            public void apply (String nodeName) {
                done.countDown();
            }
        });

        // start up all of our servers
        group.start();

        // trigger the immediate establishment of the peer network (normally this is delayed for 5
        // seconds to allow slack for differing peer startup times)
        p1.refreshPeers();

        try {
            if (!done.await(5, TimeUnit.SECONDS)) fail("Timed out");
        } catch (InterruptedException e) {
            fail("Interrupte?");
        }

        group.shutdown();
    }
}
