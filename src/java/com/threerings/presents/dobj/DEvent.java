//
// $Id: DEvent.java,v 1.2 2001/06/01 07:12:13 mdb Exp $

package com.threerings.cocktail.cher.dobj;

/**
 * A distributed object event is dispatched whenever any modification is
 * made to a distributed object. It can also be dispatched purely for
 * notification purposes, without making any modifications to the object
 * that defines the delivery group (the object's subscribers).
 */
public class DEvent
{
    /**
     * Returns the oid of the object that is the target of this event.
     */
    public int getTargetOid ()
    {
        return _toid;
    }

    /**
     * Constructs a new distributed object event that pertains to the
     * specified distributed object.
     */
    protected DEvent (int targetOid)
    {
        _toid = targetOid;
    }

    /** The oid of the object that is the target of this event. */
    protected int _toid;
}
