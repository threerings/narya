//
// $Id: ChatFilter.java,v 1.2 2004/03/06 11:29:18 mdb Exp $

package com.threerings.crowd.chat.client;

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
    public String filter (String msg, Name otherUser, boolean outgoing);
}
