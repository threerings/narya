//
// $Id: CompoundEvent.java,v 1.2 2002/02/21 00:59:29 mdb Exp $

package com.threerings.presents.dobj;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;

import java.util.ArrayList;
import java.util.List;

import com.threerings.presents.io.TypedObjectFactory;

/**
 * Used to manage and submit groups of events on a collection of
 * distributed objects in a single transaction.
 *
 * @see DObject#startTransaction
 */
public class CompoundEvent extends TypedEvent
{
    /** The typed object code for this event. */
    public static final short TYPE = TYPE_BASE + 50;

    /**
     * Constructs a compound event and prepares it for operation.
     */
    public CompoundEvent (DObjectManager omgr)
    {
        super(0); // we don't have a single target object oid
        _omgr = omgr;
    }

    /**
     * Lets the event know that this dobject is participating in their
     * transaction. The supplied dobject will have their transaction
     * cleared when this event is committed or cancelled.
     */
    public void addObject (DObject object)
    {
        if (_participants == null) {
            _participants = new ArrayList();
        }
        _participants.add(object);
    }

    /**
     * Posts an event to this transaction. The event will be delivered as
     * part of the entire transaction if it is committed or discarded if
     * the transaction is cancelled.
     */
    public void postEvent (TypedEvent event)
    {
        _events.add(event);
    }

    /**
     * Returns the list of events contained within this compound event.
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
        // first clear our participants
        clearParticipants();

        // then post this event onto the queue (but only if we actually
        // accumulated some events)
        if (_events.size() > 0) {
            _omgr.postEvent(this);
        }
    }

    /**
     * Cancels this transaction. All events posted to this transaction
     * will be discarded.
     */
    public void cancel ()
    {
        // clear our participants
        clearParticipants();
        // clear our event queue in case someone holds onto us
        _events.clear();
    }

    /**
     * Nothing to apply here.
     */
    public boolean applyToObject (DObject target)
        throws ObjectAccessException
    {
        return false;
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
        int ecount = _events.size();
        for (int i = 0; i < ecount; i++) {
            TypedEvent event = (TypedEvent)_events.get(i);
            TypedObjectFactory.writeTo(out, event);
        }
    }

    // documentation inherited
    public void readFrom (DataInputStream in)
        throws IOException
    {
        super.readFrom(in);
        int ecount = in.readInt();
        for (int i = 0; i < ecount; i++) {
            _events.add(TypedObjectFactory.readFrom(in));
        }
    }

    /**
     * Calls out to all of the participating dobjects, clearing their
     * transaction reference.
     */
    protected void clearParticipants ()
    {
        if (_participants != null) {
            int psize = _participants.size();
            for (int i = 0; i < psize; i++) {
                DObject obj = (DObject)_participants.get(i);
                obj.clearTransaction();
            }
            _participants = null;
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
    protected DObjectManager _omgr;

    /** A list of the events associated with this compound event. */
    protected ArrayList _events = new ArrayList();

    /** A list of the dobject participants in this transaction. They will
     * be notified when we are committed or cancelled so that they can
     * stop posting their events to us. */
    protected ArrayList _participants;
}
