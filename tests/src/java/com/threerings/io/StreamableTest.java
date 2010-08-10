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

package com.threerings.io;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.junit.Test;
import static org.junit.Assert.*;

import com.google.common.base.Objects;
import com.google.common.collect.Lists;

import com.samskivert.util.StringUtil;
import com.samskivert.util.Tuple;
import com.threerings.util.StreamableTuple;

/**
 * Tests the {@link Streamable} class.
 */
public class StreamableTest
{
    public static class Widget extends SimpleStreamableObject
        implements Cloneable
    {
        public boolean bool1 = true;
        public byte byte1 = Byte.MAX_VALUE;
        public char char1 = 'a';
        public short short1 = Short.MAX_VALUE;
        public int int1 = Integer.MAX_VALUE;
        public long long1 = Long.MAX_VALUE;
        public float float1 = Float.MAX_VALUE;
        public double double1 = Double.MAX_VALUE;

        public Boolean boxedBool = true;
        public Byte boxedByte = Byte.MAX_VALUE;
        public Character boxedChar = 'a';
        public Short boxedShort = Short.MAX_VALUE;
        public Integer boxedInt = Integer.MAX_VALUE;
        public Long boxedLong = Long.MAX_VALUE;
        public Float boxedFloat = Float.MAX_VALUE;
        public Double boxedDouble = Double.MAX_VALUE;

        public Boolean nullBoxedBool;
        public Byte nullBoxedByte;
        public Character nullBoxedChar;
        public Short nullBoxedShort;
        public Integer nullBoxedInt;
        public Long nullBoxedLong;
        public Float nullBoxedFloat;
        public Double nullBoxedDouble;

        public String string1 = "one";
        public String nullString1;

        public boolean[] bools = new boolean[] { true, false, true };
        public byte[] bytes = new byte[] { Byte.MAX_VALUE, 2, 3 };
        public short[] shorts = new short[] { Short.MAX_VALUE, 2, 3 };
        public char[] chars = new char[] { 'a', 'b', 'c' };
        public int[] ints = new int[] { Integer.MAX_VALUE, 2, 3 };
        public long[] longs = new long[] { Long.MAX_VALUE, 2, 3 };
        public float[] floats = new float[] { Float.MAX_VALUE, 2, 3 };
        public double[] doubles = new double[] { Double.MAX_VALUE, 2, 3 };

        public boolean[] nullBools;
        public byte[] nullBytes;
        public short[] nullShorts;
        public char[] nullChars;
        public int[] nullInts;
        public long[] nullLongs;
        public float[] nullFloats;
        public double[] nullDoubles;

        public Wocket wocket1 = new Wocket();
        public Wocket[] wockets = new Wocket[] { new Wocket(), new Wocket() };
        public Wicket[] wickets = new Wicket[] { new Wicket(), new Wicket(), new Wicket() };

        public Wocket nullWocket1;
        public Wocket[] nullWockets;
        public Wicket[] nullWickets;

        public List<Integer> list = Lists.newArrayList(1, 2, 3);
        public List<Integer> nullList = null;
        public ArrayList<Integer> arrayList = Lists.newArrayList(3, 2, 1);
        public ArrayList<Integer> nullArrayList = null;

        @Override
        public boolean equals (Object other) {
            if (!(other instanceof Widget)) {
                return false;
            }
            Widget ow = (Widget)other;
            return bool1 == ow.bool1 &&
                byte1 == ow.byte1 &&
                char1 == ow.char1 &&
                short1 == ow.short1 &&
                int1 == ow.int1 &&
                long1 == ow.long1 &&
                float1 == ow.float1 &&
                double1 == ow.double1 &&

                Objects.equal(boxedBool, ow.boxedBool) &&
                Objects.equal(boxedByte, ow.boxedByte) &&
                Objects.equal(boxedShort, ow.boxedShort) &&
                Objects.equal(boxedChar, ow.boxedChar) &&
                Objects.equal(boxedInt, ow.boxedInt) &&
                Objects.equal(boxedLong, ow.boxedLong) &&
                Objects.equal(boxedFloat, ow.boxedFloat) &&
                Objects.equal(boxedDouble, ow.boxedDouble) &&

                Objects.equal(nullBoxedBool, ow.nullBoxedBool) &&
                Objects.equal(nullBoxedByte, ow.nullBoxedByte) &&
                Objects.equal(nullBoxedShort, ow.nullBoxedShort) &&
                Objects.equal(nullBoxedChar, ow.nullBoxedChar) &&
                Objects.equal(nullBoxedInt, ow.nullBoxedInt) &&
                Objects.equal(nullBoxedLong, ow.nullBoxedLong) &&
                Objects.equal(nullBoxedFloat, ow.nullBoxedFloat) &&
                Objects.equal(nullBoxedDouble, ow.nullBoxedDouble) &&

                Objects.equal(string1, ow.string1) &&
                Objects.equal(nullString1, ow.nullString1) &&

                Arrays.equals(bools, ow.bools) &&
                Arrays.equals(bytes, ow.bytes) &&
                Arrays.equals(shorts, ow.shorts) &&
                Arrays.equals(chars, ow.chars) &&
                Arrays.equals(ints, ow.ints) &&
                Arrays.equals(longs, ow.longs) &&
                Arrays.equals(floats, ow.floats) &&
                Arrays.equals(doubles, ow.doubles) &&

                Arrays.equals(nullBools, ow.nullBools) &&
                Arrays.equals(nullBytes, ow.nullBytes) &&
                Arrays.equals(nullShorts, ow.nullShorts) &&
                Arrays.equals(nullChars, ow.nullChars) &&
                Arrays.equals(nullInts, ow.nullInts) &&
                Arrays.equals(nullLongs, ow.nullLongs) &&
                Arrays.equals(nullFloats, ow.nullFloats) &&
                Arrays.equals(nullDoubles, ow.nullDoubles) &&

                Objects.equal(wocket1, ow.wocket1) &&
                Arrays.equals(wockets, ow.wockets) &&
                Arrays.equals(wickets, ow.wickets) &&

                Objects.equal(nullWocket1, ow.nullWocket1) &&
                Arrays.equals(nullWockets, ow.nullWockets) &&
                Arrays.equals(nullWickets, ow.nullWickets) &&

                Objects.equal(list, ow.list) &&
                Objects.equal(nullList, ow.nullList) &&
                Objects.equal(arrayList, ow.arrayList) &&
                Objects.equal(nullArrayList, ow.nullArrayList);
        }

        @Override
        public Widget clone ()
        {
            try {
                return (Widget)super.clone();
            } catch (CloneNotSupportedException cnse) {
                throw new AssertionError(cnse);
            }
        }
    }

