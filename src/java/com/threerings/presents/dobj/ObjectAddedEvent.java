//
// $Id: ObjectAddedEvent.java,v 1.5 2001/10/11 04:07:52 mdb Exp $

package com.threerings.presents.dobj;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;

/**
 * An object added event is dispatched when an object is added to an
 * <code>OidList</code> attribute of a distributed object. It can also be
 * constructed to request the addition of an oid to an
 * <code>OidList</code> attribute of an object and posted to the dobjmgr.
 *
 * @see DObjectManager#postEvent
 */
public class ObjectAddedEvent extends TypedEvent
{
    /** The typed object code for this event. */
    public static final short TYPE = TYPE_BASE + 4;

    /**
     * Constructs a new object added event on the specified target object
     * with the supplied oid list attribute name and object id to add.
     *
     * @param targetOid the object id of the object to whose oid list we
     * will add an oid.
     * @param name the name of the attribute (data member) to which to add
     * the specified oid.
     * @param oid the oid to add to the oid list attribute.
     */
    public ObjectAddedEvent (int targetOid, String name, int oid)
    {
        super(targetOid);
        _name = name;
        _oid = oid;
    }

    /**
     * Constructs a blank instance of this event in preparation for
     * unserialization from the network.
     */
    public ObjectAddedEvent ()
    {
    }

    /**
     * Returns the name of the oid list attribute to which an oid has been
     * added.
     */
    public String getName ()
    {
        return _name;
    }

    /**
     * Returns the oid that has been added.
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
        list.add(_oid);
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
        out.writeInt(_oid);
    }

    public void readFrom (DataInputStream in)
        throws IOException
    {
        super.readFrom(in);
        _name = in.readUTF();
        _oid = in.readInt();
    }

    protected void toString (StringBuffer buf)
    {
        buf.append("OBJADD:");
        super.toString(buf);
        buf.append(", name=").append(_name);
        buf.append(", oid=").append(_oid);
    }

    protected String _name;
    protected int _oid;
}
