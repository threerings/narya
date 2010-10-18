// GENERATED PREAMBLE START
//
// $Id$
//
// Narya library - tools for developing networked games
// Copyright (C) 2002-2010 Three Rings Design, Inc., All Rights Reserved
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

import com.threerings.io.ObjectInputStream;
import com.threerings.io.ObjectOutputStream;
import com.threerings.io.SimpleStreamableObject;
import com.threerings.util.Long;
import com.threerings.io.TypedArray;
import flash.utils.ByteArray;
import com.threerings.io.streamers.ArrayStreamer;
import com.threerings.util.StreamableArrayList;
import com.threerings.util.Map;
import com.threerings.io.streamers.MapStreamer;
import com.threerings.util.StreamableHashMap;
// GENERATED PREAMBLE END

import com.threerings.util.Integer;
import com.threerings.util.Maps;
import com.threerings.util.Util;

// GENERATED CLASSDECL START
public class ASStreamableSubset extends SimpleStreamableObject
{
// GENERATED CLASSDECL END

    public static function createWithJavaDefaults () :ASStreamableSubset
    {
        var sub :ASStreamableSubset = new ASStreamableSubset();
        sub.fillWithJavaDefaults();
        return sub;
    }

    public function fillWithJavaDefaults () :void
    {
        bool1 = true;
        short2 = 2;
        int3 = 3;
        long4 = Long.fromNumber(4);
        float5 = 5;
        double6 = 6;
        char7 = 7;
        byte8 = 8;
        string1 = "one";
        bools = TypedArray.create(Boolean, [true, false, true]);
        bytes = new ByteArray();
        bytes.writeByte(1);
        bytes.writeByte(2);
        bytes.writeByte(3);
        ints = TypedArray.create(int, [1, 2, 3]);
        strings = TypedArray.create(String, ["one", "two", "three"]);
        stringMap = Maps.newMapOf(String);
        stringMap.put("one", "1");
        stringMap.put("two", "2");
        stringMap.put("three", "3");
        stringIntMap = Maps.newMapOf(String);
        stringIntMap.put("one", Integer.valueOf(1));
        stringIntMap.put("two", Integer.valueOf(2));
        stringIntMap.put("three", Integer.valueOf(3));
    }

    public function equals (o :ASStreamableSubset) :Boolean
    {
        return bool1 === o.bool1 && short2 === o.short2 && int3 === o.int3 && long4.equals(o.long4) &&
            float5 === o.float5 && double6 === o.double6 && char7 === o.char7 &&
            byte8 === o.byte8 && string1 === o.string1 && nullString1 === o.nullString1 &&

            Util.equals(bools, o.bools) && Util.equals(bytes, o.bytes) &&
            Util.equals(ints, o.ints) && Util.equals(nullBools, o.nullBools) &&
            Util.equals(nullBytes, o.nullBytes) && Util.equals(nullInts, o.nullInts) &&

            Util.equals(strings, o.strings) && Util.equals(nullStrings, o.nullStrings) &&
            Util.equals(sal, o.sal) &&

            Maps.equals(stringMap, o.stringMap) && Maps.equals(stringIntMap, o.stringIntMap)
            Maps.equals(nullStringMap, o.nullStringMap) && Maps.equals(shm, o.shm);

    }

// GENERATED STREAMING START
    public var bool1 :Boolean;
    public var short2 :int;
    public var int3 :int;
    public var long4 :Long;
    public var float5 :Number;
    public var double6 :Number;
    public var char7 :int;
    public var byte8 :int;
    public var string1 :String;
    public var nullString1 :String;
    public var bools :TypedArray;
    public var bytes :ByteArray;
    public var ints :TypedArray;
    public var nullBools :TypedArray;
    public var nullBytes :ByteArray;
    public var nullInts :TypedArray;
    public var strings :TypedArray;
    public var nullStrings :TypedArray;
    public var sal :StreamableArrayList;
    public var stringMap :Map;
    public var stringIntMap :Map;
    public var nullStringMap :Map;
    public var shm :StreamableHashMap;

    override public function readObject (ins :ObjectInputStream) :void
    {
        super.readObject(ins);
        bool1 = ins.readBoolean();
        short2 = ins.readShort();
        int3 = ins.readInt();
        long4 = ins.readLong();
        float5 = ins.readFloat();
        double6 = ins.readDouble();
        char7 = ins.readShort();
        byte8 = ins.readByte();
        string1 = ins.readField(String);
        nullString1 = ins.readField(String);
        bools = ins.readField(TypedArray.getJavaType(Boolean));
        bytes = ins.readField(ByteArray);
        ints = ins.readField(TypedArray.getJavaType(int));
        nullBools = ins.readField(TypedArray.getJavaType(Boolean));
        nullBytes = ins.readField(ByteArray);
        nullInts = ins.readField(TypedArray.getJavaType(int));
        strings = ins.readField(ArrayStreamer.INSTANCE);
        nullStrings = ins.readField(ArrayStreamer.INSTANCE);
        sal = ins.readObject(StreamableArrayList);
        stringMap = ins.readField(MapStreamer.INSTANCE);
        stringIntMap = ins.readField(MapStreamer.INSTANCE);
        nullStringMap = ins.readField(MapStreamer.INSTANCE);
        shm = ins.readObject(StreamableHashMap);
    }
    
    override public function writeObject (out :ObjectOutputStream) :void
    {
        super.writeObject(out);
        out.writeBoolean(bool1);
        out.writeShort(short2);
        out.writeInt(int3);
        out.writeLong(long4);
        out.writeFloat(float5);
        out.writeDouble(double6);
        out.writeShort(char7);
        out.writeByte(byte8);
        out.writeField(string1);
        out.writeField(nullString1);
        out.writeField(bools);
        out.writeField(bytes);
        out.writeField(ints);
        out.writeField(nullBools);
        out.writeField(nullBytes);
        out.writeField(nullInts);
        out.writeField(strings, ArrayStreamer.INSTANCE);
        out.writeField(nullStrings, ArrayStreamer.INSTANCE);
        out.writeObject(sal);
        out.writeField(stringMap, MapStreamer.INSTANCE);
        out.writeField(stringIntMap, MapStreamer.INSTANCE);
        out.writeField(nullStringMap, MapStreamer.INSTANCE);
        out.writeObject(shm);
    }
// GENERATED STREAMING END

}
}
