//
// $Id: StreamableArrayList.java,v 1.4 2002/03/20 22:58:26 mdb Exp $

package com.threerings.presents.util;

import java.io.IOException;
import java.io.DataInputStream;
import java.io.DataOutputStream;

import java.util.ArrayList;
import com.threerings.presents.dobj.DSet;
import com.threerings.presents.io.Streamable;
import com.threerings.presents.io.StreamableUtil;

/**
 * Provides a means by which an ordered collection of streamable instances
 * (of potentially heterogenous derived classes) can be delivered over the
 * network. A streamable array list can be supplied anywhere that a
 * distributed object value can be supplied, but bear in mind that once
 * the list is created, it's elements cannot be changed without
 * rebroadcasting the entire list. It is not like a {@link DSet} which
 * allows individual elements to be added or removed, or
 * <code>Streamable[]</code> distributed object fields for which elements
 * can be individually updated.
 */
public class StreamableArrayList
    extends ArrayList implements Streamable
{
    // documentation inherited
    public void writeTo (DataOutputStream out)
        throws IOException
    {
        // convert ourselves into an array and use streamable util to
        // write it out
        Streamable[] values = new Streamable[size()];
        toArray(values);
        StreamableUtil.writeStreamables(out, values);
    }

    // documentation inherited
    public void readFrom (DataInputStream in)
        throws IOException
    {
        // use streamable util to read in our values
        Streamable[] values = StreamableUtil.readStreamables(in);
        int vcount = values.length;
        for (int i = 0; i < vcount; i++) {
            add(values[i]);
        }
    }
}
