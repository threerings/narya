//
// $Id: HeterogenousStreamableList.java,v 1.1 2002/02/11 22:43:18 shaper Exp $

package com.threerings.presents.io;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;

import java.util.ArrayList;
import java.util.HashMap;

import com.samskivert.util.HashIntMap;

/**
 * A heterogenous streamable list is a wrapper around an {@link ArrayList}
 * that knows how to efficiently stream {@link Streamable} objects of
 * potentially heterogenous classes.  Accordingly, all objects inserted
 * into the list must implement the {@link Streamable} interface.  The
 * list ordering is properly maintained when sending data over the wire.
 */
public class HeterogenousStreamableList extends ArrayList
    implements Streamable
{
    // documentation inherited from interface
    public void writeTo (DataOutputStream out)
        throws IOException
    {
        // create a table to map class names to class ids
        HashMap cnames = new HashMap();

        // write out the size of the list
        int size = size();
        out.writeInt(size);

        // write out each of the list elements
        int classId = 0;
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
            Integer cid = (Integer)cnames.get(cname);
            if (cid == null) {
                // store this class name to class id mapping
                cid = new Integer(classId++);
                cnames.put(cname, cid);
                out.writeInt(cid.intValue());
                out.writeUTF(cname);

            } else {
                // we need only write out the class id
                out.writeInt(cid.intValue());
            }

            // write out the object itself
            s.writeTo(out);
        }
    }

    // documentation inherited from interface
    public void readFrom (DataInputStream in)
        throws IOException
    {
        // create a table to map class ids to class names
        HashIntMap cids = new HashIntMap();

        // read in the size of the list
        int size = in.readInt();

        // read in each of the list elements
        for (int ii = 0; ii < size; ii++) {
            // read the class id number
            int cid = in.readInt();

            // look up the class name
            String cname = (String)cids.get(cid);
            if (cname == null) {
                // store this class id to class name mapping
                cname = in.readUTF();
                cids.put(cid, cname);
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
}
