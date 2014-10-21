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

package com.threerings.io;

import java.io.IOException;
import java.util.Arrays;
import java.util.Collection;
import java.util.Date;
import java.util.List;
import static com.threerings.io.StreamableTest.*; // for Wicket, Wacket, Wocket

import org.junit.Test;
import static org.junit.Assert.*;

import com.google.common.collect.Lists;
import com.samskivert.util.StringUtil;

/**
 * Ensures that we don't accidentally break the wire format for streamables.
 */
public class WireFormatTest
{
    public static class WireBlob extends SimpleStreamableObject
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

        @Intern public String internedString1 = "monkey butter";
        @Intern public String nullInternedString1;

        public Date date1 = new Date(42L);

        // note: must be a Streamable class
        public Class<?> class1 = Widget.class;
        public Class<?> nullClass1;

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

        public Wackable[] wackables = new Wackable[] { new Wacket("for"), new Wacket("sure") };

        // it's legal for an Object field to contain a Streamable reference
        public Object object1 = new Wocket();
        public Object nullObject1 = new Wocket();

        // it's legal for an Object[] field to contain an array of Streamable references; NOTE:
        // this will come back as "new Object[] { new Wocket(), new Wocket() }", so it's not kosher
        // to rely on the type of your array being preserved, only the type of its contents
        public Object[] objects = new Wocket[] { new Wocket(), new Wocket() };
        public Object[] nullObjects;

        public List<Integer> list = Lists.newArrayList(1, 2, 3);
        public List<Integer> nullList = null;

        public Collection<Integer> collection = Arrays.asList(4, 5, 6);
        public Collection<Integer> nullCollection = null;

        // NOTE: don't put sets or maps in here because they have non-predictable iteration order
        // which will break our wire format test
    }

    @Test
    public void testWireFormat ()
        throws IOException, ClassNotFoundException
    {
        WireBlob w = new WireBlob();

        // make sure that we serialize to the expected stream of bytes
        byte[] data = flatten(w);

        // uncomment this and rerun the tests to generate an updated WIRE_DATA blob
        // printWireData(w);

        // oddly, JUnit doesn't like comparing byte arrays directly (this fails:
        // assertEquals(WIRE_DATA, WIRE_DATA.clone())), but comparing strings is fine
        assertEquals(StringUtil.hexlate(data), StringUtil.hexlate(WIRE_DATA));
    }

    protected void printWireData (Object o)
        throws IOException
    {
        String dstr = StringUtil.wordWrap(StringUtil.hexlate(flatten(o)), 80);
        dstr = StringUtil.join(dstr.split("\n"), "\" +\n        \"");
        System.out.println("    protected static final byte[] WIRE_DATA = "
            + "StringUtil.unhexlate(\n        \"" + dstr + "\");");
    }

    protected static final byte[] WIRE_DATA = StringUtil.unhexlate(
        "ffff0029636f6d2e746872656572696e67732e696f2e57697265466f726d61745465737424576972" +
        "65426c6f62017f00617fff7fffffff7fffffffffffffff7f7fffff7fefffffffffffff0101017f01" +
        "0061017fff017fffffff017fffffffffffffff017f7fffff017fefffffffffffff00000000000000" +
        "000100036f6e6500ffff000d6d6f6e6b6579206275747465720000000000000000002a01fffe0027" +
        "636f6d2e746872656572696e67732e696f2e53747265616d61626c65546573742457696467657400" +
        "010000000301000101000000037f020301000000037fff0002000301000000030061006200630100" +
        "0000037fffffff000000020000000301000000037fffffffffffffff000000000000000200000000" +
        "0000000301000000037f7fffff400000004040000001000000037fefffffffffffff400000000000" +
        "000040080000000000000000000000000000fffd0027636f6d2e746872656572696e67732e696f2e" +
        "53747265616d61626c655465737424576f636b65740f7fff400921fb54442d18fffc002a5b4c636f" +
        "6d2e746872656572696e67732e696f2e53747265616d61626c655465737424576f636b65743b0000" +
        "000200030f7fff400921fb54442d1800030f7fff400921fb54442d18fffb002a5b4c636f6d2e7468" +
        "72656572696e67732e696f2e53747265616d61626c6554657374245769636b65743b000000030001" +
        "070f7fff400921fb54442d1800000013000000130f7fff400921fb54442d1800000013000000130f" +
        "7fff400921fb54442d180000001300000013000000000000fffa002c5b4c636f6d2e746872656572" +
        "696e67732e696f2e53747265616d61626c6554657374245761636b61626c653b00000002fff90027" +
        "636f6d2e746872656572696e67732e696f2e53747265616d61626c6554657374245761636b657401" +
        "0009666f722d697a7a6c65000701000a737572652d697a7a6c6500030f7fff400921fb54442d1800" +
        "030f7fff400921fb54442d18010000000200030f7fff400921fb54442d1800030f7fff400921fb54" +
        "442d18000100000003fff800116a6176612e6c616e672e496e746567657200000001000800000002" +
        "00080000000300010000000300080000000400080000000500080000000600");
}
