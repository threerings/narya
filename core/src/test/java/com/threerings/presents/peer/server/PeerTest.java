//
// Narya library - tools for developing networked games
// Copyright (C) 2002-2025 Three Rings Design, Inc., All Rights Reserved
// https://github.com/threerings/narya/blob/master/LICENSE

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
            done.await(1, TimeUnit.SECONDS);
        } catch (InterruptedException e) {
            fail("Interrupte?");
        }

        group.shutdown();
    }
}
