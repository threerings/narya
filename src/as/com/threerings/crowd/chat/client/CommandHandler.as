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

import com.threerings.crowd.data.BodyObject;
import com.threerings.crowd.util.CrowdContext;

/**
 * Used to implement a slash command (e.g. <code>/who</code>).
 */
public /* abstract */ class CommandHandler
{
    /**
     * Handles the specified chat command.
     *
     * @param speakSvc an optional SpeakService object representing
     * the object to send the chat message on.
     * @param command the slash command that was used to invoke this
     * handler (e.g. <code>/tell</code>).
     * @param args the arguments provided along with the command (e.g.
     * <code>Bob hello</code>) or <code>null</code> if no arguments
     * were supplied.
     * @param history an in/out parameter that allows the command to
     * modify the text that will be appended to the chat history. If
     * this is set to null, nothing will be appended.
     *
     * @return an untranslated string that will be reported to the
     * chat box to convey an error response to the user, or {@link
     * ChatCodes#SUCCESS}.
     */
    public function handleCommand (
            ctx :CrowdContext, speakSvc :SpeakService,
            cmd :String, args :String, history :Array) :String
    {
        throw new Error("abstract");
    }

    /**
     * Returns true if this user should have access to this chat
     * command.
     */
    public function checkAccess (user :BodyObject) :Boolean
    {
        return true;
    }
}
}
