//
// $Id: Subscriber.java,v 1.6 2001/10/11 04:07:52 mdb Exp $

package com.threerings.presents.dobj;

/**
 * A subscriber is an entity that has access to a distributed object. The
 * process of obtaining access to a distributed object is an asynchronous
 * one, and changes made to an object are delivered asynchronously. By
 * registering as a subscriber to an object, an entity can react to
 * changes made to an object and ensure that their object is kept up to
 * date.
 */
public interface Subscriber
{
    /**
     * Called when a subscription request has succeeded and the object is
     * available. If the object was requested for subscription, the
     * subscriber will subsequently receive notifications via
     * <code>handleEvent</code>. If the object was requested as a one-time
     * read-only copy, these updates will not occur and the subscriber
     * should not attempt to modify the object.
     *
     * @see DObjectManager#subscribeToObject
     */
    public void objectAvailable (DObject object);

    /**
     * Called when a subscription request has failed. The nature of the
     * failure will be communicated via the supplied
     * <code>ObjectAccessException</code>.
     *
     * @see DObjectManager#subscribeToObject
     */
    public void requestFailed (int oid, ObjectAccessException cause);

    /**
     * Called when an event has been dispatched on an object. The event
     * will be of the derived class that corresponds to the kind of event
     * that occurred on the object. <code>handleEvent</code> will be
     * called <em>after</em> the event has been applied to the object. So
     * fetching an attribute upon receiving an attribute changed event
     * will provide the new value for the attribute.
     *
     * <p> A subscriber should return true from <code>handleEvent</code>
     * unless they wish their subscription to be terminated.
     *
     * @param event The event dispatched on the object.
     * @param target The object on which the event was dispatched.
     *
     * @return true if the subscriber wishes to remain subscribed to the
     * target object, false if they do not.
     */
    public boolean handleEvent (DEvent event, DObject target);
}