    public static class Wocket extends SimpleStreamableObject
    {
        public byte bizyte = 15;
        public short shizort = Short.MAX_VALUE;
        public double dizouble = Math.PI;

        @Override
        public boolean equals (Object other) {
            if (!(other instanceof Wocket)) {
                return false;
            }
            Wocket ow = (Wocket)other;
            return bizyte == ow.bizyte &&
                shizort == ow.shizort &&
                dizouble == ow.dizouble;
        }
    }

    public static final class Wicket extends SimpleStreamableObject
    {
        public byte bizyte = 15;
        public short shizort = Short.MAX_VALUE;
        public double dizouble = Math.PI;

        public void writeObject (ObjectOutputStream out)
            throws IOException
        {
            out.defaultWriteObject();
            out.writeInt(_fizzle);
        }

        public void readObject (ObjectInputStream in)
            throws IOException, ClassNotFoundException
        {
            in.defaultReadObject();
            _fizzle = in.readInt();
        }

        @Override
        public boolean equals (Object other) {
            if (!(other instanceof Wicket)) {
                return false;
            }
            Wicket ow = (Wicket)other;
            return bizyte == ow.bizyte &&
                shizort == ow.shizort &&
                dizouble == ow.dizouble &&
                _fizzle == ow._fizzle;
        }

