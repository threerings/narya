//
// $Id: CompoundEvent.java,v 1.6 2002/10/04 01:32:15 mdb Exp $

package com.threerings.presents.dobj;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import com.threerings.io.ObjectInputStream;
import com.threerings.io.ObjectOutputStream;
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
    public CompoundEvent (DObjectManager omgr)
    {
        super(0); // we don't have a single target object oid

        // sanity check
        if (omgr == null) {
            String errmsg = "Must receive non-null object manager reference";
            throw new IllegalArgumentException(errmsg);
        }

        _omgr = omgr;
        _events = new StreamableArrayList();
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
    public void postEvent (DEvent event)
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
     * Writes our custom streamable fields.
     */
    public void writeObject (ObjectOutputStream out)
        throws IOException
    {
        super.writeObject(out);
        out.writeObject(_events);
    }

    /**
     * Reads our custom streamable fields.
     */
    public void readObject (ObjectInputStream in)
        throws IOException, ClassNotFoundException
    {
        super.readObject(in);
        _events = (StreamableArrayList)in.readObject();
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
    protected transient DObjectManager _omgr;

    /** A list of the dobject participants in this transaction. They will
     * be notified when we are committed or cancelled so that they can
     * stop posting their events to us. */
    protected transient ArrayList _participants;

    /** A list of the events associated with this compound event. */
    protected StreamableArrayList _events;
}
