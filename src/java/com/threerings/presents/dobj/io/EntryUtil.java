//
// $Id: EntryUtil.java,v 1.4 2002/02/04 00:50:12 mdb Exp $

package com.threerings.presents.dobj.io;

import java.io.*;

import com.threerings.presents.Log;
import com.threerings.presents.dobj.DSet;

/**
 * Routines to simplify the process of moving set elements over the wire.
 * Because we don't know the type of the element when the event is
 * unserialized (we only know later when the event is applied to the
 * object and the event has access to the target set object), then we need
 * to do some jockeying.
 */
public class ElementUtil
{
    /**
     * Flattens the supplied element into a byte array, counts the number
     * of bytes in the array and writes the count followed by the bytes to
     * the supplied data output stream. This method should really only be
     * called by the conmgr thread, but we synchronize just in case
     * someone decides to write an event out in some other peculiar
     * context; uncontested syncs are pretty fast.
     */
    public static synchronized void flatten (
        DataOutputStream out, DSet.Element elem, boolean qualified)
        throws IOException
    {
        // write the element classname out if requested
        if (qualified) {
            _dout.writeUTF(elem.getClass().getName());
        }
        elem.writeTo(_dout);
        _dout.flush();
        out.writeInt(_bout.size());
        _bout.writeTo(out);
        _bout.reset();
    }

    /**
     * Unflattens an element given the serialized element data. We know
     * this will always be called on the dobjmgr thread, so we need not
     * synchronize.
     */
    public static DSet.Element unflatten (DSet set, byte[] data)
        throws IOException
    {
        _bin.setBytes(data);
        return set.readElement(_din);
    }

    /**
     * We extend byte array input stream to avoid having to create a new
     * input stream every time we unserialize an element. Our extensions
     * allow us to repurpose this input stream to read from a new byte
     * array each time we unserialize.
     */
    protected static class ReByteArrayInputStream extends ByteArrayInputStream
    {
        public ReByteArrayInputStream ()
        {
            super(new byte[0]);
        }

        public void setBytes (byte[] bytes)
        {
            buf = bytes;
            pos = 0;
            count = buf.length;
            mark = 0;
        }
    }

    /** Used when serializing elements. */
    protected static ByteArrayOutputStream _bout = new ByteArrayOutputStream();

    /** Used when serializing elements. */
    protected static DataOutputStream _dout = new DataOutputStream(_bout);

    /** Used when unserializing elements. */
    protected static ReByteArrayInputStream _bin =
        new ReByteArrayInputStream();

    /** Used when unserializing elements. */
    protected static DataInputStream _din = new DataInputStream(_bin);
}
