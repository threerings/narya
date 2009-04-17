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

import flash.net.registerClassAlias; // function import
import flash.net.ObjectEncoding;

import flash.geom.Point;
import flash.geom.Rectangle;

import flash.utils.ByteArray;
import flash.utils.Dictionary;
import flash.utils.Endian;
import flash.utils.IExternalizable;

import com.threerings.io.TypedArray;

/**
 * Utility methods for transforming flash objects into byte[].
 */
public class ObjectMarshaller
{
    /**
     * Encode the specified object as either a byte[] or a byte[][] (see below).
     * The specific mechanism of encoding is not important,
     * as long as decode returns a clone of the original object.
     *
     * No validation is done to verify that the object can be serialized.
     *
     * Currently, cycles in the object graph are preserved on the other end.
     *
     * @param encodeArrayElements if true and the obj is an Array, each element is
     * encoded separately, returning a byte[][] instead of a byte[].
     */
    public static function encode (
        obj :Object, encodeArrayElements :Boolean = false) :Object
    {
        if (obj == null) {
            return null;
        }
        if (encodeArrayElements && obj is Array) {
            var src :Array = (obj as Array);
            var dest :TypedArray = TypedArray.create(ByteArray);
            for (var ii :int = 0; ii < src.length; ii++) {
                dest.push(encode(src[ii], false));
            }
            return dest;
        }

        var bytes :ByteArray = new ByteArray();
        bytes.endian = Endian.BIG_ENDIAN;
        bytes.objectEncoding = ObjectEncoding.AMF3;
        // FIX, Because adobe is FUCKING IT UP as usual.
        // It seems that with flash 10, they "enhanced" AMF3 encoding. That's great and all,
        // except when we try to send this data back to a flash 9 player, which can't read it.
        // Guess what, asshats? If you change the encoding spec, IT'S NOT THE SAME VERSION ANYMORE.
        // Thanks for breaking all our code where we explicitly set AMF3, even though
        // it's currently the default.
        // TODO: find out what's doing it. I can't create a small test case- it seems to
        // only booch inside Whirled. Even using the viewer seems to always work.
        // I suspect it's a SecurityDomain/ApplicationDomain thing that's doing it, but
        // have tried correcting for or erasing those differences and I still have the problem.
        // Sometime.
        if (obj is Dictionary) {
            var asArray :Array = [];
            for (var key :* in obj) {
                asArray.push(key);
                asArray.push(obj[key]);
            }
            obj = asArray;
            // then insert our special marker byte before writing this array
            bytes.writeByte(DICTIONARY_MARKER);
        }
        bytes.writeObject(obj);
        return bytes;
    }

    /**
     * Validate the value and encode it. Arrays are not broken-up.
     * @param maxLength The maximum size of the data after encoding,
     *                  or -1 if no size restriction.
     */
    public static function validateAndEncode (obj :Object, maxLength :int = -1) :ByteArray
    {
        validateValue(obj);

        var data :ByteArray = encode(obj, false) as ByteArray;
        if (maxLength >= 0 && data != null && data.length > maxLength) {
            throw new ArgumentError("Cannot encode data of size " + data.length + " bytes. " +
                                    "May be at most " + maxLength + " bytes.");
        }

        return data;
    }

    /**
     * Decode the specified byte[] or byte[][] back into a flash Object.
     */
    public static function decode (encoded :Object) :Object
    {
        if (encoded == null) {
            return null;
        }
        if (encoded is TypedArray) {
            var src :TypedArray = (encoded as TypedArray);
            var dest :Array = [];
            for (var ii :int = 0; ii < src.length; ii++) {
                dest.push(decode(src[ii] as ByteArray));
            }
            return dest;
        }
        var bytes :ByteArray = (encoded as ByteArray);

        // Work around dictionary idiocy. Holy shit. See note in encode().
        const isDict :Boolean = (bytes[0] === DICTIONARY_MARKER);
        // re-set the position in case we're decoding the actual same byte
        // array used to encode (and not a network reconstruction)
        bytes.position = isDict ? 1 : 0;

        bytes.endian = Endian.BIG_ENDIAN;
        bytes.objectEncoding = ObjectEncoding.AMF3;
        var decoded :Object = bytes.readObject();
        if (isDict) {
            var decodedArray :Array = decoded as Array;
            var asDict :Dictionary = new Dictionary();
            for (var jj :int = 0; jj < decodedArray.length; jj += 2) {
                asDict[decodedArray[jj]] = decodedArray[jj + 1];
            }
            decoded = asDict;
        }
        return decoded;
    }

    /**
     * Validate that the value is kosher for encoding, or throw an ArgumentError if it's not.
     */
    public static function validateValue (value :Object) :void
        // throws ArgumentError
    {
        var s :String = getValidationError(value);
        if (s != null) {
            throw new ArgumentError(s);
        }
    }

    /**
     * Get the String reason why this value is not encodable, or null if no error.
     */
    public static function getValidationError (value :Object) :String
    {
        if (value == null) {
            return null;

        } else if (value is IExternalizable) {
            return "IExternalizable is not yet supported";

        } else if (value is Array) {
            if (ClassUtil.getClassName(value) != "Array") {
                // We can't allow arrays to be serialized as IExternalizables
                // because we need to know element values (opaquely) on the
                // server. Also, we don't allow other types because we wouldn't
                // create the right class on the other side.
                return "Custom array subclasses are not supported";
            }
            // then, continue on with the sub-properties check (below)

        } else if (value is Dictionary) {
            if (ClassUtil.getClassName(value) != "flash.utils.Dictionary") {
                return "Custom Dictionary subclasses are not supported";
            }
            // check all the keys
            for (var key :* in value) {
                var se :String = getValidationError(key);
                if (se != null) {
                    return se;
                }
            }
            // then, continue on with sub-property check (below)

        } else {
            var type :String = typeof(value);
            if (type == "number" || type == "string" || type == "boolean" || type == "xml") {
                return null; // kosher!
            }
            if (-1 != VALID_CLASSES.indexOf(ClassUtil.getClassName(value))) {
                return null; // kosher
            }
            if (!Util.isPlainObject(value)) {
                return "Non-simple properties may not be set.";
            }
            // fall through and verify the plain object's sub-properties
        }

        // check sub-properties (of arrays and objects)
        for each (var arrValue :Object in value) {
            var s :String = getValidationError(arrValue);
            if (s != null) {
                return s;
            }
        }

        return null; // it all checks out!
    }

    // hope and pray that they don't revamp amf3 again and start using this byte. Assholes.
    public static const DICTIONARY_MARKER :int = 99;

    /**
     * Our static initializer.
     */
    private static function staticInit () :void
    {
        registerClassAlias("P", Point);
        registerClassAlias("R", Rectangle);
        registerClassAlias("D", Dictionary);
    }
    staticInit();

    /** Non-simple classes that we allow, as long as they are not subclassed. */
    protected static const VALID_CLASSES :Array = [
        "flash.utils.ByteArray", "flash.geom.Point", "flash.geom.Rectangle" ];
}
}
