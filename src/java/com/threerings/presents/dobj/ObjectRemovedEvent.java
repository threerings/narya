//
// $Id: ObjectRemovedEvent.java,v 1.6 2001/10/12 00:03:03 mdb Exp $

package com.threerings.presents.dobj;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;

/**
 * An object removed event is dispatched when an object is removed from an
 * <code>OidList</code> attribute of a distributed object. It can also be
 * constructed to request the removal of an oid from an
 * <code>OidList</code> attribute of an object and posted to the dobjmgr.
 *
 * @see DObjectManager#postEvent
 */
public class ObjectRemovedEvent extends TypedEvent
{
    /** The typed object code for this event. */
    public static final short TYPE = TYPE_BASE + 5;

    /**
     * Constructs a new object removed event on the specified target
     * object with the supplied oid list attribute name and object id to
     * remove.
     *
     * @param targetOid the object id of the object from whose oid list we
     * will remove an oid.
     * @param name the name of the attribute (data member) from which to
     * remove the specified oid.
     * @param oid the oid to remove from the oid list attribute.
     */
    public ObjectRemovedEvent (int targetOid, String name, int oid)
    {
        super(targetOid);
        _name = name;
        _oid = oid;
    }

    /**
     * Constructs a blank instance of this event in preparation for
     * unserialization from the network.
     */
    public ObjectRemovedEvent ()
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
     * Returns the oid that has been removed.
     */
    public int getOid ()
    {
        return _oid;
    }

    /**
     * Applies this event to the object.
     */
    public boolean applyToObject (DObject target)
        throws ObjectAccessException
    {
        OidList list = (OidList)target.getAttribute(_name);
        list.remove(_oid);
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
        out.writeInt(_oid);
    }

    // documentation inherited
    public void readFrom (DataInputStream in)
        throws IOException
    {
        super.readFrom(in);
        _name = in.readUTF();
        _oid = in.readInt();
    }

    // documentation inherited
    protected void notifyListener (Object listener)
    {
        if (listener instanceof OidListListener) {
            ((OidListListener)listener).objectRemoved(this);
        }
    }

    // documentation inherited
    protected void toString (StringBuffer buf)
    {
        buf.append("OBJREM:");
        super.toString(buf);
        buf.append(", name=").append(_name);
        buf.append(", oid=").append(_oid);
    }

    protected String _name;
    protected int _oid;
}
