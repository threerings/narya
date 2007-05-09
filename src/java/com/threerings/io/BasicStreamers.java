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

package com.threerings.io;

import java.io.EOFException;
import java.io.IOException;

import java.util.ArrayList;
import java.util.List;
import java.util.RandomAccess;

/**
 * Code to read and write basic object types (like arrays of primitives, {@link Integer} instances,
 * {@link Double} instances, etc.).
 */
public class BasicStreamers
{
    /** An array of types for all of the basic streamers. */
    public static Class[] BSTREAMER_TYPES = {
        Boolean.class,
        Byte.class,
        Short.class,
        Character.class,
        Integer.class,
        Long.class,
        Float.class,
        Double.class,
        String.class,
        boolean[].class,
        byte[].class,
        short[].class,
        char[].class,
        int[].class,
        long[].class,
        float[].class,
        double[].class,
        Object[].class,
        List.class,
        ArrayList.class,
    };

    /** An array of instances of all of the basic streamers. */
    public static Streamer[] BSTREAMER_INSTANCES = {
        new BooleanStreamer(),
        new ByteStreamer(),
        new ShortStreamer(),
        new CharacterStreamer(),
        new IntegerStreamer(),
        new LongStreamer(),
        new FloatStreamer(),
        new DoubleStreamer(),
        new StringStreamer(),
        new BooleanArrayStreamer(),
        new ByteArrayStreamer(),
        new ShortArrayStreamer(),
        new CharArrayStreamer(),
        new IntArrayStreamer(),
        new LongArrayStreamer(),
        new FloatArrayStreamer(),
        new DoubleArrayStreamer(),
        new ObjectArrayStreamer(),
        new ListStreamer(),
        new ListStreamer(),
    };

    /** Streams {@link Boolean} instances. */
    public static class BooleanStreamer extends BasicStreamer
    {
        // documentation inherited
        public Object createObject (ObjectInputStream in)
            throws IOException
        {
            return Boolean.valueOf(in.readBoolean());
        }

        // documentation inherited
        public void writeObject (Object object, ObjectOutputStream out, boolean useWriter)
            throws IOException
        {
            out.writeBoolean(((Boolean)object).booleanValue());
        }
    }

    /** Streams {@link Byte} instances. */
    public static class ByteStreamer extends BasicStreamer
    {
        // documentation inherited
        public Object createObject (ObjectInputStream in)
            throws IOException
        {
            return Byte.valueOf(in.readByte());
        }

        // documentation inherited
        public void writeObject (Object object, ObjectOutputStream out, boolean useWriter)
            throws IOException
        {
            out.writeByte(((Byte)object).byteValue());
        }
    }

    /** Streams {@link Short} instances. */
    public static class ShortStreamer extends BasicStreamer
    {
        // documentation inherited
        public Object createObject (ObjectInputStream in)
            throws IOException
        {
            return Short.valueOf(in.readShort());
        }

        // documentation inherited
        public void writeObject (Object object, ObjectOutputStream out, boolean useWriter)
            throws IOException
        {
            out.writeShort(((Short)object).shortValue());
        }
    }

    /** Streams {@link Character} instances. */
    public static class CharacterStreamer extends BasicStreamer
    {
        // documentation inherited
        public Object createObject (ObjectInputStream in)throws IOException
        {
            return Character.valueOf(in.readChar());
        }

        // documentation inherited
        public void writeObject (Object object, ObjectOutputStream out, boolean useWriter)
            throws IOException
        {
            out.writeChar(((Character)object).charValue());
        }
    }

    /** Streams {@link Integer} instances. */
    public static class IntegerStreamer extends BasicStreamer
    {
        // documentation inherited
        public Object createObject (ObjectInputStream in)
            throws IOException
        {
            return Integer.valueOf(in.readInt());
        }

        // documentation inherited
        public void writeObject (Object object, ObjectOutputStream out, boolean useWriter)
            throws IOException
        {
            out.writeInt(((Integer)object).intValue());
        }
    }