        @Override
        protected void toString (StringBuilder buf)
        {
            super.toString(buf);
            buf.append(", fizzle=").append(_fizzle);
        }

        protected int _fizzle = 19;
    }

    @Test
    public void testFields ()
        throws IOException, ClassNotFoundException
    {
        Widget w = new Widget();
        ByteArrayOutputStream bout = new ByteArrayOutputStream();
        ObjectOutputStream oout = new ObjectOutputStream(bout);
        oout.writeObject(w);
        ObjectInputStream oin = new ObjectInputStream(
            new ByteArrayInputStream(bout.toByteArray()));
        assertEquals(w, oin.readObject());
    }

    @Test
    public void testWireFormat ()
        throws IOException, ClassNotFoundException
    {
        Widget w = new Widget();

        // make sure that we serialize to the expected stream of bytes
        ByteArrayOutputStream bout = new ByteArrayOutputStream();
        ObjectOutputStream oout = new ObjectOutputStream(bout);
        oout.writeObject(w);
        byte[] data = bout.toByteArray();

        // if you add fields to Widget, uncomment this and rerun the tests to generate an updated
        // WIRE_DATA blob
        // System.out.println("protected static final byte[] WIRE_DATA = " +
        //                    StringUtil.toString(data, "{", "}") + ";");

        // oddly, JUnit doesn't like comparing byte arrays directly (this fails:
        // assertEquals(WIRE_DATA, WIRE_DATA.clone())), but comparing strings is fine
        assertEquals(StringUtil.toString(data), StringUtil.toString(WIRE_DATA));

        // make sure that we unserialize a known stream of bytes to the expected object
        ObjectInputStream oin = new ObjectInputStream(new ByteArrayInputStream(WIRE_DATA));
        assertEquals(w, oin.readObject());
    }

    @Test
    public void testPostStreamingMutation ()
        throws IOException, ClassNotFoundException
    {
        // create an object graph to be streamed
        Widget w = new Widget();

        ByteArrayOutputStream bout = new ByteArrayOutputStream();
        ObjectOutputStream oout = new ObjectOutputStream(bout);
        Widget w1 = w.clone();
        oout.writeObject(w);
        w.string1 = "two";
        Widget w2 = w.clone();
        oout.writeObject(w);
        w.string1 = "three";
        Widget w3 = w.clone();
        oout.writeObject(w);
        Tuple<String,String> tup = StreamableTuple.newTuple("left", "right");
        oout.writeObject(tup);

        byte[] data = bout.toByteArray();
        // System.out.println(data.length + " bytes were written.");

        ByteArrayInputStream bin = new ByteArrayInputStream(data);
        ObjectInputStream oin = new ObjectInputStream(bin);
        Object ow1 = oin.readObject(); // widget
        Object ow2 = oin.readObject(); // modified widget
        Object ow3 = oin.readObject(); // again modified widget
        Object otup = oin.readObject(); // streamable tuple

        assertEquals(w1, ow1);
        assertEquals(w2, ow2);
        assertEquals(w3, ow3);
        assertEquals(tup, otup);
    }

