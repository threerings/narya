//
// $Id: StreamableUtil.java,v 1.1 2001/12/03 23:48:38 mdb Exp $

package com.threerings.presents.io;

import java.io.IOException;
import java.io.DataInputStream;
import java.io.DataOutputStream;

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
}