    /** Streams {@link Long} instances. */
    public static class LongStreamer extends BasicStreamer
    {
        // documentation inherited
        public Object createObject (ObjectInputStream in)
            throws IOException
        {
            return Long.valueOf(in.readLong());
        }

        // documentation inherited
        public void writeObject (Object object, ObjectOutputStream out, boolean useWriter)
            throws IOException
        {
            out.writeLong(((Long)object).longValue());
        }
    }

    /** Streams {@link Float} instances. */
    public static class FloatStreamer extends BasicStreamer
    {
        // documentation inherited
        public Object createObject (ObjectInputStream in)
            throws IOException
        {
            return Float.valueOf(in.readFloat());
        }

        // documentation inherited
        public void writeObject (Object object, ObjectOutputStream out, boolean useWriter)
            throws IOException
        {
            out.writeFloat(((Float)object).floatValue());
        }
    }

    /** Streams {@link Double} instances. */
    public static class DoubleStreamer extends BasicStreamer
    {
        // documentation inherited
        public Object createObject (ObjectInputStream in)
            throws IOException
        {
            return Double.valueOf(in.readDouble());
        }

        // documentation inherited
        public void writeObject (Object object, ObjectOutputStream out, boolean useWriter)
            throws IOException
        {
            out.writeDouble(((Double)object).doubleValue());
        }
    }

    /** Streams {@link String} instances. */
    public static class StringStreamer extends BasicStreamer
    {
        // documentation inherited
        public Object createObject (ObjectInputStream in)
            throws IOException
        {
            return in.readUTF();
        }

        // documentation inherited
        public void writeObject (Object object, ObjectOutputStream out, boolean useWriter)
            throws IOException
        {
            out.writeUTF((String)object);
        }
    }

    /** Streams arrays of booleans. */
    public static class BooleanArrayStreamer extends BasicStreamer
    {
        // documentation inherited
        public Object createObject (ObjectInputStream in)
            throws IOException
        {
            return readBooleanArray(in);
        }

        // documentation inherited
        public void writeObject (Object object, ObjectOutputStream out, boolean useWriter)
            throws IOException
        {
            writeBooleanArray(out, (boolean[])object);
        }
    }

    /** Streams arrays of bytes. */
    public static class ByteArrayStreamer extends BasicStreamer
    {
        // documentation inherited
        public Object createObject (ObjectInputStream in)
            throws IOException
        {
            return readByteArray(in);
        }

        // documentation inherited
        public void writeObject (Object object, ObjectOutputStream out, boolean useWriter)
            throws IOException
        {
            writeByteArray(out, (byte[])object);
        }
    }

    /** Streams arrays of shorts. */
    public static class ShortArrayStreamer extends BasicStreamer
    {
        // documentation inherited
        public Object createObject (ObjectInputStream in)
            throws IOException
        {
            return readShortArray(in);
        }

        // documentation inherited
        public void writeObject (Object object, ObjectOutputStream out, boolean useWriter)
            throws IOException
        {
            writeShortArray(out, (short[])object);
        }
    }

    /** Streams arrays of chars. */
    public static class CharArrayStreamer extends BasicStreamer
    {
        // documentation inherited
        public Object createObject (ObjectInputStream in)
            throws IOException
        {
            return readCharArray(in);
        }

        // documentation inherited
        public void writeObject (Object object, ObjectOutputStream out, boolean useWriter)
            throws IOException
        {
            writeCharArray(out, (char[])object);
        }
    }

    /** Streams arrays of ints. */
    public static class IntArrayStreamer extends BasicStreamer
    {
        // documentation inherited
        public Object createObject (ObjectInputStream in)
            throws IOException
        {
            return readIntArray(in);
        }

        // documentation inherited
        public void writeObject (Object object, ObjectOutputStream out, boolean useWriter)
            throws IOException
        {
            writeIntArray(out, (int[])object);
        }
    }

    /** Streams arrays of longs. */
    public static class LongArrayStreamer extends BasicStreamer
    {
        // documentation inherited
        public Object createObject (ObjectInputStream in)
            throws IOException
        {
            return readLongArray(in);
        }

