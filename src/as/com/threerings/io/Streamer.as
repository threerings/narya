package com.threerings.io {

import flash.utils.ByteArray;

import com.threerings.util.ClassUtil;
import com.threerings.util.SimpleMap;

import com.threerings.io.streamers.ArrayStreamer;
import com.threerings.io.streamers.ByteStreamer;
import com.threerings.io.streamers.ByteArrayStreamer;
import com.threerings.io.streamers.FloatStreamer;
import com.threerings.io.streamers.IntegerStreamer;
import com.threerings.io.streamers.LongStreamer;
import com.threerings.io.streamers.NumberStreamer;
import com.threerings.io.streamers.ObjectArrayStreamer;
import com.threerings.io.streamers.ShortStreamer;
import com.threerings.io.streamers.StringStreamer;

public class Streamer
{
    public static const BAD_STREAMER :Streamer = new Streamer(null, null);

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

        return BAD_STREAMER;
    }

    public static function getStreamerByClass (clazz :Class) :Streamer
    {
        initStreamers();

        if (ClassUtil.isAssignableAs(Streamable, clazz)) {
            return null; // Streamable
        }

        if (clazz === TypedArray) {
            throw new Error("Broken, TODO");
        }

        for each (var streamer :Streamer in _streamers) {
            if (streamer._target == clazz) {
                return streamer;
            }
        }

        return BAD_STREAMER;
    }

    public static function getStreamerByJavaName (jname :String) :Streamer
    {
        initStreamers();

        // see if we have a streamer for it
        for each (var streamer :Streamer in _streamers) {
            if (streamer.getJavaClassName() === jname) {
                return streamer;
            }
        }

        // see if it's an array that we unstream using an ArrayStreamer
        if (jname.charAt(0) === "[") {
            var streamer :Streamer = new ArrayStreamer(jname);
            _streamers.push(streamer);
            return streamer;
        }

        // otherwise see if it represents a Streamable
        var clazz :Class = ClassUtil.getClassByName(
            Translations.getFromServer(jname));
        if (ClassUtil.isAssignableAs(Streamable, clazz)) {
            return null; // it's streamable
        }

        return BAD_STREAMER;
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
        throw new Error("Abstract");
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
        throw new Error("Abstract");
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
                new NumberStreamer(),
                new ObjectArrayStreamer(),
                new ByteArrayStreamer(),
                new ByteStreamer(),
                new ShortStreamer(),
                new IntegerStreamer(),
                new LongStreamer(),
                new FloatStreamer()
            ];
        }
    }

    protected var _target :Class;

    protected var _jname :String;

    /** Just a list of our standard streamers. */
    protected static var _streamers :Array;
}
}
