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

import com.threerings.crowd.chat.data.ChatMessage;

/**
 * A chat display provides a means by which chat messages can be
 * displayed. The chat display will be notified when chat messages of
 * various sorts have been received by the client.
 */
public interface ChatDisplay
{
    /**
     * Called to clear the chat display.
     */
    public void clear ();

    /**
     * Called to display a chat message.
     *
     * @param alreadyDisplayed true if a previous chat display in the list has
     * already displayed this message, false otherwise.
     *
     * @return true if the message was displayed, false if not.
     */ 
    public boolean displayMessage (ChatMessage msg, boolean alreadyDisplayed);
}
