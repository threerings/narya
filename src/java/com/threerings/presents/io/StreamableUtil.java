//
// $Id: StreamableUtil.java,v 1.2 2002/03/20 22:58:26 mdb Exp $

package com.threerings.presents.io;

import java.io.IOException;
import java.io.DataInputStream;
import java.io.DataOutputStream;

import java.lang.reflect.Array;

import com.samskivert.io.NestableIOException;
import com.samskivert.util.ListUtil;

/**
 * Utility functions useful for {@link Streamable} implementations.
 */
public class StreamableUtil
{
    /**
     * Writes the supplied int array to the data output stream.
     */
    public static void writeInts (DataOutputStream out, int[] values)
        throws IOException
    {
        int vcount = values.length;
        out.writeInt(vcount);
        for (int i = 0; i < vcount; i++) {
            out.writeInt(values[i]);
        }
    }

    /**
     * Reads an array of ints from the data input stream that was
     * previously written via {@link #writeInts}.
     */
    public static int[] readInts (DataInputStream in)
        throws IOException
    {
        int vcount = in.readInt();
        int[] values = new int[vcount];
        for (int i = 0; i < vcount; i++) {
            values[i] = in.readInt();
        }
        return values;
    }

    /**
     * Writes the supplied string array to the data output stream.
     */
    public static void writeStrings (DataOutputStream out, String[] values)
        throws IOException
    {
        int vcount = values.length;
        out.writeInt(vcount);
        for (int i = 0; i < vcount; i++) {
            out.writeUTF(values[i]);
        }
    }

    /**
     * Reads an array of strings from the data input stream that was
     * previously written via {@link #writeStrings}.
     */
    public static String[] readStrings (DataInputStream in)
        throws IOException
    {
        int vcount = in.readInt();
        String[] values = new String[vcount];
        for (int i = 0; i < vcount; i++) {
            values[i] = in.readUTF();
        }
        return values;
    }

    /**
     * Writes the supplied streamable array to the data output stream. The
     * array cannot be null, but can be of zero length.
     */
    public static void writeStreamables (
        DataOutputStream out, Streamable[] values)
        throws IOException
    {
        // create a list used to map class names to class ids
        Object[] cnames = new Object[DEFAULT_SIZE];

        // seed it with the component type of the array (which we write
        // out first so that we can recreate the array on the other side)
        String cname = values.getClass().getComponentType().getName();
        cnames[0] = cname;
        out.writeUTF(cname);

        // write out the size of the list
        int size = (values == null) ? 0 : values.length;
        out.writeInt(size);

        // write out each of the list elements
        short nextIdx = 1;
        for (int ii = 0; ii < size; ii++) {
            // get the next streamable to write out
            Streamable s = values[ii];

            // look up the class id
            cname = s.getClass().getName();
            short cidx = (short)ListUtil.indexOfEqual(cnames, cname);
            if (cidx == -1) {
                // store this class name to class id mapping
                cidx = nextIdx++;
                cnames = ListUtil.add(cnames, cidx, cname);

                // write out the class id and class name
                out.writeShort(cidx);
                out.writeUTF(cname);

            } else {
                // we need only write out the class id
                out.writeShort(cidx);
            }

            // write out the object itself
            s.writeTo(out);
        }
    }

    /**
     * Reads an array of streamables from the data input stream that was
     * previously written via {@link #writeStreamables}.
     */
    public static Streamable[] readStreamables (DataInputStream in)
        throws IOException
    {
        // read in the component type and array size
        String ctype = in.readUTF();
        int size = in.readInt();
        Streamable[] values = null;

        // create the target array
        try {
            values = (Streamable[])
                Array.newInstance(Class.forName(ctype), size);
        } catch (Exception e) {
            String errmsg = "Unable to instantiate streamable array " +
                "[ctype=" + ctype + "]";
            throw new NestableIOException(errmsg, e);
        }

        // create a list used to map class ids to class names
        Object[] cnames = new Object[DEFAULT_SIZE];
        // seed our class names with the component type
        cnames[0] = ctype;

        // read in each of the list elements
        for (int ii = 0; ii < size; ii++) {
            // read the class id number
            short cidx = in.readShort();

            // look up the class name
            String cname = (cidx > cnames.length - 1) ? null :
                (String)cnames[cidx];
            if (cname == null) {
                // store this class id to class name mapping
                cname = in.readUTF();
                cnames = ListUtil.add(cnames, cidx, cname);
            }

            try {
                // instantiate the streamable object
                Class clazz = Class.forName(cname);
                Streamable s = (Streamable)clazz.newInstance();
                // read in the object itself
                s.readFrom(in);
                // and add it to the list
                values[ii] = s;

            } catch (Exception e) {
                String errmsg = "Error instantiating streamable array " +
                    "element [ctype=" + ctype + ", index=" + ii +
                    ", etype=" + cname + "]";
                throw new NestableIOException(errmsg, e);
            }
        }

        return values;
    }

    /** The default size of the class name lists used when serializing or
     * unserializing the list. */
    protected static final int DEFAULT_SIZE = 8;
}
