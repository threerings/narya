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

import flash.system.ApplicationDomain;

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

        // TODO: Our own encoding, that takes into account
        // the ApplicationDomain.. ACK
        var bytes :ByteArray = new ByteArray();
        bytes.endian = Endian.BIG_ENDIAN;
        bytes.objectEncoding = ObjectEncoding.AMF3;
        bytes.writeObject(obj);
        return bytes;
    }

    /**
     * Validate the value and encode it. Arrays are not broken-up.
     */
    public static function validateAndEncode (obj :Object) :ByteArray
    {
        validateValue(obj);
        return encode(obj, false) as ByteArray;
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
        // re-set the position in case we're decoding the actual same byte
        // array used to encode (and not a network reconstruction)
        bytes.position = 0;

        // TODO: Our own decoding, that takes into account
        // the ApplicationDomain.. ACK
        bytes.endian = Endian.BIG_ENDIAN;
        bytes.objectEncoding = ObjectEncoding.AMF3;
        return bytes.readObject();
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
            if (type == "number" || type == "string" || type == "boolean" ) {
                return null; // kosher!
            }
            if (-1 != VALID_CLASSES.indexOf(ClassUtil.getClass(value))) {
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

    /**
     * Our static initializer.
     */
    private static function staticInit () :void
    {
        registerClassAlias("Point", Point);
        registerClassAlias("Rectangle", Rectangle);
        registerClassAlias("Dictionary", Dictionary);
    }
    staticInit();

    /** Non-simple classes that we allow, as long as they are not subclassed. */
    protected static const VALID_CLASSES :Array = [ ByteArray, Point, Rectangle ];
}
}
