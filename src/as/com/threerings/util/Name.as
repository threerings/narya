package com.threerings.util {

import com.threerings.io.ObjectInputStream;
import com.threerings.io.ObjectOutputStream;
import com.threerings.io.Streamable;

public class Name extends Object
    implements Streamable
{
    public function Name (name :String = "")
    {
        _name = name;
    }

    public function getNormal () :String
    {
        if (_normal == null) {
            _normal = normalize(_name);
        }
        return _normal;
    }

    public function isValid () :Boolean
    {
        return !isBlank();
    }

    public function isBlank () :Boolean
    {
        return Name.isBlank(this);
    }

    public /*override*/ function toString () :String
    {
        return _name;
    }

    // TODO: needed? I'd think so. Maybe we need to create OOObject
    // that all our classes can extend that define a reasonable
    // equals().
    public function equals (other :Name) :Boolean
    {
        return getNormal() === other.getNormal();
    }

    // TODO: comparable?

    // documentation inherited from interface Streamable
    public function writeObject (out :ObjectOutputStream) :void
        //throws IOError
    {
        /*
        // TODO: oh shit, this is wrong. We need to write a boolean out
        // indicating whether the object that follows is null or not
        out.writeBareObject(_name);
        */

        out.writeField(_name);

        // we need a way of doing the following automatically:
        /*
        var b :Boolean = (_name != null);
        out.writeBoolean(b);
        if (b) {
            out.writeUTF(_name);
        }
        */


    }

    // documentation inherited from interface Streamable
    public function readObject (ins :ObjectInputStream) :void
        //throws IOError
    {
        // TODO: oh shit, this is wrong
        _name = ins.readField(String);

        /*
        var b :Boolean = in.readBoolean();
        _name = b ? in.readUTF() : null;
        */
    }

    protected function normalize (txt :String) :String
    {
        return txt.toLowerCase();
    }

    public static function isBlank (name :Name) :Boolean
    {
        return (name == null || "" === name.toString());
    }

    /** The raw name text. */
    protected var _name :String;

    /** The normalized name text. */
    protected var _normal :String;
}
}
