//
// $Id: EntryAddedEvent.java,v 1.7 2002/03/18 23:21:26 mdb Exp $

package com.threerings.presents.dobj;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;

import com.threerings.presents.Log;
import com.threerings.presents.dobj.io.EntryUtil;

/**
 * An entry added event is dispatched when an entry is added to a {@link
 * DSet} attribute of a distributed entry. It can also be constructed to
 * request the addition of an entry to a set and posted to the dobjmgr.
 *
 * @see DObjectManager#postEvent
 */
public class EntryAddedEvent extends TypedEvent
{
    /** The typed object code for this event. */
    public static final short TYPE = TYPE_BASE + 8;

    /**
     * Constructs a new entry added event on the specified target object
     * with the supplied set attribute name and entry to add.
     *
     * @param targetOid the object id of the object to whose set we will
     * add an entry.
     * @param name the name of the attribute to which to add the specified
     * entry.
     * @param entry the entry to add to the set attribute.
     * @param qualified whether or not the entry need be qualified with
     * its class when serializing (true for heterogenous sets, false for
     * homogenous sets).
     */
    public EntryAddedEvent (int targetOid, String name, DSet.Entry entry,
                              boolean qualified)
    {
        super(targetOid);
        _name = name;
        _entry = entry;
        _qualified = qualified;
    }

    /**
     * Constructs a blank instance of this event in preparation for
     * unserialization from the network.
     */
    public EntryAddedEvent ()
    {
    }

    /**
     * Returns the name of the set attribute to which an entry has been
     * added.
     */
    public String getName ()
    {
        return _name;
    }

    /**
     * Returns the entry that has been added.
     */
    public DSet.Entry getEntry ()
    {
        return _entry;
    }

    /**
     * Applies this event to the object.
     */
    public boolean applyToObject (DObject target)
        throws ObjectAccessException
    {
        DSet set = (DSet)target.getAttribute(_name);

        // now that we have access to our target set, we can unflatten our
        // entry (if need be)
        if (_entry == null) {
            try {
                _entry = EntryUtil.unflatten(set, _bytes);
            } catch (Exception e) {
                Log.warning("Error unflattening entry " + this + ".");
                Log.logStackTrace(e);
                return false;
            }
        }

        set.add(_entry);
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
        EntryUtil.flatten(out, _entry, _qualified);
    }

    // documentation inherited
    public void readFrom (DataInputStream in)
        throws IOException
    {
        super.readFrom(in);
        _name = in.readUTF();

        // we read in the raw entry data now and decode it later when we
        // have access to the object and the DSet instance that knows what
        // type of entry we need to decode
        int bcount = in.readInt();
        _bytes = new byte[bcount];
        in.readFully(_bytes, 0, bcount);
    }

    // documentation inherited
    protected void notifyListener (Object listener)
    {
        if (listener instanceof SetListener) {
            ((SetListener)listener).entryAdded(this);
        }
    }

    // documentation inherited
    protected void toString (StringBuffer buf)
    {
        buf.append("ELADD:");
        super.toString(buf);
        buf.append(", name=").append(_name);
        buf.append(", entry=").append(_entry);
    }

    protected String _name;
    protected byte[] _bytes;
    protected DSet.Entry _entry;
    protected boolean _qualified;
}
