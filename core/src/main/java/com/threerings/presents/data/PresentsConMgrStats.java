//
// Narya library - tools for developing networked games
// Copyright (C) 2002-2025 Three Rings Design, Inc., All Rights Reserved
// https://github.com/threerings/narya/blob/master/LICENSE

package com.threerings.presents.data;

import com.threerings.io.Streamable;
import com.threerings.nio.conman.ConMgrStats;

/**
 * Used to track and report stats on the connection manager.
 */
public class PresentsConMgrStats extends ConMgrStats
    implements Streamable
{
    /** The size of the queue of waiting to auth sockets. This is a snapshot at the time the stats
     * are requested. */
    public int authQueueSize;

    @Override // from Object
    public PresentsConMgrStats clone ()
    {
        return (PresentsConMgrStats)super.clone();
    }
}
