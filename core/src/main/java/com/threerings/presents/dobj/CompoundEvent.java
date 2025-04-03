//
// Narya library - tools for developing networked games
// Copyright (C) 2002-2025 Three Rings Design, Inc., All Rights Reserved
// https://github.com/threerings/narya/blob/master/LICENSE

package com.threerings.presents.dobj;

import java.util.List;

import com.threerings.util.StreamableArrayList;

import com.threerings.presents.net.Transport;

/**
 * Used to manage and submit groups of events on a collection of distributed objects in a single
 * transaction.
 *
 * @see DObject#startTransaction
 */
public class CompoundEvent extends DEvent
{
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
        _events = StreamableArrayList.newList();
    }

    /** Used when unserializing. */
    public CompoundEvent ()
    {
        super(0);
    }

    /**
     * Posts an event to this transaction. The event will be delivered as part of the entire
     * transaction if it is committed or discarded if the transaction is cancelled.
     */
    public void postEvent (DEvent event)
    {
        _events.add(event);
    }

    /**
     * Returns the list of events contained within this compound event.
     *
     * Don't mess with it.
     */
    public List<DEvent> getEvents ()
    {
        return _events;
    }

    /**
     * Commits this transaction by posting this event to the distributed object event queue. All
     * participating dobjects will have their transaction references cleared and will go back to
     * normal operation.
     */
    public void commit ()
    {
        // first clear our target
        clearTarget();

        // then post this event onto the queue (but only if we actually
        // accumulated some events)
        int size = _events.size();
        switch (size) {
        case 0: // nothing doing
            break;
        case 1: // no point in being compound
            _omgr.postEvent(_events.get(0));
            break;
        default: // now we're talking
            _transport = _events.get(0).getTransport();
            for (int ii = 1; ii < size; ii++) {
                _transport = _events.get(ii).getTransport().combine(_transport);
            }
            _omgr.postEvent(this);
            break;
        }
    }

    /**
     * Cancels this transaction. All events posted to this transaction will be discarded.
     */
    public void cancel ()
    {
        // clear our target
        clearTarget();
        // clear our event queue in case someone holds onto us
        _events.clear();
    }

    @Override
    public void setSourceOid (int sourceOid)
    {
        super.setSourceOid(sourceOid);

        // we need to propagate our source oid to our constituent events
        int ecount = _events.size();
        for (int ii = 0; ii < ecount; ii++) {
            _events.get(ii).setSourceOid(sourceOid);
        }
    }

    @Override
    public void setTargetOid (int targetOid)
    {
        super.setTargetOid(targetOid);

        // we need to propagate our target oid to our constituent events
        int ecount = _events.size();
        for (int ii = 0; ii < ecount; ii++) {
            _events.get(ii).setTargetOid(targetOid);
        }
    }

    @Override
    public DEvent setTransport (Transport transport)
    {
        super.setTransport(transport);
        for (int ii = 0, nn = _events.size(); ii < nn; ii++) {
            _events.get(ii).setTransport(transport);
        }
        return this;
    }

    @Override
    public boolean applyToObject (DObject target)
        throws ObjectAccessException
    {
        // nothing to apply here
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

    @Override
    protected void toString (StringBuilder buf)
    {
        buf.append("COMPOUND:");
        super.toString(buf);
        for (int ii = 0, nn = _events.size(); ii < nn; ii++) {
            buf.append(", ").append(_events.get(ii));
        }
    }

    /** The object manager that we'll post ourselves to when we're committed. */
    protected transient DObjectManager _omgr;

    /** The object for which we're managing a transaction. */
    protected transient DObject _target;

    /** A list of the events associated with this compound event. */
    protected StreamableArrayList<DEvent> _events;
}
