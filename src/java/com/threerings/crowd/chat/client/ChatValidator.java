//
// $Id: ChatValidator.java,v 1.2 2003/06/03 21:41:33 ray Exp $

package com.threerings.crowd.chat.client;

/**
 * A chat validator validates chat messages before they are sent to the
 * server. By default, each method returns true, it is assumed that
 * validators will be added that override at least one of the methods.
 */
public interface ChatValidator
{
    /**
     * Validate that the specified speak message may be sent.
     */
    public boolean validateSpeak (String msg);

    /**
     * Validate that the specified tell message may be sent.
     */
    public boolean validateTell (String target, String msg);
}
