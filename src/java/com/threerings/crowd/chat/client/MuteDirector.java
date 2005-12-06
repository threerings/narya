//
// $Id$
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

package com.threerings.crowd.chat.client;

import java.util.HashSet;

import com.samskivert.util.CollectionUtil;
import com.samskivert.util.ObserverList;

import com.threerings.util.MessageBundle;
import com.threerings.util.Name;

import com.threerings.crowd.util.CrowdContext;

import com.threerings.presents.client.BasicDirector;
import com.threerings.presents.client.Client;

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
        public void muteChanged (Name playername, boolean nowMuted);
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

        CollectionUtil.addAll(_mutelist, list);
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
        boolean changed = mute ? _mutelist.add(username)
                               : _mutelist.remove(username);
        String feedback;
        if (mute) {
            feedback = "m.muted";
        } else {
            feedback = changed ? "m.unmuted" : "m.notmuted";
        }

        // always give some feedback to the user
        _chatdir.displayFeedback(null,
            MessageBundle.tcompose(feedback, username));

        // if the mutelist actually changed, notify observers
        if (changed) {
            notifyObservers(username, mute);
        }
    }

    /**
     * @return a list of the currently muted players.
     *
     * This list may be out of date immediately upon returning from this
     * method.
     */
    public Name[] getMuted ()
    {
        return (Name[]) _mutelist.toArray(new Name[_mutelist.size()]);
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
        _observers.apply(new ObserverList.ObserverOp() {
            public boolean apply (Object observer) {
                ((MuteObserver)observer).muteChanged(username, muted);
                return true;
            }
        });
    }

    /** The chat director that we're working hard for. */
    protected ChatDirector _chatdir;

    /** The mutelist. */
    protected HashSet _mutelist = new HashSet();

    /** List of mutelist observers. */
    protected ObserverList _observers =
        new ObserverList(ObserverList.FAST_UNSAFE_NOTIFY);
}
