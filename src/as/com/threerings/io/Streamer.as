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

package com.threerings.io {

import flash.utils.ByteArray;
import flash.utils.Dictionary;

import com.threerings.util.ClassUtil;
import com.threerings.util.Enum;

import com.threerings.io.streamers.ArrayStreamer;
import com.threerings.io.streamers.ByteStreamer;
import com.threerings.io.streamers.ByteArrayStreamer;
import com.threerings.io.streamers.EnumStreamer;
import com.threerings.io.streamers.FloatStreamer;
import com.threerings.io.streamers.IntegerStreamer;
import com.threerings.io.streamers.LongStreamer;
import com.threerings.io.streamers.NumberStreamer;
import com.threerings.io.streamers.ShortStreamer;
import com.threerings.io.streamers.StringStreamer;

public class Streamer
{
    public static function getStreamer (obj :Object) :Streamer
    {
        if (obj is TypedArray) {
            return getStreamerByJavaName(TypedArray(obj).getJavaType());
        } else {
            return getStreamerByClass(ClassUtil.getClass(obj));
        }
    }

    public static function getStreamerByClass (clazz :Class) :Streamer
    {
        initStreamers();

        if (clazz === TypedArray) {
            throw new Error("Broken, TODO");
        }

        var streamer :Streamer = _byClass[clazz] as Streamer;
        if (streamer != null) {
            return streamer;
        }

        if (ClassUtil.isAssignableAs(Enum, clazz)) {
            streamer = new EnumStreamer(clazz);

        } else if (ClassUtil.isAssignableAs(Streamable, clazz)) {
            streamer = new Streamer(clazz);

        } else {
            return null;
        }

        // add the new streamer and return it
        registerStreamer(streamer);
        return streamer;
    }

    public static function getStreamerByJavaName (jname :String) :Streamer
    {
        initStreamers();
        // unstream lists as simple arrays
        if (jname == "java.util.List" || jname == "java.util.ArrayList") {
            jname = "[Ljava.lang.Object;";
        }

        // see if we have a streamer for it
        var streamer :Streamer = _byJName[jname] as Streamer;
        if (streamer != null) {
            return streamer;
        }

        // see if it's an array that we unstream using an ArrayStreamer
        if (jname.charAt(0) === "[") {
            streamer = new ArrayStreamer(jname);

        } else {
            // otherwise see if it represents a Streamable
            var clazz :Class = ClassUtil.getClassByName(Translations.getFromServer(jname));

            if (ClassUtil.isAssignableAs(Enum, clazz)) {
                streamer = new EnumStreamer(clazz, jname);

            } else if (ClassUtil.isAssignableAs(Streamable, clazz)) {
                streamer = new Streamer(clazz, jname);

            } else {
                return null;
            }
        }

        // add the good new streamer
        registerStreamer(streamer);
        return streamer;
    }

    /** This should be a protected constructor. */
    public function Streamer (targ :Class, jname :String = null)
        //throws IOError
    {
        _target = targ;
        _jname = (jname != null) ? jname : Translations.getToServer(ClassUtil.getClassName(targ));
    }

    /**
     * Return the String to use to identify the class that we're streaming.
     */
    public function getJavaClassName () :String
    {
        return _jname;
    }

    /**
     * Return the Class identifying that which we stream, or null to not be registered.
     */
    public function getClass () :Class
    {
        return _target;
    }

    public function writeObject (obj :Object, out :ObjectOutputStream) :void
        //throws IOError
    {
        (obj as Streamable).writeObject(out);
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
        (obj as Streamable).readObject(ins);
    }

    protected static function registerStreamer (st :Streamer) :void
    {
        _byJName[st.getJavaClassName()] = st;
        trace(": registered " + st.getJavaClassName());
        var c :Class = st.getClass();
        if (c != null) {
            _byClass[c] = st;
        }
    }

    /**
     * Initialize our streamers. This cannot simply be done statically
     * because we cannot instantiate a subclass when this class is still
     * being created. Fucking actionscript.
     */
    private static function initStreamers () :void
    {
        if (_byJName != null) {
            return;
        }
        _byJName = new Dictionary();
        _byClass = new Dictionary();
        for each (var c :Class in
                [ StringStreamer, NumberStreamer, ByteStreamer, IntegerStreamer, LongStreamer,
                  FloatStreamer, ArrayStreamer, ByteArrayStreamer ]) {
            registerStreamer(Streamer(new c()));
        }
    }

    protected var _target :Class;

    protected var _jname :String;

    protected static var _byClass :Dictionary;
    protected static var _byJName :Dictionary;
}
}
