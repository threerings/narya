//
// $Id: EntryUpdatedEvent.java,v 1.7 2002/07/23 05:52:48 mdb Exp $

package com.threerings.presents.dobj;

import java.io.IOException;

import com.samskivert.util.StringUtil;

import com.threerings.io.ObjectInputStream;
import com.threerings.io.ObjectOutputStream;

import com.threerings.presents.Log;

/**
 * An entry updated event is dispatched when an entry of a {@link DSet} is
 * updated. It can also be constructed to request the update of an entry
 * and posted to the dobjmgr.
 *
 * @see DObjectManager#postEvent
 */
public class EntryUpdatedEvent extends DEvent
{
    /**
     * Constructs a new entry updated event on the specified target object
     * for the specified set name and with the supplied updated entry.
     *
     * @param targetOid the object id of the object to whose set we will
     * add an entry.
     * @param name the name of the attribute in which to update the
     * specified entry.
     * @param entry the entry to update.
     */
    public EntryUpdatedEvent (int targetOid, String name, DSet.Entry entry)
    {
        super(targetOid);
        _name = name;
        _entry = entry;
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

        // update the entry
        if (!set.update(_entry)) {
            // complain if we didn't update anything
            Log.warning("No matching entry to update [entry=" + this +
                        ", set=" + set + "].");
            return false;
        }

        return true;
    }

    /**
     * Writes our custom streamable fields.
     */
    public void writeObject (ObjectOutputStream out)
        throws IOException
    {
        super.writeObject(out);
        out.writeUTF(_name);
        out.writeObject(_entry);
    }

    /**
     * Reads our custom streamable fields.
     */
    public void readObject (ObjectInputStream in)
        throws IOException, ClassNotFoundException
    {
        super.readObject(in);
        _name = in.readUTF();
        _entry = (DSet.Entry)in.readObject();
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
        buf.append(", entry=");
        StringUtil.toString(buf, _entry);
    }

    protected String _name;
    protected DSet.Entry _entry;
}
