//
// $Id: DEvent.java,v 1.7 2001/10/12 00:03:03 mdb Exp $

package com.threerings.presents.dobj;

/**
 * A distributed object event is dispatched whenever any modification is
 * made to a distributed object. It can also be dispatched purely for
 * notification purposes, without making any modifications to the object
 * that defines the delivery group (the object's subscribers).
 */
public abstract class DEvent
{
    /**
     * Returns the oid of the object that is the target of this event.
     */
    public int getTargetOid ()
    {
        return _toid;
    }

    /**
     * Applies the attribute modifications represented by this event to
     * the specified target object. This is called by the distributed
     * object manager in the course of dispatching events and should not
     * be called directly.
     *
     * @exception ObjectAccessException thrown if there is any problem
     * applying the event to the object (invalid attribute, etc.).
     *
     * @return true if the object manager should go on to notify the
     * object's subscribers of this event, false if the event should be
     * treated silently and the subscribers should not be notified.
     */
    public abstract boolean applyToObject (DObject target)
        throws ObjectAccessException;

    /**
     * Returns the object id of the client that generated this event. This
     * will only be valid on the server, it will return -1 otherwise.
     */
    public int getSourceOid ()
    {
        return _soid;
    }

    /**
     * Do not call this method. Sets the source oid of the client that
     * generated this event. It is automatically called by the client
     * management code when a client forwards an event to the server.
     */
    public void setSourceOid (int sourceOid)
    {
        _soid = sourceOid;
    }

    /**
     * Constructs a new distributed object event that pertains to the
     * specified distributed object.
     */
    protected DEvent (int targetOid)
    {
        _toid = targetOid;
    }

    /**
     * Events with associated listener interfaces should implement this
     * function and notify the supplied listener if it implements their
     * event listening interface. For example, the {@link
     * AttributeChangedEvent} will notify listeners that implement
     * {@AttributeChangeListener}.
     */
    protected void notifyListener (Object listener)
    {
        // the default is to do nothing
    }

    /**
     * Constructs and returns a string representation of this event.
     */
    public String toString ()
    {
        StringBuffer buf = new StringBuffer();
        buf.append("[");
        toString(buf);
        buf.append("]");
        return buf.toString();
    }

    /**
     * This should be overridden by derived classes (which should be sure
     * to call <code>super.toString()</code>) to append the derived class
     * specific event information to the string buffer.
     */
    protected void toString (StringBuffer buf)
    {
        buf.append("targetOid=").append(_toid);
        buf.append(", sourceOid=").append(_soid);
    }

    /** The oid of the object that is the target of this event. */
    protected int _toid;

    /** The oid of the client that generated this event. */
    protected int _soid;
}
