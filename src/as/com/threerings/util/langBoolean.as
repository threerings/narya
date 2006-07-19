package com.threerings.util {

import com.threerings.io.ObjectInputStream;
import com.threerings.io.ObjectOutputStream;
import com.threerings.io.Streamable;

/**
 * Equivalent to java.lang.Boolean.
 */
public class langBoolean
    implements Equalable, Streamable
{
    public var value :Boolean;

    public static function valueOf (val :Boolean) :langBoolean
    {
        return new langBoolean(val);
    }

    public function langBoolean (value :Boolean = false)
    {
        this.value = value;
    }

    // from Equalable
    public function equals (other :Object) :Boolean
    {
        return (other is langBoolean) &&
            (value === (other as langBoolean).value);
    }

    // from Streamable
    public function writeObject (out :ObjectOutputStream) :void
    {
        out.writeBoolean(value);
    }

    // from Streamable
    public function readObject (ins :ObjectInputStream) :void
    {
        value = ins.readBoolean();
    }
}
}
