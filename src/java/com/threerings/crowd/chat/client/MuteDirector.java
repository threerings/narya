//
// $Id: MuteDirector.java,v 1.1 2002/06/28 04:09:39 ray Exp $

package com.threerings.crowd.chat;

import java.util.HashSet;

import com.threerings.crowd.util.CrowdContext;

/**
 * Manages the mutelist.
 *
 * TODO: This class right now is pretty much just a placeholder.
 */
public class MuteDirector
    implements ChatValidator
{
    /**
     * Should be instantiated after the ChatDirector.
     */
    public MuteDirector (CrowdContext ctx)
    {
        // nothing to initialize right now
    }

    /**
     * Check to see if the specified user is muted.
     */
    public boolean isMuted (String username)
    {
        return _mutelist.contains(username);
    }

    /**
     * Mute or unmute the specified user.
     */
    public void setMuted (String username, boolean mute)
    {
        if (mute) {
            _mutelist.add(username);
        } else {
            _mutelist.remove(username);
        }
    }

    // documentation inherited from interface ChatValidator
    public boolean validateSpeak (String msg)
    {
        return true; // sure!
    }

    // documentation inherited from interface ChatValidator
    public boolean validateTell (String target, String msg)
    {
        if (isMuted(target)) {
            // TODO: give user feedback on why the TELL is cancelled
            return false;
        }

        return true; // let it go through..
    }

    /** The mutelist. */
    protected HashSet _mutelist = new HashSet();
}
