//
// $Id$
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

import java.io.EOFException;
import java.io.IOException;

/**
 * Code to read and write basic object types (like arrays of primitives,
 * {@link Integer} instances, {@link Double} instances, etc.).
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
    };

    /** Streams {@link Boolean} instances. */
    public static class BooleanStreamer extends Streamer
    {
        // documentation inherited
        public Object createObject (ObjectInputStream in)
            throws IOException
        {
            return Boolean.valueOf(in.readBoolean());
        }

        // documentation inherited
        public void writeObject (
            Object object, ObjectOutputStream out, boolean useWriter)
            throws IOException
        {
            out.writeBoolean(((Boolean)object).booleanValue());
        }

        // documentation inherited
        public void readObject (
            Object object, ObjectInputStream in, boolean useReader)
            throws IOException, ClassNotFoundException
        {
            // nothing to do here
        }
    }

    /** Streams {@link Byte} instances. */
    public static class ByteStreamer extends Streamer
    {
        // documentation inherited
        public Object createObject (ObjectInputStream in)
            throws IOException
        {
            return Byte.valueOf(in.readByte());
        }

        // documentation inherited
        public void writeObject (
            Object object, ObjectOutputStream out, boolean useWriter)
            throws IOException
        {
            out.writeByte(((Byte)object).byteValue());
        }

        // documentation inherited
        public void readObject (
            Object object, ObjectInputStream in, boolean useReader)
            throws IOException, ClassNotFoundException
        {
            // nothing to do here
        }
    }

    /** Streams {@link Short} instances. */
    public static class ShortStreamer extends Streamer
    {
        // documentation inherited
        public Object createObject (ObjectInputStream in)
            throws IOException
        {
            return Short.valueOf(in.readShort());
        }

        // documentation inherited
        public void writeObject (
            Object object, ObjectOutputStream out, boolean useWriter)
            throws IOException
        {
            out.writeShort(((Short)object).shortValue());
        }

        // documentation inherited
        public void readObject (
            Object object, ObjectInputStream in, boolean useReader)
            throws IOException, ClassNotFoundException
        {
            // nothing to do here
        }
    }

    /** Streams {@link Character} instances. */
    public static class CharacterStreamer extends Streamer
    {
        // documentation inherited
        public Object createObject (ObjectInputStream in)
            throws IOException
        {
            return Character.valueOf(in.readChar());
        }

        // documentation inherited
        public void writeObject (
            Object object, ObjectOutputStream out, boolean useWriter)
            throws IOException
        {
            out.writeChar(((Character)object).charValue());
        }

        // documentation inherited
        public void readObject (
            Object object, ObjectInputStream in, boolean useReader)
            throws IOException, ClassNotFoundException
        {
            // nothing to do here
        }
    }

    /** Streams {@link Integer} instances. */
    public static class IntegerStreamer extends Streamer
    {
        // documentation inherited
        public Object createObject (ObjectInputStream in)
            throws IOException
        {
            return Integer.valueOf(in.readInt());
        }

        // documentation inherited
        public void writeObject (
            Object object, ObjectOutputStream out, boolean useWriter)
            throws IOException
        {
            out.writeInt(((Integer)object).intValue());
        }

        // documentation inherited
        public void readObject (
            Object object, ObjectInputStream in, boolean useReader)
            throws IOException, ClassNotFoundException
        {
            // nothing to do here
        }
    }

    /** Streams {@link Long} instances. */
    public static class LongStreamer extends Streamer
    {
        // documentation inherited
        public Object createObject (ObjectInputStream in)
            throws IOException
        {
            return Long.valueOf(in.readLong());
        }

        // documentation inherited
        public void writeObject (
            Object object, ObjectOutputStream out, boolean useWriter)
            throws IOException
        {
            out.writeLong(((Long)object).longValue());
        }

        // documentation inherited
        public void readObject (
            Object object, ObjectInputStream in, boolean useReader)
            throws IOException, ClassNotFoundException
        {
            // nothing to do here
        }
    }

    /** Streams {@link Float} instances. */
    public static class FloatStreamer extends Streamer
    {
        // documentation inherited
        public Object createObject (ObjectInputStream in)
            throws IOException
        {
            return Float.valueOf(in.readFloat());
        }

        // documentation inherited
        public void writeObject (
            Object object, ObjectOutputStream out, boolean useWriter)
            throws IOException
        {
            out.writeFloat(((Float)object).floatValue());
        }

        // documentation inherited
        public void readObject (
            Object object, ObjectInputStream in, boolean useReader)
            throws IOException, ClassNotFoundException
        {
            // nothing to do here
        }
    }

    /** Streams {@link Double} instances. */
    public static class DoubleStreamer extends Streamer
    {
        // documentation inherited
        public Object createObject (ObjectInputStream in)
            throws IOException
        {
            return Double.valueOf(in.readDouble());
        }

        // documentation inherited
        public void writeObject (
            Object object, ObjectOutputStream out, boolean useWriter)
            throws IOException
        {
            out.writeDouble(((Double)object).doubleValue());
        }

        // documentation inherited
        public void readObject (
            Object object, ObjectInputStream in, boolean useReader)
            throws IOException, ClassNotFoundException
        {
            // nothing to do here
        }
    }

    /** Streams {@link String} instances. */
    public static class StringStreamer extends Streamer
    {
        // documentation inherited
        public Object createObject (ObjectInputStream in)
            throws IOException
        {
            return in.readUTF();
        }

        // documentation inherited
        public void writeObject (
            Object object, ObjectOutputStream out, boolean useWriter)
            throws IOException
        {
            out.writeUTF((String)object);
        }

        // documentation inherited
        public void readObject (
            Object object, ObjectInputStream in, boolean useReader)
            throws IOException, ClassNotFoundException
        {
            // nothing to do here
        }
    }

    /** Streams arrays of booleans. */
    public static class BooleanArrayStreamer extends Streamer
    {
        // documentation inherited
        public Object createObject (ObjectInputStream in)
            throws IOException
        {
            return new boolean[in.readInt()];
        }

        // documentation inherited
        public void writeObject (
            Object object, ObjectOutputStream out, boolean useWriter)
            throws IOException
        {
            boolean[] value = (boolean[])object;
            int ecount = value.length;
            out.writeInt(ecount);
            for (int ii = 0; ii < ecount; ii++) {
                out.writeBoolean(value[ii]);
            }
        }

        // documentation inherited
        public void readObject (
            Object object, ObjectInputStream in, boolean useReader)
            throws IOException, ClassNotFoundException
        {
            boolean[] value = (boolean[])object;
            int ecount = value.length;
            for (int ii = 0; ii < ecount; ii++) {
                value[ii] = in.readBoolean();
            }
        }
    }

    /** Streams arrays of bytes. */
    public static class ByteArrayStreamer extends Streamer
    {
        // documentation inherited
        public Object createObject (ObjectInputStream in)
            throws IOException
        {
            return new byte[in.readInt()];
        }

        // documentation inherited
        public void writeObject (
            Object object, ObjectOutputStream out, boolean useWriter)
            throws IOException
        {
            byte[] value = (byte[])object;
            int ecount = value.length;
            out.writeInt(ecount);
            out.write(value);
        }

        // documentation inherited
        public void readObject (
            Object object, ObjectInputStream in, boolean useReader)
            throws IOException, ClassNotFoundException
        {
            byte[] value = (byte[])object;
            int remain = value.length, offset = 0, read;
            while (remain > 0) {
                if ((read = in.read(value, offset, remain)) > 0) {
                    remain -= read;
                    offset += read;
                } else {
                    throw new EOFException();
                }
            }
        }
    }

    /** Streams arrays of shorts. */
    public static class ShortArrayStreamer extends Streamer
    {
        // documentation inherited
        public Object createObject (ObjectInputStream in)
            throws IOException
        {
            return new short[in.readInt()];
        }

        // documentation inherited
        public void writeObject (
            Object object, ObjectOutputStream out, boolean useWriter)
            throws IOException
        {
            short[] value = (short[])object;
            int ecount = value.length;
            out.writeInt(ecount);
            for (int ii = 0; ii < ecount; ii++) {
                out.writeShort(value[ii]);
            }
        }

        // documentation inherited
        public void readObject (
            Object object, ObjectInputStream in, boolean useReader)
            throws IOException, ClassNotFoundException
        {
            short[] value = (short[])object;
            int ecount = value.length;
            for (int ii = 0; ii < ecount; ii++) {
                value[ii] = in.readShort();
            }
        }
    }

    /** Streams arrays of chars. */
    public static class CharArrayStreamer extends Streamer
    {
        // documentation inherited
        public Object createObject (ObjectInputStream in)
            throws IOException
        {
            return new char[in.readInt()];
        }

        // documentation inherited
        public void writeObject (
            Object object, ObjectOutputStream out, boolean useWriter)
            throws IOException
        {
            char[] value = (char[])object;
            int ecount = value.length;
            out.writeInt(ecount);
            for (int ii = 0; ii < ecount; ii++) {
                out.writeChar(value[ii]);
            }
        }

        // documentation inherited
        public void readObject (
            Object object, ObjectInputStream in, boolean useReader)
            throws IOException, ClassNotFoundException
        {
            char[] value = (char[])object;
            int ecount = value.length;
            for (int ii = 0; ii < ecount; ii++) {
                value[ii] = in.readChar();
            }
        }
    }

    /** Streams arrays of ints. */
    public static class IntArrayStreamer extends Streamer
    {
        // documentation inherited
        public Object createObject (ObjectInputStream in)
            throws IOException
        {
            return new int[in.readInt()];
        }

        // documentation inherited
        public void writeObject (
            Object object, ObjectOutputStream out, boolean useWriter)
            throws IOException
        {
            int[] value = (int[])object;
            int ecount = value.length;
            out.writeInt(ecount);
            for (int ii = 0; ii < ecount; ii++) {
                out.writeInt(value[ii]);
            }
        }

        // documentation inherited
        public void readObject (
            Object object, ObjectInputStream in, boolean useReader)
            throws IOException, ClassNotFoundException
        {
            int[] value = (int[])object;
            int ecount = value.length;
            for (int ii = 0; ii < ecount; ii++) {
                value[ii] = in.readInt();
            }
        }
    }

    /** Streams arrays of longs. */
    public static class LongArrayStreamer extends Streamer
    {
        // documentation inherited
        public Object createObject (ObjectInputStream in)
            throws IOException
        {
            return new long[in.readInt()];
        }

        // documentation inherited
        public void writeObject (
            Object object, ObjectOutputStream out, boolean useWriter)
            throws IOException
        {
            long[] value = (long[])object;
            int ecount = value.length;
            out.writeInt(ecount);
            for (int ii = 0; ii < ecount; ii++) {
                out.writeLong(value[ii]);
            }
        }

        // documentation inherited
        public void readObject (
            Object object, ObjectInputStream in, boolean useReader)
            throws IOException, ClassNotFoundException
        {
            long[] value = (long[])object;
            int ecount = value.length;
            for (int ii = 0; ii < ecount; ii++) {
                value[ii] = in.readLong();
            }
        }
    }

    /** Streams arrays of floats. */
    public static class FloatArrayStreamer extends Streamer
    {
        // documentation inherited
        public Object createObject (ObjectInputStream in)
            throws IOException
        {
            return new float[in.readInt()];
        }

        // documentation inherited
        public void writeObject (
            Object object, ObjectOutputStream out, boolean useWriter)
            throws IOException
        {
            float[] value = (float[])object;
            int ecount = value.length;
            out.writeInt(ecount);
            for (int ii = 0; ii < ecount; ii++) {
                out.writeFloat(value[ii]);
            }
        }

        // documentation inherited
        public void readObject (
            Object object, ObjectInputStream in, boolean useReader)
            throws IOException, ClassNotFoundException
        {
            float[] value = (float[])object;
            int ecount = value.length;
            for (int ii = 0; ii < ecount; ii++) {
                value[ii] = in.readFloat();
            }
        }
    }

    /** Streams arrays of doubles. */
    public static class DoubleArrayStreamer extends Streamer
    {
        // documentation inherited
        public Object createObject (ObjectInputStream in)
            throws IOException
        {
            return new double[in.readInt()];
        }

        // documentation inherited
        public void writeObject (
            Object object, ObjectOutputStream out, boolean useWriter)
            throws IOException
        {
            double[] value = (double[])object;
            int ecount = value.length;
            out.writeInt(ecount);
            for (int ii = 0; ii < ecount; ii++) {
                out.writeDouble(value[ii]);
            }
        }

        // documentation inherited
        public void readObject (
            Object object, ObjectInputStream in, boolean useReader)
            throws IOException, ClassNotFoundException
        {
            double[] value = (double[])object;
            int ecount = value.length;
            for (int ii = 0; ii < ecount; ii++) {
                value[ii] = in.readDouble();
            }
        }
    }

    /** Streams arrays of Object instances. */
    public static class ObjectArrayStreamer extends Streamer
    {
        // documentation inherited
        public Object createObject (ObjectInputStream in)
            throws IOException
        {
            return new Object[in.readInt()];
        }

        // documentation inherited
        public void writeObject (
            Object object, ObjectOutputStream out, boolean useWriter)
            throws IOException
        {
            Object[] value = (Object[])object;
            int ecount = value.length;
            out.writeInt(ecount);
            for (int ii = 0; ii < ecount; ii++) {
                out.writeObject(value[ii]);
            }
        }

        // documentation inherited
        public void readObject (
            Object object, ObjectInputStream in, boolean useReader)
            throws IOException, ClassNotFoundException
        {
            Object[] value = (Object[])object;
            int ecount = value.length;
            for (int ii = 0; ii < ecount; ii++) {
                value[ii] = in.readObject();
            }
        }
    }
}
