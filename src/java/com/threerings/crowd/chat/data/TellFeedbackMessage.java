//
// $Id$
//
// Narya library - tools for developing networked games
// Copyright (C) 2002-2007 Three Rings Design, Inc., All Rights Reserved
// http://www.threerings.net/code/narya/
//
// This library is free software; you can redistribute it and/or modify it
// under the terms of the GNU Lesser General Public License as published
// by the Free Software Foundation; either version 2.1 of the License, or
// (at your option) any later version.
//
// This library is distributed in the hope that it will be useful,
// but WITHOUT ANY WARRANTY; without even the implied warranty of
// MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
// Lesser General Public License for more details.
//
// You should have received a copy of the GNU Lesser General Public
// License along with this library; if not, write to the Free Software
// Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA 02111-1307 USA

package com.threerings.crowd.chat.data;

import java.io.IOException;

import com.threerings.io.ObjectInputStream;
import com.threerings.io.ObjectOutputStream;
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
        setClientInfo(message, ChatCodes.PLACE_CHAT_TYPE);
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

    // AUTO-GENERATED: METHODS START
    // from interface Streamable
    public void readObject (ObjectInputStream ins)
        throws IOException, ClassNotFoundException
    {
        super.readObject(ins);
        _failure = ins.readBoolean();
    }

    // from interface Streamable
    public void writeObject (ObjectOutputStream out)
        throws IOException
    {
        super.writeObject(out);
        out.writeBoolean(_failure);
    }
    // AUTO-GENERATED: METHODS END

    protected boolean _failure;
}
