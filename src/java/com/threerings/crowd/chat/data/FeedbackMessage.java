//
// $Id: FeedbackMessage.java,v 1.1 2002/07/26 20:35:01 ray Exp $

package com.threerings.crowd.chat;

/**
 * A ChatMessage to indicate to the user that an action they took
 * has been processed.
 */
public class FeedbackMessage extends ChatMessage
{
    /**
     * Construct a FeedbackMessage.
     */
    public FeedbackMessage (String message, String localtype)
    {
        super(message, localtype);
    }
}
