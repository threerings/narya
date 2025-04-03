//
// Narya library - tools for developing networked games
// Copyright (C) 2002-2025 Three Rings Design, Inc., All Rights Reserved
// https://github.com/threerings/narya/blob/master/LICENSE

package com.threerings.presents.peer.net;

import com.threerings.presents.net.BootstrapData;

/**
 * Extensd the standard bootstrap with some information needed by our peers.
 */
public class PeerBootstrapData extends BootstrapData
{
    /** The id of this peer's node object. */
    public int nodeOid;
}
