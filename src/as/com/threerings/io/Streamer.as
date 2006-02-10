package com.threerings.io {

import flash.util.ByteArray;

import com.threerings.util.SimpleMap;

import com.threerings.io.streamers.ArrayStreamer;
import com.threerings.io.streamers.ByteArrayStreamer;
import com.threerings.io.streamers.IntStreamer;
import com.threerings.io.streamers.NumberStreamer;
import com.threerings.io.streamers.StringStreamer;

public class Streamer
{
    /*
    public static function getStreamer (className :String) :Streamer
        //throws IOError
    {
        if (_streamerMap == null) {
            createStreamers();
        }

        var streamer :Streamer = _streamerMap[className];
        if (streamer == null) {
            streamer = new Streamer(className);
            _streamerMap[className] = streamer;
        }

        return streamer;
    }
    */

    public static function getStreamer (obj :*) :Streamer
    {
        if (_streamerMap == null) {
            createStreamers();
        }

        if (obj is Streamable) {
            return null;

        } else if (obj is String) {
            return STRING_STREAMER;

        } else if (obj is int) {
            return INT_STREAMER;

        } else if (obj is Number) {
            return NUMBER_STREAMER;

        } else if (obj is Array) {
            return ARRAY_STREAMER;

        } else if (obj is ByteArray) {
            return BYTE_ARRAY_STREAMER;

        } else {
            return undefined;
        }
    }

    public static function getStreamerByJavaName (jname :String) :Streamer
    {
        if (jname === "java.lang.String") {
            return STRING_STREAMER;

        } else if (jname === "java.lang.Integer") {
            return INT_STREAMER;

        } else if (jname === "java.lang.Double") {
            return NUMBER_STREAMER;

        } else if (jname === "[Ljava.lang.Object") {
            return ARRAY_STREAMER;

        } else if (jname === "[B") {
            return BYTE_ARRAY_STREAMER;

        } else {
            return null;
        }
    }

    /** This should be a protected constructor. */
    public function Streamer (targ :Class)
        //throws IOError
    {
        _targ = targ;
    }

    /**
     * Return the String to use to identify the class that we're streaming.
     */
    // TODO
    public function getClassName () :String
    {
        return "TODO: javaname";
    }

    public function writeObject (obj :*, out :ObjectOutputStream,
            useWriter :Boolean) :void
        //throws IOError
    {
        // TODO: check use of isProtoTypeOf, it's unclear if it's
        // going to do what we want
        if (useWriter && obj.isPrototypeOf(Streamable)) {
            obj.writeObject(out);
            return;
        }

        // TODO: cope with arrays

        // write out the fields... this is bogus because
        // we just want to call the streamer method
        obj.writeObject(out);
    }

    public function createObject (ins :ObjectInputStream) :*
        //throws IOError
    {
        // actionscript is so fucked up
        return new _targ();
    }

    public function readObject (obj :*, ins :ObjectInputStream,
            useReader :Boolean) :void
        //throws IOError
    {
        // TODO: check use of isProtoTypeOf, it's unclear if it's
        // going to do what we want
        if (useReader && obj.isPrototypeOf(Streamable)) {
            obj.readObject(ins);
            return;
        }

        // TODO: cope with arrays

        // read in the fields... this is bogus because we just
        // want to call the streamer method
        obj.readObject(ins);
    }

    /**
     * Creates our streamers table map and registers streamers for
     * basic types.
     */
    protected static function createStreamers () :void
    {
        _streamerMap = new SimpleMap();
    }

    protected var _targ :Class;

    protected static var _streamerMap :SimpleMap;


    protected static const STRING_STREAMER :Streamer = new StringStreamer();
    protected static const BYTE_ARRAY_STREAMER :Streamer =
        new ByteArrayStreamer();
    protected static const ARRAY_STREAMER :Streamer = new ArrayStreamer();
    protected static const INT_STREAMER :Streamer = new IntStreamer();
    protected static const NUMBER_STREAMER :Streamer = new NumberStreamer();
}
}
