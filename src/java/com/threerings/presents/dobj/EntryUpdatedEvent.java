//
// $Id: EntryUpdatedEvent.java,v 1.6 2002/04/18 00:31:26 mdb Exp $

package com.threerings.presents.dobj;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;

import com.threerings.presents.Log;
import com.threerings.presents.dobj.io.EntryUtil;

/**
 * An entry updated event is dispatched when an entry of a {@link DSet} is
 * updated. It can also be constructed to request the update of an entry
 * and posted to the dobjmgr.
 *
 * @see DObjectManager#postEvent
 */
public class EntryUpdatedEvent extends TypedEvent
{
    /** The typed object code for this event. */
    public static final short TYPE = TYPE_BASE + 10;

    /**
     * Constructs a new entry updated event on the specified target object
     * for the specified set name and with the supplied updated entry.
     *
     * @param targetOid the object id of the object to whose set we will
     * add an entry.
     * @param name the name of the attribute in which to update the
     * specified entry.
     * @param entry the entry to update.
     * @param qualified whether or not the entry need be qualified with
     * its class when serializing (true for heterogenous sets, false for
     * homogenous sets).
     */
    public EntryUpdatedEvent (int targetOid, String name, DSet.Entry entry,
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
    public EntryUpdatedEvent ()
    {
    }

    /**
     * Returns the name of the set attribute for which an entry has been
     * updated.
     */
    public String getName ()
    {
        return _name;
    }

    /**
     * Returns the entry that has been updated.
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

        // update the entry
        if (!set.update(_entry)) {
            // complain if we didn't update anything
            Log.warning("No matching entry to update [entry=" + this +
                        ", set=" + set + "].");
            return false;
        }

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
            ((SetListener)listener).entryUpdated(this);
        }
    }

    // documentation inherited
    protected void toString (StringBuffer buf)
    {
        buf.append("ELUPD:");
        super.toString(buf);
        buf.append(", name=").append(_name);
        buf.append(", entry=").append(_entry);
    }

    protected String _name;
    protected byte[] _bytes;
    protected DSet.Entry _entry;
    protected boolean _qualified;
}
