//
// $Id: CompoundEvent.java,v 1.11 2004/08/27 02:20:20 mdb Exp $
//
// Narya library - tools for developing networked games
// Copyright (C) 2002-2004 Three Rings Design, Inc., All Rights Reserved
// http://www.threerings.net/code/narya/
//
// This library is free software; you can redistribute it and/or modify it
// under the terms of the GNU Lesser General Public License as published
// by the Free Software Foundation; either version 2.1 of the License, or
// (at your option) any later version.
//
// This library is distributed in the hope that it will be useful,
// but WITHOUT ANY WARRANTY; without even the implied warranty of
// MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
// Lesser General Public License for more details.
//
// You should have received a copy of the GNU Lesser General Public
// License along with this library; if not, write to the Free Software
// Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA 02111-1307 USA

package com.threerings.presents.dobj;

import java.util.List;

import com.threerings.util.StreamableArrayList;

/**
 * Used to manage and submit groups of events on a collection of
 * distributed objects in a single transaction.
 *
 * @see DObject#startTransaction
 */
public class CompoundEvent extends DEvent
{
    /**
     * Constructs a blank compound event in preparation for
     * unserialization.
     */
    public CompoundEvent ()
    {
    }

    /**
     * Constructs a compound event and prepares it for operation.
     */
    public CompoundEvent (DObject target, DObjectManager omgr)
    {
        super(target.getOid());

        // sanity check
        if (omgr == null) {
            String errmsg = "Must receive non-null object manager reference";
            throw new IllegalArgumentException(errmsg);
        }

        _omgr = omgr;
        _target = target;
        _events = new StreamableArrayList();
    }

    /**
     * Posts an event to this transaction. The event will be delivered as
     * part of the entire transaction if it is committed or discarded if
     * the transaction is cancelled.
     */
    public void postEvent (DEvent event)
    {
        _events.add(event);
    }

    /**
     * Returns the list of events contained within this compound event.
     * Don't mess with it.
     */
    public List getEvents ()
    {
        return _events;
    }

    /**
     * Commits this transaction by posting this event to the distributed
     * object event queue. All participating dobjects will have their
     * transaction references cleared and will go back to normal
     * operation.
     */
    public void commit ()
    {
        // first clear our target
        clearTarget();

        // then post this event onto the queue (but only if we actually
        // accumulated some events)
        switch (_events.size()) {
        case 0: // nothing doing
            break;
        case 1: // no point in being compound
            _omgr.postEvent((DEvent)_events.get(0));
            break;
        default: // now we're talking
            _omgr.postEvent(this);
            break;
        }
    }

    /**
     * Cancels this transaction. All events posted to this transaction
     * will be discarded.
     */
    public void cancel ()
    {
        // clear our target
        clearTarget();
        // clear our event queue in case someone holds onto us
        _events.clear();
    }

    /**
     * We need to propagate our source oid to our constituent events.
     */
    public void setSourceOid (int sourceOid)
    {
        super.setSourceOid(sourceOid);

        int ecount = _events.size();
        for (int i = 0; i < ecount; i++) {
            ((DEvent)_events.get(i)).setSourceOid(sourceOid);
        }
    }

    /**
     * Nothing to apply here.
     */
    public boolean applyToObject (DObject target)
        throws ObjectAccessException
    {
        return false;
    }

    /**
     * Calls out to our target object, clearing its transaction reference.
     */
    protected void clearTarget ()
    {
        if (_target != null) {
            _target.clearTransaction();
            _target = null;
        }
    }

    // documentation inherited
    protected void toString (StringBuffer buf)
    {
        buf.append("COMPOUND:");
        super.toString(buf);
        for (int i = 0; i < _events.size(); i++) {
            buf.append(", ").append(_events.get(i));
        }
    }

    /** The object manager that we'll post ourselves to when we're
     * committed. */
    protected transient DObjectManager _omgr;

    /** The object for which we're managing a transaction. */
    protected transient DObject _target;

    /** A list of the events associated with this compound event. */
    protected StreamableArrayList _events;
}
