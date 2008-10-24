//
// $Id$

package com.threerings.crowd.chat.client {

import com.threerings.util.Name;

/**
 * An interface that can be registered with the MuteDirector to receive
 * notifications to the mutelist.
 */
public interface MuteObserver
{
    /**
     * The specified player was added or removed from the mutelist.
     */
    function muteChanged (playername :Name, nowMuted :Boolean) :void;
}
}
