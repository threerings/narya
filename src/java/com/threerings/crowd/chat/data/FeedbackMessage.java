//
// $Id: FeedbackMessage.java,v 1.2 2003/06/03 21:41:33 ray Exp $

package com.threerings.crowd.chat.data;

/**
 * A ChatMessage to indicate to the user that an action they took
 * has been processed.
 */
public class FeedbackMessage extends ChatMessage
{
    public FeedbackMessage ()
    {
    }

    /**
     * Construct a FeedbackMessage.
     */
    public FeedbackMessage (String message, String localtype)
    {
        super(message, localtype);
    }
}
