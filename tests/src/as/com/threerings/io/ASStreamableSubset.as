// GENERATED PREAMBLE START
//
// $Id$
//
// Narya library - tools for developing networked games
// Copyright (C) 2002-2010 Three Rings Design, Inc., All Rights Reserved
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

import com.threerings.io.ObjectInputStream;
import com.threerings.io.ObjectOutputStream;
import com.threerings.io.SimpleStreamableObject;
import com.threerings.util.Long;
import com.threerings.io.TypedArray;
import flash.utils.ByteArray;
// GENERATED PREAMBLE END

import com.threerings.util.Util;

// GENERATED CLASSDECL START
public class ASStreamableSubset extends SimpleStreamableObject
{
    public function ASStreamableSubset ()
    {}
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
    }

    public function equals (o :ASStreamableSubset) :Boolean
    {
        return bool1 === o.bool1 && short2 === o.short2 && int3 === o.int3 && long4.equals(o.long4) &&
            float5 === o.float5 && double6 === o.double6 && char7 === o.char7 &&
            byte8 === o.byte8 && string1 === o.string1 && nullString1 === o.nullString1 &&
            Util.equals(bools, o.bools) && Util.equals(bytes, o.bytes) &&
            Util.equals(ints, o.ints) && Util.equals(nullBools, o.nullBools) &&
            Util.equals(nullBytes, o.nullBytes) && Util.equals(nullInts, o.nullInts);

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
    }
// GENERATED STREAMING END

}
}
