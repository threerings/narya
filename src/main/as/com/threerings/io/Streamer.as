//
// $Id$
//
// Narya library - tools for developing networked games
// Copyright (C) 2002-2012 Three Rings Design, Inc., All Rights Reserved
// http://code.google.com/p/narya/
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

import com.threerings.io.streamers.ArrayStreamer;
import com.threerings.io.streamers.ByteArrayStreamer;
import com.threerings.io.streamers.ByteEnumStreamer;
import com.threerings.io.streamers.DelegatingStreamer;
import com.threerings.io.streamers.EnumStreamer;
import com.threerings.io.streamers.MapStreamer;
import com.threerings.io.streamers.NumberStreamer;
import com.threerings.io.streamers.SetStreamer;
import com.threerings.io.streamers.StringStreamer;

import com.threerings.util.ByteEnum;
import com.threerings.util.ClassUtil;
import com.threerings.util.Enum;
import com.threerings.util.Log;

public class Streamer
{
    public static function registerStreamer (st :Streamer, ... extraJavaNames) :void
    {
        initStreamers();

        extraJavaNames.unshift(st.getJavaClassName());
        for each (var name :String in extraJavaNames) {
            if (name in _byJName) {
                log.warning("Existing Streamer replaced", "name", name,
                    "cur", _byJName[name], "replacement", st);
            }
            _byJName[name] = st;
        }
    }

    public static function getStreamer (obj :Object) :Streamer
    {
        var jname :String;
        if (obj is TypedArray) {
            jname = TypedArray(obj).getJavaType();
        } else {
            jname = Translations.getToServer(ClassUtil.getClassName(obj));
        }
        return getStreamerByJavaName(jname);
    }

    public static function getStreamerByJavaName (jname :String) :Streamer
    {
        initStreamers();

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
            // usually this is called from ObjectInputStream, but when it's called from
            // ObjectOutputStream it's a bit annoying, because we started with a class/object.
            // But: the code is smaller, so that wins
            var clazz :Class = ClassUtil.getClassByName(Translations.getFromServer(jname));

            if (ClassUtil.isAssignableAs(Enum, clazz)) {
                streamer = ClassUtil.isAssignableAs(ByteEnum, clazz) ?
                    new ByteEnumStreamer(clazz, jname) : new EnumStreamer(clazz, jname);

            } else if (ClassUtil.isAssignableAs(Streamable, clazz)) {
                streamer = new Streamer(clazz, jname);

            } else {
                // if we already support streaming a superclass of this object, use it instead
                // (e.g. a DictionaryMap gets deserialized on the server as a HashMap)
                for each (var upcast :Streamer in _byJName) {
                    if (ClassUtil.isAssignableAs(upcast._target, clazz)) {
                        streamer = new DelegatingStreamer(upcast, clazz, jname);
                        break;
                    }
                }
                if (streamer == null) {
                    return null;
                }
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
     * Returns the canonical class name used to identify this streamer. This is known as the java
     * class name and is typically the class sent to the server, but may in some cases be different.
     * @see com.threerings.io.streamers.DelegatingStreamer
     */
    public function getJavaClassName () :String
    {
        return _jname;
    }

    /**
     * Returns the class name used to deserialize the class represented by this streamer on the
     * server. This is typically the same as the java class name, but may in some cases be
     * different.
     * @see com.threerings.io.streamers.DelegatingStreamer
     */
    public function getUpstreamJavaClassName () :String
    {
        return _jname;
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

    public function toString () :String
    {
        return "[Streamer(" + _jname + ")]";
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

        for each (var c :Class in [ StringStreamer, NumberStreamer, ByteArrayStreamer ]) {
            registerStreamer(Streamer(new c()));
        }
        registerStreamer(ArrayStreamer.INSTANCE,
            "java.util.List", "java.util.ArrayList", "java.util.Collection");
        registerStreamer(SetStreamer.INSTANCE, "java.util.Set");
        registerStreamer(MapStreamer.INSTANCE, "java.util.Map");
    }

    protected var _target :Class;

    protected var _jname :String;

    protected static var _byJName :Dictionary;

    protected static const log :Log = Log.getLog(Streamer);
}
}
