//
// $Id: ValueMarshaller.java,v 1.1 2001/06/12 02:57:30 mdb Exp $

package com.threerings.cocktail.cher.dobj.io;

import java.io.*;
import java.util.HashMap;

import com.samskivert.util.IntMap;

/**
 * The value marshaller provides a mechanism for marshalling and
 * unmarshalling values of valid distributed object attribute types
 * (<code>int</code>, <code>String</code>, etc.). It is used when the type
 * needs to be accompanied by a type identifier because it cannot be
 * inferred from the context.
 *
 * <p> For example, when we are serializing a distributed object, we don't
 * use the value marshaller because we can just write the attributes out
 * in a known order and infer their types on the receiving end. On the
 * other hand, when serializing an event, we need to use the value
 * marshaller because we only have an oid, an attribute name and some
 * arbitrary value at that point. We can't be sure that the oid maps to an
 * object on the receiving end from which we could determine the type of
 * that particular attribute.
 *
 * <p> Note also that we only deal with distributed object types in object
 * form (meaning <code>int</code> values have been converted into
 * <code>Integer</code> instances, etc.).
 */
public class ValueMarshaller
{
    /**
     * Writes the supplied value to the output stream preceeded by a type
     * identifier that will allow us to read it back in on the other end.
     * The value must be one of the valid distributed object attribute
     * types.
     *
     * @see DObject
     * @see #readFrom
     */
    public static void writeTo (DataOutputStream out, Object value)
        throws IOException
    {
        Marshaller marsh = (Marshaller)_outmap.get(value.getClass());
        if (marsh == null) {
            throw new RuntimeException("Requested to serialize invalid " +
                                       "type [value=" + value + ", type=" +
                                       value.getClass().getName() + "].");
        }

        // write the value out using the appropriate marshaller
        out.writeByte(marsh.code);
        marsh.writeValue(out, value);
    }

    /**
     * Reads a value in from the stream that was previously written out
     * with <code>writeTo</code>.
     *
     * @see #writeTo
     */
    public static Object readFrom (DataInputStream in)
        throws IOException
    {
        byte code = in.readByte();
        Marshaller marsh = (Marshaller)_inmap.get((int)code);
        if (marsh == null) {
            throw new RuntimeException("Requested to unserialize invalid " +
                                       "type [code=" + code + "].");
        }
        return marsh.readValue(in);
    }

    // test these suckers out
    public static void main (String[] args)
    {
        Object[] values = new Object[14];
        values[0] = new Byte((byte)1);
        values[1] = new Short((short)2);
        values[2] = new Integer(3);
        values[3] = new Long(4l);
        values[4] = new Float(5.0f);
        values[5] = new Double(6.0);
        values[6] = "this is a string";
        values[7] = new byte[] { 0, 1, 2, 3 };
        values[8] = new short[] { 0, 1, 2, 3 };
        values[9] = new int[] { 0, 1, 2, 3 };
        values[10] = new long[] { 0, 1, 2, 3 };
        values[11] = new float[] { 0.0f, 1.0f, 2.0f, 3.0f };
        values[12] = new double[] { 0.0, 1.0, 2.0, 3.0 };
        values[13] = new String[] { "one", "two", "three", "four" };

        File file = new File("test.dat");

        try {
            FileOutputStream fout = new FileOutputStream(file);
            BufferedOutputStream bout = new BufferedOutputStream(fout);
            DataOutputStream dout = new DataOutputStream(bout);

            // write out our values
            for (int i = 0; i < values.length; i++) {
                writeTo(dout, values[i]);
            }

            // close the file
            dout.flush();
            fout.close();

            // and read it all back in
            FileInputStream fin = new FileInputStream(file);
            BufferedInputStream bin = new BufferedInputStream(fin);
            DataInputStream din = new DataInputStream(bin);

            for (int i = 0; i < values.length; i++) {
                Object value = readFrom(din);
                System.out.println(value.getClass().getName() + ": " + value);
            }

            fin.close();

        } catch (IOException ioe) {
            ioe.printStackTrace();
        }

        // delete our temp file
        file.delete();
    }

    /**
     * Used to marshall and unmarshall values.
     */
    protected static abstract class Marshaller
    {
        public byte code;

        public Marshaller (byte code)
        {
            this.code = code;
        }

