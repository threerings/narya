package com.threerings.util {

import flash.net.ObjectEncoding;

import flash.system.ApplicationDomain;

import flash.utils.ByteArray;
import flash.utils.Endian;

import com.threerings.io.TypedArray;

/**
 * Utility methods for transferring flash properties via
 * the presents dobj system.
 */
public class FlashObjectMarshaller
{
    public static function encode (
        obj :Object, keepArrays :Boolean = false) :Object
    {
        if (obj == null) {
            return null;
        }
        if (!keepArrays && obj is Array) {
            var src :Array = (obj as Array);
            var dest :TypedArray = TypedArray.create(ByteArray);
            for each (var o :Object in src) {
                dest.push(encode(o, true));
            }
            return dest;
        }

        // TODO: Our own encoding, that takes into account
        // the ApplicationDomain
        var bytes :ByteArray = new ByteArray();
        bytes.endian = Endian.BIG_ENDIAN;
        bytes.objectEncoding = ObjectEncoding.AMF3;
        bytes.writeObject(obj);
        return bytes;
    }

    public static function decode (encoded :Object) :Object
    {
        if (encoded == null) {
            return null;
        }
        if (encoded is TypedArray) {
            var src :TypedArray = (encoded as TypedArray);
            var dest :Array = [];
            for each (var b :ByteArray in src) {
                dest.push(decode(b));
            }
            return dest;
        }
        var bytes :ByteArray = (encoded as ByteArray);

        // TODO: Our own decoding, that takes into account
        // the ApplicationDomain
        bytes.endian = Endian.BIG_ENDIAN;
        bytes.objectEncoding = ObjectEncoding.AMF3;
        return bytes.readObject();
    }
}
}
