//
// $Id: EntryRemovedEvent.java,v 1.8 2002/03/18 23:21:26 mdb Exp $

package com.threerings.presents.dobj;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;

import com.threerings.presents.io.ValueMarshaller;

/**
 * An entry removed event is dispatched when an entry is removed from a
 * {@link DSet} attribute of a distributed object. It can also be
 * constructed to request the removal of an entry from a set and posted to
 * the dobjmgr.
 *
 * @see DObjectManager#postEvent
 */
public class EntryRemovedEvent extends TypedEvent
{
    /** The typed object code for this event. */
    public static final short TYPE = TYPE_BASE + 9;

    /**
     * Constructs a new entry removed event on the specified target object
     * with the supplied set attribute name and entry key to remove.
     *
     * @param targetOid the object id of the object from whose set we will
     * remove an entry.
     * @param name the name of the attribute from which to remove the
     * specified entry.
     * @param key the entry key that identifies the entry to remove.
     */
    public EntryRemovedEvent (int targetOid, String name, Object key)
    {
        super(targetOid);
        _name = name;
        _key = key;
    }

    /**
     * Constructs a blank instance of this event in preparation for
     * unserialization from the network.
     */
    public EntryRemovedEvent ()
    {
    }

    /**
     * Returns the name of the oid list attribute from which an oid has
     * been removed.
     */
    public String getName ()
    {
        return _name;
    }

    /**
     * Returns the key that identifies the entry that has been removed.
     */
    public Object getKey ()
    {
        return _key;
    }

    /**
     * Applies this event to the object.
     */
    public boolean applyToObject (DObject target)
        throws ObjectAccessException
    {
        DSet set = (DSet)target.getAttribute(_name);
        set.removeKey(_key);
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
        ValueMarshaller.writeTo(out, _key);
    }

    // documentation inherited
    public void readFrom (DataInputStream in)
        throws IOException
    {
        super.readFrom(in);
        _name = in.readUTF();
        _key = ValueMarshaller.readFrom(in);
    }

    // documentation inherited
    protected void notifyListener (Object listener)
    {
        if (listener instanceof SetListener) {
            ((SetListener)listener).entryRemoved(this);
        }
    }

    // documentation inherited
    protected void toString (StringBuffer buf)
    {
        buf.append("ELREM:");
        super.toString(buf);
        buf.append(", name=").append(_name);
        buf.append(", key=").append(_key);
    }

    protected String _name;
    protected Object _key;
}
