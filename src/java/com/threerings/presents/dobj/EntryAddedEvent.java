//
// $Id: EntryAddedEvent.java,v 1.8 2002/07/23 05:52:48 mdb Exp $

package com.threerings.presents.dobj;

import java.io.IOException;

import com.samskivert.util.StringUtil;

import com.threerings.io.ObjectInputStream;
import com.threerings.io.ObjectOutputStream;

import com.threerings.presents.Log;

/**
 * An entry added event is dispatched when an entry is added to a {@link
 * DSet} attribute of a distributed entry. It can also be constructed to
 * request the addition of an entry to a set and posted to the dobjmgr.
 *
 * @see DObjectManager#postEvent
 */
public class EntryAddedEvent extends DEvent
{
    /**
     * Constructs a new entry added event on the specified target object
     * with the supplied set attribute name and entry to add.
     *
     * @param targetOid the object id of the object to whose set we will
     * add an entry.
     * @param name the name of the attribute to which to add the specified
     * entry.
     * @param entry the entry to add to the set attribute.
     */
    public EntryAddedEvent (int targetOid, String name, DSet.Entry entry)
    {
        super(targetOid);
        _name = name;
        _entry = entry;
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
        set.add(_entry);
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
            ((SetListener)listener).entryAdded(this);
        }
    }

    // documentation inherited
    protected void toString (StringBuffer buf)
    {
        buf.append("ELADD:");
        super.toString(buf);
        buf.append(", name=").append(_name);
        buf.append(", entry=");
        StringUtil.toString(buf, _entry);
    }

    protected String _name;
    protected DSet.Entry _entry;
}
