//
// $Id: MuteDirector.java,v 1.3 2002/10/25 20:32:41 ray Exp $

package com.threerings.crowd.chat;

import java.util.ArrayList;
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
     * An interface that can be registered with the MuteDirector to
     * receive notifications to the mutelist.
     */
    public static interface MuteObserver
    {
        /**
         * The specified player was added or removed from the mutelist.
         */
        public void muteChanged (String playername, boolean nowMuted);
    }

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
     * Add the specified mutelist observer.
     */
    public void addMuteObserver (MuteObserver obs)
    {
        if (!_observers.contains(obs)) {
            _observers.add(obs);
        }
    }

    /**
     * Remove the specified mutelist observer.
     */
    public void removeMuteObserver (MuteObserver obs)
    {
        _observers.remove(obs);
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
        if (mute ? _mutelist.add(username) : _mutelist.remove(username)) {
            notifyObservers(username, mute);
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

    /**
     * Notify our observers of a change in the mutelist.
     */
    protected void notifyObservers (String username, boolean muted)
    {
        for (int ii=0, nn=_observers.size(); ii < nn; ii++) {
            ((MuteObserver) _observers.get(ii)).muteChanged(username, muted);
        }
    }

    /** The chat director that we're working hard for. */
    protected ChatDirector _chatdir;

    /** The mutelist. */
    protected HashSet _mutelist = new HashSet();

    /** List of mutelist observers. */
    protected ArrayList _observers = new ArrayList();
}