        public abstract void writeValue (DataOutputStream out, Object value)
            throws IOException;

        public abstract Object readValue (DataInputStream in)
            throws IOException;
    }

    protected static class ByteMarshaller extends Marshaller
    {
        public ByteMarshaller ()
        {
            super((byte)1);
        }

        public void writeValue (DataOutputStream out, Object value)
            throws IOException
        {
            out.writeByte(((Byte)value).byteValue());
        }

        public Object readValue (DataInputStream in)
            throws IOException
        {
            return new Byte(in.readByte());
        }
    }

    protected static class ShortMarshaller extends Marshaller
    {
        public ShortMarshaller ()
        {
            super((byte)2);
        }

        public void writeValue (DataOutputStream out, Object value)
            throws IOException
        {
            out.writeShort(((Short)value).shortValue());
        }

        public Object readValue (DataInputStream in)
            throws IOException
        {
            return new Short(in.readShort());
        }
    }

    protected static class IntegerMarshaller extends Marshaller
    {
        public IntegerMarshaller ()
        {
            super((byte)3);
        }

        public void writeValue (DataOutputStream out, Object value)
            throws IOException
        {
            out.writeInt(((Integer)value).intValue());
        }

        public Object readValue (DataInputStream in)
            throws IOException
        {
            return new Integer(in.readInt());
        }
    }

    protected static class LongMarshaller extends Marshaller
    {
        public LongMarshaller ()
        {
            super((byte)4);
        }

        public void writeValue (DataOutputStream out, Object value)
            throws IOException
        {
            out.writeLong(((Long)value).longValue());
        }

        public Object readValue (DataInputStream in)
            throws IOException
        {
            return new Long(in.readLong());
        }
    }

    protected static class FloatMarshaller extends Marshaller
    {
        public FloatMarshaller ()
        {
            super((byte)5);
        }

        public void writeValue (DataOutputStream out, Object value)
            throws IOException
        {
            out.writeFloat(((Float)value).floatValue());
        }

        public Object readValue (DataInputStream in)
            throws IOException
        {
            return new Float(in.readFloat());
        }
    }

    protected static class DoubleMarshaller extends Marshaller
    {
        public DoubleMarshaller ()
        {
            super((byte)6);
        }

        public void writeValue (DataOutputStream out, Object value)
            throws IOException
        {
            out.writeDouble(((Double)value).doubleValue());
        }

        public Object readValue (DataInputStream in)
            throws IOException
        {
            return new Double(in.readDouble());
        }
    }

    protected static class StringMarshaller extends Marshaller
    {
        public StringMarshaller ()
        {
            super((byte)7);
        }

        public void writeValue (DataOutputStream out, Object value)
            throws IOException
        {
            out.writeUTF((String)value);
        }

        public Object readValue (DataInputStream in)
            throws IOException
        {
            return in.readUTF();
        }
    }

    protected static class ByteArrayMarshaller extends Marshaller
    {
        public ByteArrayMarshaller ()
        {
            super((byte)8);
        }

        public void writeValue (DataOutputStream out, Object value)
            throws IOException
        {
            byte[] data = (byte[])value;
            out.writeInt(data.length);
            for (int i = 0; i < data.length; i++) {
                out.writeByte(data[i]);
            }
        }

        public Object readValue (DataInputStream in)
            throws IOException
        {
            byte[] data = new byte[in.readInt()];
            for (int i = 0; i < data.length; i++) {
                data[i] = in.readByte();
            }
            return data;
        }
    }

    protected static class ShortArrayMarshaller extends Marshaller
    {
        public ShortArrayMarshaller ()
        {
            super((byte)9);
        }

        public void writeValue (DataOutputStream out, Object value)
            throws IOException
        {
            short[] data = (short[])value;
            out.writeInt(data.length);
            for (int i = 0; i < data.length; i++) {
                out.writeShort(data[i]);
            }
        }

        public Object readValue (DataInputStream in)
            throws IOException
        {
            short[] data = new short[in.readInt()];
            for (int i = 0; i < data.length; i++) {
                data[i] = in.readShort();
            }
            return data;
        }
    }

    protected static class IntegerArrayMarshaller extends Marshaller
    {
        public IntegerArrayMarshaller ()
        {
            super((byte)10);
        }

