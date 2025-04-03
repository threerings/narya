//
// Narya library - tools for developing networked games
// Copyright (C) 2002-2025 Three Rings Design, Inc., All Rights Reserved
// https://github.com/threerings/narya/blob/master/LICENSE

package com.threerings.crowd.chat.client;

import java.util.Collections;
import java.util.HashSet;

import com.google.common.collect.Sets;

import com.samskivert.util.ObserverList;

import com.threerings.util.MessageBundle;
import com.threerings.util.Name;

import com.threerings.presents.client.BasicDirector;

import com.threerings.crowd.util.CrowdContext;

/**
 * Manages the mutelist.
 *
 * TODO: This class right now is pretty much just a placeholder.
 */
public class MuteDirector extends BasicDirector
    implements ChatFilter
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
        void muteChanged (Name playername, boolean nowMuted);
    }

    /**
     * Should be instantiated after the ChatDirector.
     */
    public MuteDirector (CrowdContext ctx)
    {
        super(ctx);
    }

    /**
     * Set up the mute director with the specified list of initial mutees.
     */
    public MuteDirector (CrowdContext ctx, Name[] list)
    {
        this(ctx);

        Collections.addAll(_mutelist, list);
    }

    /**
     * Called to shut down the mute director.
     */
    public void shutdown ()
    {
        if (_chatdir != null) {
            _chatdir.removeChatFilter(this);
            _chatdir = null;
        }
        _ctx.getClient().removeClientObserver(this);
    }

    /**
     * Set the required ChatDirector.
     */
    public void setChatDirector (ChatDirector chatdir)
    {
        if (_chatdir == null) {
            _chatdir = chatdir;
            _chatdir.addChatFilter(this);
        }
    }

    /**
     * Add the specified mutelist observer.
     */
    public void addMuteObserver (MuteObserver obs)
    {
        _observers.add(obs);
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
    public boolean isMuted (Name username)
    {
        return _mutelist.contains(username);
    }

    /**
     * Mute or unmute the specified user.
     */
    public void setMuted (Name username, boolean mute)
    {
        boolean changed = mute ? _mutelist.add(username) : _mutelist.remove(username);
        String feedback;
        if (mute) {
            feedback = "m.muted";
        } else {
            feedback = changed ? "m.unmuted" : "m.notmuted";
        }

        // always give some feedback to the user
        _chatdir.displayFeedback(null, MessageBundle.tcompose(feedback, username));

        // if the mutelist actually changed, notify observers
        if (changed) {
            notifyObservers(username, mute);
        }
    }

    /**
     * @return a list of the currently muted players.
     *
     * This list may be out of date immediately upon returning from this method.
     */
    public Name[] getMuted ()
    {
        return _mutelist.toArray(new Name[_mutelist.size()]);
    }

    // documentation inherited from interface ChatFilter
    public String filter (String msg, Name otherUser, boolean outgoing)
    {
        // we are only concerned with filtering things going to or coming
        // from muted users
        if ((otherUser != null) && isMuted(otherUser)) {
            // if it was outgoing, explain the dropped message, otherwise
            // silently drop
            if (outgoing) {
                _chatdir.displayFeedback(null, "m.no_tell_mute");
            }
            return null;
        }

        return msg;
    }

    /**
     * Notify our observers of a change in the mutelist.
     */
    protected void notifyObservers (final Name username, final boolean muted)
    {
        _observers.apply(new ObserverList.ObserverOp<MuteObserver>() {
            public boolean apply (MuteObserver observer) {
                observer.muteChanged(username, muted);
                return true;
            }
        });
    }

    /** The chat director that we're working hard for. */
    protected ChatDirector _chatdir;

    /** The mutelist. */
    protected HashSet<Name> _mutelist = Sets.newHashSet();

    /** List of mutelist observers. */
    protected ObserverList<MuteObserver> _observers = ObserverList.newFastUnsafe();
}
