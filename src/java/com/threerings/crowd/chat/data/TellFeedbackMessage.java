//
// $Id: TellFeedbackMessage.java,v 1.1 2003/06/03 23:46:10 ray Exp $

package com.threerings.crowd.chat.data;

/**
 * A feedback message to indicate that a tell succeeded.
 */
public class TellFeedbackMessage extends FeedbackMessage
{
    /**
     * A tell feedback message is only composed on the client.
     */
    public TellFeedbackMessage (String message)
    {
        super(message, null);
        setClientInfo(message, ChatCodes.PLACE_CHAT_TYPE);
    }
}
