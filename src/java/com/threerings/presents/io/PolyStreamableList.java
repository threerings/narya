//
// $Id: PolyStreamableList.java,v 1.1 2002/02/12 01:54:51 shaper Exp $

package com.threerings.presents.io;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;

import java.util.ArrayList;

import com.samskivert.util.ListUtil;

import com.threerings.presents.Log;

/**
 * A poly streamable list is a wrapper around an {@link ArrayList} that
 * knows how to efficiently stream {@link Streamable} objects of
 * potentially heterogenous classes.  Accordingly, all objects inserted
 * into the list must implement the {@link Streamable} interface.  The
 * list ordering is properly maintained when sending data over the wire.
 */
public class PolyStreamableList extends ArrayList
    implements Streamable
{
    // documentation inherited from interface
    public void writeTo (DataOutputStream out)
        throws IOException
    {
        // create a list used to map class names to class ids
        Object[] cnames = new String[DEFAULT_SIZE];

        // write out the size of the list
        int size = size();
        out.writeInt(size);

        // write out each of the list elements
        int nextIdx = 0;
        for (int ii = 0; ii < size; ii++) {
            // get the next streamable to write out
            Object value = get(ii);
            if (!(value instanceof Streamable)) {
                throw new RuntimeException(
                    "Requested to serialize invalid type [value=" + value +
                    ", type=" + value.getClass().getName() + "]");
            }
            Streamable s = (Streamable)value;

            // look up the class id
            String cname = s.getClass().getName();
            int cidx = ListUtil.indexOfEqual(cnames, cname);
            if (cidx == -1) {
                // store this class name to class id mapping
                cidx = nextIdx++;
                cnames = ListUtil.add(cnames, cidx, cname);

                // write out the class id and class name
                out.writeInt(cidx);
                out.writeUTF(cname);

            } else {
                // we need only write out the class id
                out.writeInt(cidx);
            }

            // write out the object itself
            s.writeTo(out);
        }
    }

    // documentation inherited from interface
    public void readFrom (DataInputStream in)
        throws IOException
    {
        // create a list used to map class ids to class names
        Object[] cnames = new String[DEFAULT_SIZE];

        // read in the size of the list
        int size = in.readInt();

        // read in each of the list elements
        for (int ii = 0; ii < size; ii++) {
            // read the class id number
            int cidx = in.readInt();

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
                if (!Streamable.class.isAssignableFrom(clazz)) {
                    throw new RuntimeException(
                        "Requested to unserialize invalid type " +
                        "[type=" + cname + "]");
                }
                Streamable s = (Streamable)clazz.newInstance();

                // read in the object itself
                s.readFrom(in);

                // and add it to the list
                add(s);

            } catch (ClassNotFoundException cnfe) {
                throw new IOException(
                    "Unable to instantiate class [cname=" + cname +
                    ", cnfe=" + cnfe + "]");

            } catch (InstantiationException ie) {
                throw new IOException(
                    "Unable to instantiate class [cname=" + cname +
                    ", ie=" + ie + "]");

            } catch (IllegalAccessException iae) {
                throw new IOException(
                    "Unable to instantiate class [cname=" + cname +
                    ", iae=" + iae + "]");
            }
        }
    }

    /** The default size of the class name lists used when serializing or
     * unserializing the list. */
    protected static final int DEFAULT_SIZE = 8;
}
