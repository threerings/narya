package com.threerings.io {

import com.threerings.util.SimpleMap;

public class Streamer
{
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

        // TODO: fill in with the basic streamers
    }

    protected var _targ :Class;

    protected static var _streamerMap :SimpleMap;
}
}
