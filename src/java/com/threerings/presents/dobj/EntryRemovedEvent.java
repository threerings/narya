//
// $Id: EntryRemovedEvent.java,v 1.1 2001/08/16 03:31:09 mdb Exp $

package com.threerings.cocktail.cher.dobj;

import java.io.IOException;
import java.io.DataInputStream;
import java.io.DataOutputStream;

import com.threerings.cocktail.cher.dobj.io.ValueMarshaller;

/**
 * An element removed event is dispatched when an element is removed from
 * a <code>DSet</code> attribute of a distributed object. It can also be
 * constructed to request the removal of an element from a
 * <code>DSet</code> attribute of an object and posted to the dobjmgr.
 *
 * @see DObjectManager#postEvent
 */
public class ElementRemovedEvent extends TypedEvent
{
    /** The typed object code for this event. */
    public static final short TYPE = TYPE_BASE + 9;

    /**
     * Constructs a new element removed event on the specified target
     * object with the supplied set attribute name and element key to
     * remove.
     *
     * @param targetOid the object id of the object from whose set we will
     * remove an element.
     * @param name the name of the attribute from which to remove the
     * specified element.
     * @param key the element key that identifies the element to remove.
     */
    public ElementRemovedEvent (int targetOid, String name, Object key)
    {
        super(targetOid);
        _name = name;
        _key = key;
    }

    /**
     * Constructs a blank instance of this event in preparation for
     * unserialization from the network.
     */
    public ElementRemovedEvent ()
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
     * Returns the key that identifies the element that has been removed.
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

    public short getType ()
    {
        return TYPE;
    }

    public void writeTo (DataOutputStream out)
        throws IOException
    {
        super.writeTo(out);
        out.writeUTF(_name);
        ValueMarshaller.writeTo(out, _key);
    }

    public void readExternal (DataInputStream in)
        throws IOException
    {
        super.readFrom(in);
        _name = in.readUTF();
        _key = ValueMarshaller.readFrom(in);
    }

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
