package com.threerings.io {

import flash.util.trace;

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

    public static function getStreamer (obj :Object) :Streamer
    {
        if (obj is Streamable) {
            return null;
        }

        if (_streamers == null) {
            createStreamers();
        }

        for (var ii :int = 0; ii < _streamers.length; ii++) {
            if (_streamers[ii].isStreamerFor(obj)) {
                return _streamers[ii];
            }
        }

        return undefined;
    }

    public static function getStreamerByJavaName (jname :String) :Streamer
    {
        if (_streamers == null) {
            createStreamers();
        }

        for (var ii :int = 0; ii < _streamers.length; ii++) {
            if (_streamers[ii].getJavaClassName() === jname) {
                return _streamers[ii];
            }
        }

        return null;
    }

    /** This should be a protected constructor. */
    public function Streamer (targ :Class, jname :String)
        //throws IOError
    {
        _targ = targ;
        _jname = jname;
    }

    public function isStreamerFor (obj :Object) :Boolean
    {
        return (obj is _targ); // scripting langs are weird
    }

    /**
     * Return the String to use to identify the class that we're streaming.
     */
    public function getJavaClassName () :String
    {
        return _jname;
    }

    public function writeObject (obj :Object, out :ObjectOutputStream) :void
        //throws IOError
    {
        trace("TODO");

        if (obj is Array) {
            trace("Arrays not yet done. Crap!");
            /**
            var arr :Array = (obj as Array); // not strictly necessary
            var length :int = arr.length;
            out.writeInt(length);
            */
        }
    }

    public function createObject (ins :ObjectInputStream) :Object
        //throws IOError
    {
        // actionscript is so fucked up
        return new _targ();
    }

    public function readObject (obj :Object, ins :ObjectInputStream) :void
        //throws IOError
    {
        trace("TODO");
    }

    /**
     * Creates our streamers table map and registers streamers for
     * basic types.
     */
    protected static function createStreamers () :void
    {
        _streamers = new Array();

        // add our default streamers
        _streamers.push(
            new StringStreamer(),
            new IntStreamer(),
            new NumberStreamer(),
            new ArrayStreamer(),
            new ByteArrayStreamer()
        );
    }

    protected var _targ :Class;

    protected var _jname :String;

    /** Just a list of our standard streamers. */
    protected static var _streamers :Array;
}
}
