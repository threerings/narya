//
// $Id: SpeakObject.java,v 1.1 2003/06/14 00:47:16 mdb Exp $

package com.threerings.crowd.chat.data;

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
        public void apply (int bodyOid);
    }

    /**
     * The speak service will call this every time a chat message is
     * delivered on this speak object to note the listeners that
     * received the message.
     */
    public void applyToListeners (ListenerOp op);
}
