//
// $Id: SystemMessage.java,v 1.3 2003/06/04 02:50:18 ray Exp $

package com.threerings.crowd.chat.data;

/**
 * A ChatMessage that represents a message that came from the server
 * and did not result from direct user action.
 */
public class SystemMessage extends ChatMessage
{
    /** Attention level constant to indicate that this message is merely
     * providing the user with information. */
    public static final byte INFO      = 0;

    /** Attention level constant to indicate that this message is the
     * result of a user action. */
    public static final byte FEEDBACK  = 1;

    /** Attention level constant to indicate that some action is required. */
    public static final byte ATTENTION = 2;

    //----

    /** The attention level of this message. */
    public byte attentionLevel;

    // documentation inherited
    public SystemMessage ()
    {
    }

    /**
     * Construct a SystemMessage.
     */
    public SystemMessage (String message, String bundle, byte attentionLevel)
    {
        super(message, bundle);
        this.attentionLevel = attentionLevel;
    }
}
