//
// $Id: Subscriber.java,v 1.7 2001/10/12 00:03:03 mdb Exp $

package com.threerings.presents.dobj;

/**
 * A subscriber is an entity that has access to a distributed object. The
 * process of obtaining access to a distributed object is an asynchronous
 * one, and changes made to an object are delivered asynchronously. By
 * registering as a subscriber to an object, an entity can react to
 * changes made to an object and ensure that their object is kept up to
 * date.
 *
 * <p> To actually receive callbacks when events are dispatched on a
 * distributed object, an entity should register itself as a listener on
 * the object once it has received its object reference.
 *
 * @see EventListener
 * @see AttributeChangeListener
 * @see SetListener
 * @see OidListListener
 */
public interface Subscriber
{
    /**
     * Called when a subscription request has succeeded and the object is
     * available. If the object was requested for subscription, the
     * subscriber can subsequently receive notifications by registering
     * itself as a listener of some sort (see {@link
     * DObject#addListener}).
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
}
