//
// $Id: ObjectRemovedEvent.java,v 1.8 2002/12/20 23:29:04 mdb Exp $

package com.threerings.presents.dobj;

/**
 * An object removed event is dispatched when an object is removed from an
 * <code>OidList</code> attribute of a distributed object. It can also be
 * constructed to request the removal of an oid from an
 * <code>OidList</code> attribute of an object and posted to the dobjmgr.
 *
 * @see DObjectManager#postEvent
 */
public class ObjectRemovedEvent extends NamedEvent
{
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
        super(targetOid, name);
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
        buf.append(", oid=").append(_oid);
    }

    protected int _oid;
}
