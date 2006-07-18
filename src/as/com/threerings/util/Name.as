package com.threerings.util {

import com.threerings.util.Equalable;

import com.threerings.io.ObjectInputStream;
import com.threerings.io.ObjectOutputStream;
import com.threerings.io.Streamable;

public class Name extends Object
    implements Comparable, Hashable, Streamable
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

    // from interface Hashable
    public function equals (other :Object) :Boolean
    {
        return (other is Name) &&
            (getNormal() === (other as Name).getNormal());
    }

    // from interface Hashable
    public function hashCode () :int
    {
        var norm :String = getNormal();
        var hash :int = 0;
        for (var ii :int = 0; ii < norm.length; ii++) {
            hash = (hash << 1) ^ int(norm.charCodeAt(ii));
        }
        return hash;
    }

    // from interface Comparable
    public function compareTo (other :Object) :int
    {
        var thisNormal :String = getNormal();
        var thatNormal :String = (other as Name).getNormal();
        if (thisNormal == thatNormal) {
            return 0;

        } if (thisNormal < thatNormal) {
            return -1;

        }  else {
            return 1;
        }
    }

    // from interface Streamable
    public function writeObject (out :ObjectOutputStream) :void
        //throws IOError
    {
        out.writeField(_name);
    }

    // from interface Streamable
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
