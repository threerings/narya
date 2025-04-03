//
// Narya library - tools for developing networked games
// Copyright (C) 2002-2025 Three Rings Design, Inc., All Rights Reserved
// https://github.com/threerings/narya/blob/master/LICENSE

package com.threerings.crowd.chat.data;

import com.threerings.util.Name;

/**
 * A feedback message to indicate that a tell succeeded.
 */
public class TellFeedbackMessage extends UserMessage
{
    /**
     * A tell feedback message is only composed on the client.
     */
    public TellFeedbackMessage (Name target, String message, boolean failure)
    {
        super(target, null, message, ChatCodes.DEFAULT_MODE);
        _failure = failure;
    }

    /**
     * Returns true if this is a failure feedback, false if it is successful tell feedback.
     */
    public boolean isFailure ()
    {
        return _failure;
    }

    @Override
    public String getFormat ()
    {
        return _failure ? null : "m.told_format";
    }

    protected boolean _failure;
}
