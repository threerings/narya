//
// $Id: StreamableArrayList.java,v 1.1 2002/07/23 05:54:52 mdb Exp $

package com.threerings.util;

import java.io.IOException;
import java.util.ArrayList;

import com.threerings.io.ObjectInputStream;
import com.threerings.io.ObjectOutputStream;
import com.threerings.io.Streamable;

/**
 * An {@link ArrayList} extension that can be streamed. The contents of
 * the list must also be of streamable types.
 *
 * @see Streamable
 */
public class StreamableArrayList extends ArrayList
    implements Streamable
{
    /**
     * Writes our custom streamable fields.
     */
    public void writeObject (ObjectOutputStream out)
        throws IOException
    {
        int ecount = size();
        out.writeInt(ecount);
        for (int ii = 0; ii < ecount; ii++) {
            out.writeObject(get(ii));
        }
    }

    /**
     * Reads our custom streamable fields.
     */
    public void readObject (ObjectInputStream in)
        throws IOException, ClassNotFoundException
    {
        int ecount = in.readInt();
        for (int ii = 0; ii < ecount; ii++) {
            add(in.readObject());
        }
    }
}