        // documentation inherited
        public void writeObject (Object object, ObjectOutputStream out, boolean useWriter)
            throws IOException
        {
            writeLongArray(out, (long[])object);
        }
    }

    /** Streams arrays of floats. */
    public static class FloatArrayStreamer extends BasicStreamer
    {
        // documentation inherited
        public Object createObject (ObjectInputStream in)
            throws IOException
        {
            return readFloatArray(in);
        }

        // documentation inherited
        public void writeObject (Object object, ObjectOutputStream out, boolean useWriter)
            throws IOException
        {
            writeFloatArray(out, (float[])object);
        }
    }

    /** Streams arrays of doubles. */
    public static class DoubleArrayStreamer extends BasicStreamer
    {
        // documentation inherited
        public Object createObject (ObjectInputStream in)
            throws IOException
        {
            return readDoubleArray(in);
        }

        // documentation inherited
        public void writeObject (Object object, ObjectOutputStream out, boolean useWriter)
            throws IOException
        {
            writeDoubleArray(out, (double[])object);
        }
    }

    /** Streams arrays of Object instances. */
    public static class ObjectArrayStreamer extends BasicStreamer
    {
        // documentation inherited
        public Object createObject (ObjectInputStream in)
            throws IOException, ClassNotFoundException
        {
            return readObjectArray(in);
        }

        // documentation inherited
        public void writeObject (Object object, ObjectOutputStream out, boolean useWriter)
            throws IOException
        {
            writeObjectArray(out, (Object[])object);
        }
    }

    /** Streams {@link List} instances. */
    public static class ListStreamer extends BasicStreamer
    {
        // documentation inherited
        public Object createObject (ObjectInputStream in)
            throws IOException, ClassNotFoundException
        {
            return readList(in);
        }

        // documentation inherited
        public void writeObject (Object object, ObjectOutputStream out, boolean useWriter)
            throws IOException
        {
            writeList(out, (List)object);
        }
    }

    /** Streams {@link String} instances. */
    public static class BasicStreamer extends Streamer
    {
        public void readObject (Object object, ObjectInputStream in, boolean useReader)
            throws IOException, ClassNotFoundException
        {
            // nothing to do here
        }
    }

    public static boolean[] readBooleanArray (ObjectInputStream ins)
        throws IOException
    {
        boolean[] value = new boolean[ins.readInt()];
        int ecount = value.length;
        for (int ii = 0; ii < ecount; ii++) {
            value[ii] = ins.readBoolean();
        }
        return value;
    }

    public static byte[] readByteArray (ObjectInputStream ins)
        throws IOException
    {
        byte[] value = new byte[ins.readInt()];
        int remain = value.length, offset = 0, read;
        while (remain > 0) {
            if ((read = ins.read(value, offset, remain)) > 0) {
                remain -= read;
                offset += read;
            } else {
                throw new EOFException();
            }
        }
        return value;
    }

    public static short[] readShortArray (ObjectInputStream ins)
        throws IOException
    {
        short[] value = new short[ins.readInt()];
        int ecount = value.length;
        for (int ii = 0; ii < ecount; ii++) {
            value[ii] = ins.readShort();
        }
        return value;
    }

    public static char[] readCharArray (ObjectInputStream ins)
        throws IOException
    {
        char[] value = new char[ins.readInt()];
        int ecount = value.length;
        for (int ii = 0; ii < ecount; ii++) {
            value[ii] = ins.readChar();
        }
        return value;
    }

    public static int[] readIntArray (ObjectInputStream ins)
        throws IOException
    {
        int[] value = new int[ins.readInt()];
        int ecount = value.length;
        for (int ii = 0; ii < ecount; ii++) {
            value[ii] = ins.readInt();
        }
        return value;
    }

    public static long[] readLongArray (ObjectInputStream ins)
        throws IOException
    {
        long[] value = new long[ins.readInt()];
        int ecount = value.length;
        for (int ii = 0; ii < ecount; ii++) {
            value[ii] = ins.readLong();
        }
        return value;
    }

