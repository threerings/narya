package com.threerings.io {

import flash.util.trace;

import flash.util.ByteArray;

import com.threerings.util.SimpleMap;

import com.threerings.io.streamers.ArrayStreamer;
import com.threerings.io.streamers.ByteArrayStreamer;
import com.threerings.io.streamers.IntStreamer;
import com.threerings.io.streamers.NumberStreamer;
import com.threerings.io.streamers.ObjectArrayStreamer;
import com.threerings.io.streamers.StringStreamer;

public class Streamer
{
    public static function getStreamer (obj :Object) :Streamer
    {
        if (obj is Streamable) {
            return null;
        }

        initStreamers();

        for each (var streamer :Streamer in _streamers) {
            if (streamer.isStreamerFor(obj)) {
                return streamer;
            }
        }

        if (obj is TypedArray) {
            var streamer :Streamer = new ArrayStreamer(
                (obj as TypedArray).getJavaType());
            _streamers.push(streamer);
            return streamer;
        }

        return undefined;
    }

    public static function getStreamerByClass (clazz :Class) :Streamer
    {
        initStreamers();

        if (clazz === TypedArray) {
            throw new Error("Broken, TODO");
        }

        for each (var streamer :Streamer in _streamers) {
            if (streamer._target == clazz) {
                return streamer;
            }
        }

        return undefined;
    }

    public static function getStreamerByJavaName (jname :String) :Streamer
    {
        initStreamers();

        for each (var streamer :Streamer in _streamers) {
            if (streamer.getJavaClassName() === jname) {
                return streamer;
            }
        }

        if (jname.charAt(0) === "[") {
            var streamer :Streamer = new ArrayStreamer(jname);
            _streamers.push(streamer);
            return streamer;
        }

        return null;
    }

    /** This should be a protected constructor. */
    public function Streamer (targ :Class, jname :String)
        //throws IOError
    {
        _target = targ;
        _jname = jname;
    }

    public function isStreamerFor (obj :Object) :Boolean
    {
        return (obj is _target); // scripting langs are weird
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
            var arr :Array = Array(obj); // not strictly necessary
            var length :int = arr.length;
            out.writeInt(length);
            */
        }
    }

    public function createObject (ins :ObjectInputStream) :Object
        //throws IOError
    {
        // actionscript is so fucked up
        return new _target();
    }

    public function readObject (obj :Object, ins :ObjectInputStream) :void
        //throws IOError
    {
        trace("TODO");
    }

    /**
     * Initialize our streamers. This cannot simply be done statically
     * because we cannot instantiate a subclass when this class is still
     * being created. Fucking actionscript.
     */
    private static function initStreamers () :void
    {
        if (_streamers == null) {
            _streamers = [
                new StringStreamer(),
                new IntStreamer(),
                new NumberStreamer(),
                new ObjectArrayStreamer(),
                new ByteArrayStreamer()
            ];
        }
    }

    protected var _target :Class;

    protected var _jname :String;

    /** Just a list of our standard streamers. */
    protected static var _streamers :Array;
}
}
