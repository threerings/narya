//
// $Id$
//
// Narya library - tools for developing networked games
// Copyright (C) 2002-2007 Three Rings Design, Inc., All Rights Reserved
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

import com.threerings.util.Name;

/**
 * Filters messages chat messages to or from the server.
 */
public interface ChatFilter
{
    /**
     * Filter a chat message.
     * @param msg the message text to be filtered.
     * @param otherUser an optional argument that represents the target or the
     * speaker, depending on 'outgoing', and can be considered in filtering if
     * it is provided.
     * @param outgoing true if the message is going out to the server.
     *
     * @return the filtered message, or null to block it completely.
     */
    function filter (msg :String, otherUser :Name, outgoing :Boolean) :String;
}
}
