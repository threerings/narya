//
// $Id: ArrayMask.java,v 1.1 2002/07/23 05:42:34 mdb Exp $

package com.threerings.io;

import java.io.IOException;

/**
 * Used to keep track of which entries in an array are null and which are
 * not. <em>Note:</em> only arrays up to 262,144 elements in length can be
 * handled by this class.
 */
public class ArrayMask
{
    /**
     * Creates an array mask suitable for unserializing.
     */
    public ArrayMask ()
    {
    }

    /**
     * Creates an array mask for an array of the specified length.
     */
    public ArrayMask (int length)
    {
        int mlength = length/8;
        if (length % 8 != 0) {
            mlength++;
        }
        _mask = new byte[mlength];
    }

    /**
     * Sets the bit indicating that the specified array index is non-null.
     */
    public void set (int index)
    {
        _mask[index/8] |= (1 << (index%8));
    }

    /**
     * Returns true if the specified array index should be non-null.
     */
    public boolean isSet (int index)
    {
        return (_mask[index/8] & (1 << (index%8))) != 0;
    }

    /**
     * Writes this mask to the specified output stream.
     */
    public void writeTo (ObjectOutputStream out)
        throws IOException
    {
        out.writeShort(_mask.length);
        out.write(_mask);
    }

    /**
     * Reads this mask from the specified input stream.
     */
    public void readFrom (ObjectInputStream in)
        throws IOException
    {
        int length = in.readShort();
        _mask = new byte[length];
        in.read(_mask);
    }

    /** A byte array with bits for every entry in the source array. */
    protected byte[] _mask;
}
