package com.threerings.util {

import flash.net.ObjectEncoding;

import flash.system.ApplicationDomain;

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
        if (obj == null) {
            return null;
        }

        // TODO: Our own encoding, that takes into account
        // the ApplicationDomain
        var bytes :ByteArray = new ByteArray();
        bytes.endian = Endian.BIG_ENDIAN;
        bytes.objectEncoding = ObjectEncoding.AMF3;
        bytes.writeObject(obj);
        return bytes;
    }

    public static function decode (bytes :ByteArray) :Object
    {
        if (bytes == null) {
            return null;
        }

        // TODO: Our own decoding, that takes into account
        // the ApplicationDomain
        bytes.endian = Endian.BIG_ENDIAN;
        bytes.objectEncoding = ObjectEncoding.AMF3;
        return bytes.readObject();
    }
}
}