    protected static final byte[] WIRE_DATA = {-1, -1, 0, 39, 99, 111, 109, 46, 116, 104, 114, 101, 101, 114, 105, 110, 103, 115, 46, 105, 111, 46, 83, 116, 114, 101, 97, 109, 97, 98, 108, 101, 84, 101, 115, 116, 36, 87, 105, 100, 103, 101, 116, 1, 127, 0, 97, 127, -1, 127, -1, -1, -1, 127, -1, -1, -1, -1, -1, -1, -1, 127, 127, -1, -1, 127, -17, -1, -1, -1, -1, -1, -1, 1, 1, 1, 127, 1, 0, 97, 1, 127, -1, 1, 127, -1, -1, -1, 1, 127, -1, -1, -1, -1, -1, -1, -1, 1, 127, 127, -1, -1, 1, 127, -17, -1, -1, -1, -1, -1, -1, 0, 0, 0, 0, 0, 0, 0, 0, 1, 0, 3, 111, 110, 101, 0, 1, 0, 0, 0, 3, 1, 0, 1, 1, 0, 0, 0, 3, 127, 2, 3, 1, 0, 0, 0, 3, 127, -1, 0, 2, 0, 3, 1, 0, 0, 0, 3, 0, 97, 0, 98, 0, 99, 1, 0, 0, 0, 3, 127, -1, -1, -1, 0, 0, 0, 2, 0, 0, 0, 3, 1, 0, 0, 0, 3, 127, -1, -1, -1, -1, -1, -1, -1, 0, 0, 0, 0, 0, 0, 0, 2, 0, 0, 0, 0, 0, 0, 0, 3, 1, 0, 0, 0, 3, 127, 127, -1, -1, 64, 0, 0, 0, 64, 64, 0, 0, 1, 0, 0, 0, 3, 127, -17, -1, -1, -1, -1, -1, -1, 64, 0, 0, 0, 0, 0, 0, 0, 64, 8, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, -1, -2, 0, 39, 99, 111, 109, 46, 116, 104, 114, 101, 101, 114, 105, 110, 103, 115, 46, 105, 111, 46, 83, 116, 114, 101, 97, 109, 97, 98, 108, 101, 84, 101, 115, 116, 36, 87, 111, 99, 107, 101, 116, 15, 127, -1, 64, 9, 33, -5, 84, 68, 45, 24, -1, -3, 0, 42, 91, 76, 99, 111, 109, 46, 116, 104, 114, 101, 101, 114, 105, 110, 103, 115, 46, 105, 111, 46, 83, 116, 114, 101, 97, 109, 97, 98, 108, 101, 84, 101, 115, 116, 36, 87, 111, 99, 107, 101, 116, 59, 0, 0, 0, 2, 0, 2, 15, 127, -1, 64, 9, 33, -5, 84, 68, 45, 24, 0, 2, 15, 127, -1, 64, 9, 33, -5, 84, 68, 45, 24, -1, -4, 0, 42, 91, 76, 99, 111, 109, 46, 116, 104, 114, 101, 101, 114, 105, 110, 103, 115, 46, 105, 111, 46, 83, 116, 114, 101, 97, 109, 97, 98, 108, 101, 84, 101, 115, 116, 36, 87, 105, 99, 107, 101, 116, 59, 0, 0, 0, 3, 0, 1, 7, 15, 127, -1, 64, 9, 33, -5, 84, 68, 45, 24, 0, 0, 0, 19, 0, 0, 0, 19, 15, 127, -1, 64, 9, 33, -5, 84, 68, 45, 24, 0, 0, 0, 19, 0, 0, 0, 19, 15, 127, -1, 64, 9, 33, -5, 84, 68, 45, 24, 0, 0, 0, 19, 0, 0, 0, 19, 0, 0, 0, 0, 0, 0, -1, -5, 0, 19, 106, 97, 118, 97, 46, 117, 116, 105, 108, 46, 65, 114, 114, 97, 121, 76, 105, 115, 116, 0, 0, 0, 3, -1, -6, 0, 17, 106, 97, 118, 97, 46, 108, 97, 110, 103, 46, 73, 110, 116, 101, 103, 101, 114, 0, 0, 0, 1, 0, 6, 0, 0, 0, 2, 0, 6, 0, 0, 0, 3, 0, 0, 1, 0, 0, 0, 3, 0, 6, 0, 0, 0, 3, 0, 6, 0, 0, 0, 2, 0, 6, 0, 0, 0, 1, 0};
}