    public static float[] readFloatArray (ObjectInputStream ins)
        throws IOException
    {
        float[] value = new float[ins.readInt()];
        int ecount = value.length;
        for (int ii = 0; ii < ecount; ii++) {
            value[ii] = ins.readFloat();
        }
        return value;
    }

    public static double[] readDoubleArray (ObjectInputStream ins)
        throws IOException
    {
        double[] value = new double[ins.readInt()];
        int ecount = value.length;
        for (int ii = 0; ii < ecount; ii++) {
            value[ii] = ins.readDouble();
        }
        return value;
    }

    public static Object[] readObjectArray (ObjectInputStream ins)
        throws IOException, ClassNotFoundException
    {
        Object[] value = new Object[ins.readInt()];
        int ecount = value.length;
        for (int ii = 0; ii < ecount; ii++) {
            value[ii] = ins.readObject();
        }
        return value;
    }

    public static ArrayList readList (ObjectInputStream ins)
        throws IOException, ClassNotFoundException
    {
        int ecount = ins.readInt();
        ArrayList<Object> list = new ArrayList<Object>(ecount);
        for (int ii = 0; ii < ecount; ii++) {
            list.add(ins.readObject());
        }
        return list;
    }

    public static void writeBooleanArray (ObjectOutputStream out, boolean[] value)
        throws IOException
    {
        int ecount = value.length;
        out.writeInt(ecount);
        for (int ii = 0; ii < ecount; ii++) {
            out.writeBoolean(value[ii]);
        }
    }

    public static void writeByteArray (ObjectOutputStream out, byte[] value)
        throws IOException
    {
        int ecount = value.length;
        out.writeInt(ecount);
        out.write(value);
    }

    public static void writeCharArray (ObjectOutputStream out, char[] value)
        throws IOException
    {
        int ecount = value.length;
        out.writeInt(ecount);
        for (int ii = 0; ii < ecount; ii++) {
            out.writeChar(value[ii]);
        }
    }

    public static void writeShortArray (ObjectOutputStream out, short[] value)
        throws IOException
    {
        int ecount = value.length;
        out.writeInt(ecount);
        for (int ii = 0; ii < ecount; ii++) {
            out.writeShort(value[ii]);
        }
    }

    public static void writeIntArray (ObjectOutputStream out, int[] value)
        throws IOException
    {
        int ecount = value.length;
        out.writeInt(ecount);
        for (int ii = 0; ii < ecount; ii++) {
            out.writeInt(value[ii]);
        }
    }

    public static void writeLongArray (ObjectOutputStream out, long[] value)
        throws IOException
    {
        int ecount = value.length;
        out.writeInt(ecount);
        for (int ii = 0; ii < ecount; ii++) {
            out.writeLong(value[ii]);
        }
    }

    public static void writeFloatArray (ObjectOutputStream out, float[] value)
        throws IOException
    {
        int ecount = value.length;
        out.writeInt(ecount);
        for (int ii = 0; ii < ecount; ii++) {
            out.writeFloat(value[ii]);
        }
    }

    public static void writeDoubleArray (ObjectOutputStream out, double[] value)
        throws IOException
    {
        int ecount = value.length;
        out.writeInt(ecount);
        for (int ii = 0; ii < ecount; ii++) {
            out.writeDouble(value[ii]);
        }
    }

    public static void writeObjectArray (ObjectOutputStream out, Object[] value)
        throws IOException
    {
        int ecount = value.length;
        out.writeInt(ecount);
        for (int ii = 0; ii < ecount; ii++) {
            out.writeObject(value[ii]);
        }
    }

    public static void writeList (ObjectOutputStream out, List value)
        throws IOException
    {
        int ecount = value.size();
        out.writeInt(ecount);
        if (value instanceof RandomAccess) {
            // if RandomAccess, it's faster and less garbagey this way
            for (int ii = 0; ii < ecount; ii++) {
                out.writeObject(value.get(ii));
            }
        } else {
            // if not RandomAccess, go ahead and make an Iterator...
            for (Object o : value) {
                out.writeObject(o);
            }
        }
    }
}
