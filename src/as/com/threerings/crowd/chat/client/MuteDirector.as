//
// $Id$
//
// Narya library - tools for developing networked games
// Copyright (C) 2002-2010 Three Rings Design, Inc., All Rights Reserved
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

package com.threerings.crowd.chat.client {

import com.threerings.util.MessageBundle;
import com.threerings.util.Name;
import com.threerings.util.ObserverList;
import com.threerings.util.Set;
import com.threerings.util.Sets;

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
     * Should be instantiated after the ChatDirector.
     */
    public function MuteDirector (ctx :CrowdContext)
    {
        super(ctx);
    }

    /**
     * Called to shut down the mute director.
     */
    public function shutdown () :void
    {
        if (_chatdir != null) {
            _chatdir.removeChatFilter(this);
            _chatdir = null;
        }
    }

    /**
     * Set the required ChatDirector.
     */
    public function setChatDirector (chatdir :ChatDirector) :void
    {
        if (_chatdir == null) {
            _chatdir = chatdir;
            _chatdir.addChatFilter(this);
        }
    }

    /**
     * Add the specified mutelist observer.
     */
    public function addMuteObserver (obs :MuteObserver) :void
    {
        _observers.add(obs);
    }

    /**
     * Remove the specified mutelist observer.
     */
    public function removeMuteObserver (obs :MuteObserver) :void
    {
        _observers.remove(obs);
    }

    /**
     * Check to see if the specified user is muted.
     */
    public function isMuted (username :Name) :Boolean
    {
        return _mutelist.contains(username);
    }

    /**
     * Mute or unmute the specified user.
     */
    public function setMuted (username :Name, mute :Boolean, giveFeedback :Boolean = true) :void
    {
        var changed :Boolean = mute ? _mutelist.add(username) : _mutelist.remove(username);

        if (giveFeedback) {
            var feedback :String = mute ? "m.muted"
                : (changed ? "m.unmuted" : "m.notmuted");
            _chatdir.displayFeedback(null, MessageBundle.tcompose(feedback, username));
        }

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
    public function getMuted () :Array /* of Name */
    {
        return _mutelist.toArray();
    }

    // documentation inherited from interface ChatFilter
    public function filter (msg :String, otherUser :Name, outgoing :Boolean) :String
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
    protected function notifyObservers (username :Name, muted :Boolean) :void
    {
        _observers.apply(function (observer :MuteObserver) :void {
            observer.muteChanged(username, muted);
        });
    }

    /** The chat director that we're working hard for. */
    protected var _chatdir :ChatDirector;

    /** The mutelist. */
    protected var _mutelist :Set = Sets.newSetOf(Name);

    /** List of mutelist observers. */
    protected var _observers :ObserverList = new ObserverList(ObserverList.FAST_UNSAFE_NOTIFY);
}
}
