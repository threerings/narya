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

package com.threerings.util {

import flash.utils.ByteArray;
import flash.utils.Endian;

/**
 * Equivalent to java.lang.Long.
 */
public class Long
    implements Equalable
{
    public var bytes :ByteArray;

    public function Long ()
    {
        bytes = new ByteArray();
        bytes.endian = Endian.BIG_ENDIAN;
        bytes.writeDouble(0);
        bytes.position = 0;
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
            l.bytes[ii] = (n % 256);
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
        var positive :Boolean = ((bytes[0] & 0x80) == 0x00);
        for (var ii :int = 0; ii < 8; ii++) {
            // if the number is negative, complement each byte as it comes in, and fix up later
            n = n * 256 + (positive ? bytes[ii] : (255 - bytes[ii]));
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
            if (bytes[ii] < 16) { s += "0"; }
            s += int(bytes[ii]).toString(16);
        }
        s += "]";
        return s;
    }

    // from Equalable
    public function equals (other :Object) :Boolean
    {
        var that :Long = (other as Long);
        if (that == null || this.bytes.length != 8 || that.bytes.length != 8) {
            return false;
        }

        // byte-wise comparison
        for (var ii :int = 0; ii < 8; ii++) {
            if (this.bytes[ii] != that.bytes[ii]) {
                return false;
            }
        }
        return true;
    }
}
}
