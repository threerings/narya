//
// $Id: ChatFilter.java,v 1.1 2003/09/15 21:11:40 ray Exp $

package com.threerings.crowd.chat.client;

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
    public String filter (String msg, String otherUser, boolean outgoing);
}
