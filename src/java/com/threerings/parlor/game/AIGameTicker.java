//
// $Id: AIGameTicker.java,v 1.6 2004/08/27 02:20:14 mdb Exp $
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

package com.threerings.parlor.game;

import java.util.Iterator;
import java.util.HashSet;

import com.samskivert.util.IntervalManager;

import com.threerings.presents.server.util.SafeInterval;
import com.threerings.crowd.server.CrowdServer;

/**
 * Handles ticking all the GameManagers that are hosting games with AIs.
 */
public class AIGameTicker extends SafeInterval
{
    /** The frequency with which we dispatch AI game ticks. */
    public static final long TICK_FREQUENCY = 3333L; // every 3 1/3 seconds

    /**
     * Register the specified GameManager to receive AI ticks.
     */
    public static synchronized void registerAIGame (GameManager mgr)
    {
        if (_ticker == null) {
            _ticker = new AIGameTicker();
        }
        _ticker.addAIGame(mgr);
    }

    /**
     * Take the specified GameManager off the AI tick list.
     */
    public static synchronized void unregisterAIGame (GameManager mgr)
    {
        if (_ticker != null) {
            _ticker.removeAIGame(mgr);
        }
    }

    /**
     * Construct an AIGameTicker and start it ticking.
     */
    private AIGameTicker ()
    {
        super(CrowdServer.omgr);
        _games = new HashSet();

        _id = IntervalManager.register(this, TICK_FREQUENCY, null, true);
    }

    /**
     * Add the specified manager to the list of those receiving AI ticks.
     */
    protected void addAIGame (GameManager mgr)
    {
        _games.add(mgr);
    }

    /**
     * Remove the specified manager from receiving AI ticks.
     *
     * @return true if there are no more games.
     */
    protected void removeAIGame (GameManager mgr)
    {
        _games.remove(mgr);

        // if there aren't any AIs, let's stop running
        if (_games.isEmpty()) {
            _ticker = null;
            IntervalManager.remove(_id);
        }
    }

    /**
     * Tick all the game AIs while on the dobj thread.
     */
    public void run ()
    {
        Iterator iter = _games.iterator();
        while (iter.hasNext()) {
            ((GameManager) iter.next()).tickAIs();
        }
    }

    /** Our set of ai games. */
    protected HashSet _games;

    /** Our interval id. */
    protected int _id;

    /** Our single ticker for all AI games. */
    protected static AIGameTicker _ticker;
}
