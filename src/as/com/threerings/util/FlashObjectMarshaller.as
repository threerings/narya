package com.threerings.util {

import flash.net.ObjectEncoding;

import flash.utils.ByteArray;
import flash.utils.Endian;

/**
 * Utility methods for transferring flash properties via
 * the presents dobj system.
 */
public class FlashObjectMarshaller
{
    public static function encode (obj :Object) :ByteArray
    {
        var bytes :ByteArray = new ByteArray();
        bytes.endian = Endian.BIG_ENDIAN;
        bytes.objectEncoding = ObjectEncoding.AMF3;
        bytes.writeObject(obj);
        return bytes;
    }

    public static function decode (bytes :ByteArray) :Object
    {
        bytes.endian = Endian.BIG_ENDIAN;
        bytes.objectEncoding = ObjectEncoding.AMF3;
        return bytes.readObject();
    }
}
}
