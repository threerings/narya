//
// $Id: StreamableTest.java,v 1.2 2004/08/27 02:20:57 mdb Exp $
//
// Narya library - tools for developing networked games
// Copyright (C) 2002-2004 Three Rings Design, Inc., All Rights Reserved
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

import junit.framework.Test;
import junit.framework.TestCase;

/**
 * Tests the {@link Streamable} class.
 */
public class StreamableTest extends TestCase
{
    public static class Widget extends SimpleStreamableObject
    {
        public boolean bool1 = true;
        public byte byte1 = Byte.MAX_VALUE;
        public char char1 = 'a';
        public short short1 = Short.MAX_VALUE;
        public int int1 = Integer.MAX_VALUE;
        public long long1 = Long.MAX_VALUE;
        public float float1 = Float.MAX_VALUE;
        public double double1 = Double.MAX_VALUE;
        public String string1 = "one";

        public boolean[] bools = new boolean[] { true, false, true };
        public byte[] bytes = new byte[] { Byte.MAX_VALUE, 2, 3 };
        public short[] shorts = new short[] { Short.MAX_VALUE, 2, 3 };
        public char[] chars = new char[] { 'a', 'b', 'c' };
        public int[] ints = new int[] { Integer.MAX_VALUE, 2, 3 };
        public long[] longs = new long[] { Long.MAX_VALUE, 2, 3 };
        public float[] floats = new float[] { Float.MAX_VALUE, 2, 3 };
        public double[] doubles = new double[] { Double.MAX_VALUE, 2, 3 };

        public Wocket wocket1 = new Wocket();
        public Wocket[] wockets = new Wocket[] { new Wocket(), new Wocket() };
        public Wicket[] wickets = new Wicket[] {
            new Wicket(), new Wicket(), new Wicket() };
    }

    public static class Wocket extends SimpleStreamableObject
    {
        public byte bizyte = 15;
        public short shizort = Short.MAX_VALUE;
        public double dizouble = Math.PI;
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

        protected void toString (StringBuffer buf)
        {
            super.toString(buf);
            buf.append(", fizzle=").append(_fizzle);
        }

        protected int _fizzle = 19;
    }

    public StreamableTest ()
    {
        super(StreamableTest.class.getName());
    }

    public void runTest ()
    {
        try {
            // create an object graph to be streamed
            Widget w = new Widget();

            ByteArrayOutputStream bout = new ByteArrayOutputStream();
            ObjectOutputStream oout = new ObjectOutputStream(bout);
            oout.writeObject(w);
            w.string1 = "two";
            oout.writeObject(w);
            w.string1 = "three";
            oout.writeObject(w);

            byte[] data = bout.toByteArray();
//             System.out.println(data.length + " bytes were written.");

            ByteArrayInputStream bin = new ByteArrayInputStream(data);
            ObjectInputStream oin = new ObjectInputStream(bin);
//             System.out.println(oin.readObject());
//             System.out.println(oin.readObject());
//             System.out.println(oin.readObject());
            oin.readObject();
            oin.readObject();
            oin.readObject();

        } catch (Exception e) {
            e.printStackTrace();
            fail("Urk " + e);
        }
    }

    public static Test suite ()
    {
        return new StreamableTest();
    }

    public static void main (String[] args)
    {
        StreamableTest test = new StreamableTest();
        test.runTest();
    }
}
