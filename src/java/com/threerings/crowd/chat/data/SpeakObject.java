//
// $Id: SpeakObject.java,v 1.4 2004/08/27 02:12:31 mdb Exp $
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

package com.threerings.crowd.chat.data;

import com.threerings.util.Name;

/**
 * Provides a mechanism by which the speak service can identify chat
 * listeners so as to maintain a recent history of all chat traffic on the
 * server.
 */
public interface SpeakObject
{
    /** Used in conjunction with {@link #applyToListeners}. */
    public static interface ListenerOp
    {
        /** Call this method if you only have access to body oids. */
        public void apply (int bodyOid);

        /** Call this method if you can provide usernames directly. */
        public void apply (Name username);
    }

    /**
     * The speak service will call this every time a chat message is
     * delivered on this speak object to note the listeners that
     * received the message.
     */
    public void applyToListeners (ListenerOp op);
}
