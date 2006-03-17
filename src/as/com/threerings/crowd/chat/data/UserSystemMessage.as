//
// $Id$

package com.threerings.crowd.chat.data {

import com.threerings.util.Name;

import com.threerings.io.ObjectInputStream;
import com.threerings.io.ObjectOutputStream;

/**
 * A system message triggered by the activity of another user.
 * If the user is muted we can suppress this message, unlike a normal
 * system message.
 */
public class UserSystemMessage extends SystemMessage
{
    /** The "speaker" of this message, the user that triggered that this
     * message be sent to us. */
    public var speaker :Name;

    /**
     * Construct a UserSystemMessage.
     */
    public function UserSystemMessage (
            sender :Name = null, message :String = null, bundle :String = null,
            attLevel :int = 0)
    {
        super(message, bundle, attLevel);
        this.speaker = sender;
    }

    public override function readObject (ins :ObjectInputStream) :void
    {
        super.readObject(ins);
        speaker = (ins.readObject() as Name);
    }

    public override function writeObject (out :ObjectOutputStream) :void
    {
        super.writeObject(out);
        out.writeObject(speaker);
    }
}
}
