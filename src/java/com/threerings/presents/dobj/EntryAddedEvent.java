//
// $Id: EntryAddedEvent.java,v 1.2 2001/08/16 03:33:11 mdb Exp $

package com.threerings.cocktail.cher.dobj;

import java.io.*;

import com.threerings.cocktail.cher.Log;

/**
 * An element added event is dispatched when an element is added to a
 * <code>DSet</code> attribute of a distributed element. It can also be
 * constructed to request the addition of an element to a set and posted
 * to the dobjmgr.
 *
 * @see DObjectManager#postEvent
 */
public class ElementAddedEvent extends TypedEvent
{
    /** The typed object code for this event. */
    public static final short TYPE = TYPE_BASE + 8;

    /**
     * Constructs a new element added event on the specified target object
     * with the supplied set attribute name and element to add.
     *
     * @param targetOid the object id of the object to whose set we will
     * add an element.
     * @param name the name of the attribute to which to add the specified
     * element.
     * @param elem the element to add to the set attribute.
     */
    public ElementAddedEvent (int targetOid, String name, DSet.Element elem)
    {
        super(targetOid);
        _name = name;
        _elem = elem;
    }

    /**
     * Constructs a blank instance of this event in preparation for
     * unserialization from the network.
     */
    public ElementAddedEvent ()
    {
    }

    /**
     * Returns the name of the set attribute to which an element has been
     * added.
     */
    public String getName ()
    {
        return _name;
    }

    /**
     * Returns the element that has been added.
     */
    public DSet.Element getElement ()
    {
        return _elem;
    }

    /**
     * Applies this event to the object.
     */
    public boolean applyToObject (DObject target)
        throws ObjectAccessException
    {
        DSet set = (DSet)target.getAttribute(_name);

        // now that we have access to our target set, we can unflatten our
        // element (if need be)
        if (_elem == null) {
            try {
                _elem = unflatten(set, _bytes);
            } catch (Exception e) {
                Log.warning("Error unflattening element " + this + ".");
                Log.logStackTrace(e);
                return false;
            }
        }

        set.add(_elem);
        return true;
    }

    public short getType ()
    {
        return TYPE;
    }

    public void writeTo (DataOutputStream out)
        throws IOException
    {
        super.writeTo(out);
        out.writeUTF(_name);
        flatten(out, _elem);
    }

    public void readFrom (DataInputStream in)
        throws IOException
    {
        super.readFrom(in);
        _name = in.readUTF();

        // we read in the raw element data now and decode it later when we
        // have access to the object and the DSet instance that knows what
        // type of element we need to decode
        int bcount = in.readInt();
        _bytes = new byte[bcount];
        in.readFully(_bytes, 0, bcount);
    }

    protected void toString (StringBuffer buf)
    {
        buf.append("ELADD:");
        super.toString(buf);
        buf.append(", name=").append(_name);
        buf.append(", elem=").append(_elem);
    }

    protected String _name;
    protected byte[] _bytes;
    protected DSet.Element _elem;

    /**
     * Because we don't know the type of the element at the time that the
     * event is read from the network (we only know it once the event is
     * being applied to the object and we can ask the target
     * <code>DSet</code> instance what type it manages), we flatten the
     * element before writing it to the wire so that we can prepend it by
     * a byte count. The receiver will read in the raw bytes and decode
     * them only later when it has access to the <code>DSet</code>
     * instance that knows what element type to use. This method should
     * really only be called by the conmgr thread, but we synchronize just
     * in case someone decides to write an event out in some other
     * peculiar context. Uncontested syncs are pretty fast.
     */
    protected static synchronized void flatten (
        DataOutputStream out, DSet.Element elem)
        throws IOException
    {
        elem.writeTo(_dout);
        _dout.flush();
        out.writeInt(_bout.size());
        _bout.writeTo(out);
    }

    /**
     * Unflattens an element given the serialized element data. We know
     * this will always be called on the dobjmgr thread, so we need not
     * synchronize.
     */
    protected static DSet.Element unflatten (DSet set, byte[] data)
        throws IOException
    {
        _bin.setBytes(data);
        DSet.Element elem = set.newElement();
        elem.readFrom(_din);
        return elem;
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
