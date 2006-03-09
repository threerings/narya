package com.threerings.util {

import com.threerings.util.Equalable;

import com.threerings.io.ObjectInputStream;
import com.threerings.io.ObjectOutputStream;
import com.threerings.io.Streamable;

public class Name extends Object
    implements Equalable, Streamable
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

    public function toString () :String
    {
        return _name;
    }

    // documentation inherited from interface Equalable
    public function equals (other :Object) :Boolean
    {
        return (other is Name) &&
            (getNormal() === (other as Name).getNormal());
    }

    // documentation inherited from interface Streamable
    public function writeObject (out :ObjectOutputStream) :void
        //throws IOError
    {
        out.writeField(_name);
    }

    // documentation inherited from interface Streamable
    public function readObject (ins :ObjectInputStream) :void
        //throws IOError
    {
        _name = (ins.readField(String) as String);
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
