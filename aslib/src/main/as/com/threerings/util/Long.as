//
// $Id$
//
// Narya library - tools for developing networked games
// Copyright (C) 2002-2012 Three Rings Design, Inc., All Rights Reserved
// http://code.google.com/p/narya/
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

package com.threerings.util {

import flash.utils.ByteArray;
import flash.utils.Endian;

import com.threerings.io.ObjectInputStream;
import com.threerings.io.ObjectOutputStream;
import com.threerings.io.Streamable;

/**
 * Equivalent to java.lang.Long.
 */
public final class Long
    implements Hashable, Streamable
{
    public function Long ()
    {
        _bytes = new ByteArray();
        _bytes.endian = Endian.BIG_ENDIAN;
        _bytes.writeDouble(0);
        _bytes.position = 0;
    }

    /**
     * Creates a new Long from the provided variable. Only integers in the [-2^63, 2^63) range
     * can be converted; non-integer values in this range will be rounded, and values outside
     * of the range will trigger an ArgumentError. Additionally, since Number is a
     * double-precision floating point value, values outside of the [-2^52, 2^52) range
     * will suffer loss of precision.
     */
    public static function fromNumber (value :Number = 0) :Long
    {
        if (value < -9223372036854775808 || value >= 9223372036854775808 ||
            isNaN(value) || !isFinite(value)) {
            throw new ArgumentError("Out of range initialization value for Long: " + value);
        }

        var n :Number = Math.round(value);
        var l :Long = new Long();
        for (var ii :int = 7; ii >= 0; ii--) {
            l._bytes[ii] = (n % 256);
            n = Math.floor(n / 256);
        }
        return l;
    }

    /**
     * Creates a new Number from this Long variable. Since Number is a double-precision
     * floating point type, values outside the [-2^52, 2^52) range will lose precision.
     */
    public function toNumber () :Number
    {
        var n :Number = 0;
        var positive :Boolean = ((_bytes[0] & 0x80) == 0x00);
        for (var ii :int = 0; ii < 8; ii++) {
            // if the number is negative, complement each byte as it comes in, and fix up later
            n = n * 256 + (positive ? _bytes[ii] : (255 - _bytes[ii]));
        }
        // now fix up negative numbers
        if (! positive) {
            n = -(n + 1);
        }
        return n;
    }

    public function toString () :String
    {
        var s :String = "Long [0x ";
        for (var ii :int = 0; ii < 8; ii++) {
            // my kingdom for a hex formatting routine!
            if (ii != 0) { s += " "; }
            if (_bytes[ii] < 16) { s += "0"; }
            s += int(_bytes[ii]).toString(16);
        }
        s += "]";
        return s;
    }

    // from Equalable
    public function equals (other :Object) :Boolean
    {
        return (other is Long) && Util.equals(_bytes, Long(other)._bytes);
    }

    // from Hashable
    public function hashCode () :int
    {
        return _bytes[0];
    }

    // from Streamable
    public function readObject (ins :ObjectInputStream) :void
    {
        ins.readBytes(_bytes, 0, 8);
    }

    // from Streamable
    public function writeObject (out :ObjectOutputStream) :void
    {
        out.writeBytes(_bytes, 0, 8);
    }

    protected var _bytes :ByteArray;
}
}
