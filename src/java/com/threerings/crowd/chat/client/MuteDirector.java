//
// $Id: MuteDirector.java,v 1.2 2002/07/27 01:58:57 ray Exp $

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
     * Set the required ChatDirector.
     */
    public void setChatDirector (ChatDirector chatdir)
    {
        if (_chatdir == null) {
            _chatdir = chatdir;
            _chatdir.addChatValidator(this);
            _chatdir.setMuteDirector(this);
        }
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
            _chatdir.displayFeedbackMessage("m.no_tell_mute");
            return false;
        }

        return true; // let it go through..
    }

    /** The chat director that we're working hard for. */
    protected ChatDirector _chatdir;

    /** The mutelist. */
    protected HashSet _mutelist = new HashSet();
}
