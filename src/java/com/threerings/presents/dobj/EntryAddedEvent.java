//
// $Id: EntryAddedEvent.java,v 1.5 2001/10/12 00:03:03 mdb Exp $

package com.threerings.presents.dobj;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;

import com.threerings.presents.Log;
import com.threerings.presents.dobj.io.ElementUtil;

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
                _elem = ElementUtil.unflatten(set, _bytes);
            } catch (Exception e) {
                Log.warning("Error unflattening element " + this + ".");
                Log.logStackTrace(e);
                return false;
            }
        }

        set.add(_elem);
        return true;
    }

    // documentation inherited
    public short getType ()
    {
        return TYPE;
    }

    // documentation inherited
    public void writeTo (DataOutputStream out)
        throws IOException
    {
        super.writeTo(out);
        out.writeUTF(_name);
        ElementUtil.flatten(out, _elem);
    }

    // documentation inherited
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

    // documentation inherited
    protected void notifyListener (Object listener)
    {
        if (listener instanceof SetListener) {
            ((SetListener)listener).elementAdded(this);
        }
    }

    // documentation inherited
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
}
