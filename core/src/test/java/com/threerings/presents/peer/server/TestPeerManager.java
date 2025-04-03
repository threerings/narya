//
// Narya library - tools for developing networked games
// Copyright (C) 2002-2025 Three Rings Design, Inc., All Rights Reserved
// https://github.com/threerings/narya/blob/master/LICENSE

package com.threerings.presents.peer.server;

import com.google.inject.Inject;
import com.google.inject.Singleton;

import com.samskivert.util.Lifecycle;

/**
 * A peer manager with hooks for testing.
 */
@Singleton
public class TestPeerManager extends PeerManager
{
    public interface Callback<T> {
        void apply (T value);
    }

    @Inject
    public TestPeerManager (Lifecycle cycle) {
        super(cycle);
    }

    public void setOnConnected (Callback<String> onConnected) {
        _onConnected = onConnected;
    }

    @Override
    protected void connectedToPeer (PeerNode peer) {
        super.connectedToPeer(peer);
        if (_onConnected != null) {
            _onConnected.apply(peer.nodeobj.nodeName);
        }
    }

    protected Callback<String> _onConnected;
}
