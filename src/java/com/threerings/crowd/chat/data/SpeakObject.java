//
// $Id: SpeakObject.java,v 1.3 2004/03/06 11:29:18 mdb Exp $

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
