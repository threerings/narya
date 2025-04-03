//
// Narya library - tools for developing networked games
// Copyright (C) 2002-2025 Three Rings Design, Inc., All Rights Reserved
// https://github.com/threerings/narya/blob/master/LICENSE

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
 *
 * @param <T> the type object being subscribed to.
 */
public interface Subscriber<T extends DObject>
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
    void objectAvailable (T object);

    /**
     * Called when a subscription request has failed. The nature of the
     * failure will be communicated via the supplied
     * <code>ObjectAccessException</code>.
     *
     * @see DObjectManager#subscribeToObject
     */
    void requestFailed (int oid, ObjectAccessException cause);
}
