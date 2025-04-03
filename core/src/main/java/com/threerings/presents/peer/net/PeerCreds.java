//
// Narya library - tools for developing networked games
// Copyright (C) 2002-2025 Three Rings Design, Inc., All Rights Reserved
// https://github.com/threerings/narya/blob/master/LICENSE

package com.threerings.presents.peer.net;

import com.threerings.presents.net.ServiceCreds;

/**
 * Used by peer servers in a cluster installation to authenticate with one another.
 */
public class PeerCreds extends ServiceCreds
{
    public PeerCreds (String nodeName, String sharedSecret)
    {
        super(nodeName, sharedSecret);
    }

    /**
     * Used when unserializing an instance from the network.
     */
    public PeerCreds ()
    {
    }
}