        public void writeValue (DataOutputStream out, Object value)
            throws IOException
        {
            int[] data = (int[])value;
            out.writeInt(data.length);
            for (int i = 0; i < data.length; i++) {
                out.writeInt(data[i]);
            }
        }

        public Object readValue (DataInputStream in)
            throws IOException
        {
            int[] data = new int[in.readInt()];
            for (int i = 0; i < data.length; i++) {
                data[i] = in.readInt();
            }
            return data;
        }
    }

    protected static class LongArrayMarshaller extends Marshaller
    {
        public LongArrayMarshaller ()
        {
            super((byte)11);
        }

        public void writeValue (DataOutputStream out, Object value)
            throws IOException
        {
            long[] data = (long[])value;
            out.writeInt(data.length);
            for (int i = 0; i < data.length; i++) {
                out.writeLong(data[i]);
            }
        }

        public Object readValue (DataInputStream in)
            throws IOException
        {
            long[] data = new long[in.readInt()];
            for (int i = 0; i < data.length; i++) {
                data[i] = in.readLong();
            }
            return data;
        }
    }

    protected static class FloatArrayMarshaller extends Marshaller
    {
        public FloatArrayMarshaller ()
        {
            super((byte)12);
        }

        public void writeValue (DataOutputStream out, Object value)
            throws IOException
        {
            float[] data = (float[])value;
            out.writeInt(data.length);
            for (int i = 0; i < data.length; i++) {
                out.writeFloat(data[i]);
            }
        }

        public Object readValue (DataInputStream in)
            throws IOException
        {
            float[] data = new float[in.readInt()];
            for (int i = 0; i < data.length; i++) {
                data[i] = in.readFloat();
            }
            return data;
        }
    }

    protected static class DoubleArrayMarshaller extends Marshaller
    {
        public DoubleArrayMarshaller ()
        {
            super((byte)13);
        }

        public void writeValue (DataOutputStream out, Object value)
            throws IOException
        {
            double[] data = (double[])value;
            out.writeInt(data.length);
            for (int i = 0; i < data.length; i++) {
                out.writeDouble(data[i]);
            }
        }

        public Object readValue (DataInputStream in)
            throws IOException
        {
            double[] data = new double[in.readInt()];
            for (int i = 0; i < data.length; i++) {
                data[i] = in.readDouble();
            }
            return data;
        }
    }

    protected static class StringArrayMarshaller extends Marshaller
    {
        public StringArrayMarshaller ()
        {
            super((byte)14);
        }

        public void writeValue (DataOutputStream out, Object value)
            throws IOException
        {
            String[] data = (String[])value;
            out.writeInt(data.length);
            for (int i = 0; i < data.length; i++) {
                out.writeUTF(data[i]);
            }
        }

        public Object readValue (DataInputStream in)
            throws IOException
        {
            String[] data = new String[in.readInt()];
            for (int i = 0; i < data.length; i++) {
                data[i] = in.readUTF();
            }
            return data;
        }
    }

    protected static HashMap _outmap = new HashMap();
    protected static IntMap _inmap = new IntMap();

    protected static Class[] _classes = {
        Byte.class,
        Short.class,
        Integer.class,
        Long.class,
        Float.class,
        Double.class,
        String.class,
        (new byte[0]).getClass(),
        (new short[0]).getClass(),
        (new int[0]).getClass(),
        (new long[0]).getClass(),
        (new float[0]).getClass(),
        (new double[0]).getClass(),
        (new String[0]).getClass()
    };

    protected static Marshaller[] _marshallers = {
        new ByteMarshaller(),
        new ShortMarshaller(),
        new IntegerMarshaller(),
        new LongMarshaller(),
        new FloatMarshaller(),
        new DoubleMarshaller(),
        new StringMarshaller(),
        new ByteArrayMarshaller(),
        new ShortArrayMarshaller(),
        new IntegerArrayMarshaller(),
        new LongArrayMarshaller(),
        new FloatArrayMarshaller(),
        new DoubleArrayMarshaller(),
        new StringArrayMarshaller()
    };

    // register our marshallers
    static {
        Marshaller marsh;
        for (int i = 0; i < _classes.length; i++) {
            marsh = _marshallers[i];
            _outmap.put(_classes[i], marsh);
            _inmap.put(marsh.code, marsh);
        }
    }
}
