//
// $Id: StreamableArrayList.java,v 1.1 2001/10/04 23:02:07 mdb Exp $

package com.threerings.cocktail.cher.util;

import java.io.IOException;
import java.io.DataInputStream;
import java.io.DataOutputStream;

import java.util.ArrayList;
import com.threerings.cocktail.cher.io.Streamable;

/**
 * Provides a means by which an ordered collection of streamable instances
 * (all of the exact same class) can be delivered over the network. A
 * streamable array list can be supplied anywhere that a distributed
 * object value can be supplied, but bear in mind that once the list is
 * created, it's elements cannot be changed without rebroadcasting the
 * entire list. It is not like a {@link
 * com.threerings.cocktail.cher.dobj.DSet} which allows individual
 * elements to be added or removed.
 */
public class StreamableArrayList
    extends ArrayList implements Streamable
{
    // documentation inherited
    public void writeTo (DataOutputStream out)
        throws IOException
    {
        int count = size();
        out.writeInt(count);

        // only write element info if we have elements
        if (count > 0) {
            Streamable first = (Streamable)get(0);
            // write out the classname of our elements
            out.writeUTF(first.getClass().getName());

            // now write out our elements
            for (int i = 0; i < count; i++) {
                Streamable s = (Streamable)get(i);
                s.writeTo(out);
            }
        }
    }

    // documentation inherited
    public void readFrom (DataInputStream in)
        throws IOException
    {
        // read in our element count
        int count = in.readInt();

        // only read in elements if we have some
        if (count > 0) {
            // read in the element class and load it up
            String cname = in.readUTF();
            try {
                Class clazz = Class.forName(cname);

                // now read in the elements
                for (int i = 0; i < count; i++) {
                    Streamable s = (Streamable)clazz.newInstance();
                    s.readFrom(in);
                    add(s);
                }

            } catch (Exception e) {
                throw new IOException("Error reading streamable " +
                                      "[class=" + cname + ", err=" + e + "]");
            }
        }
    }
}
