//
// $Id: DObjectManager.java,v 1.3 2001/06/01 07:12:13 mdb Exp $

package com.threerings.cocktail.cher.dobj;

/**
 * The distributed object manager is responsible for managing the creation
 * and destruction of distributed objects and propagating dobj events to
 * the appropriate subscribers. On the client, objects are managed as
 * proxies to the real objects managed by the server, so attribute change
 * requests are forwarded to the server and events coming down from the
 * server are delivered to the local subscribers. On the server, the
 * objects are managed directly.
 */
public interface DObjectManager
{
    /**
     * Creates a distributed object instance of the supplied class and
     * notifies the specified subscriber when it becomes available. This
     * is the proper mechanism for constructing a new distributed object
     * as it is the only way in which it can be properly registered with
     * the dobj system.
     *
     * @param dclass The class object of the derived class of
     * <code>DObject</code> (or <code>DObject.class</code> itself) that
     * should be instantiated.
     * @param target The subscriber to be notified when the object is
     * created and available, or if there was a problem creating the
     * object.
     * @param subscribe If true, the subscriber will be subscribed to the
     * object after creation; if false, it will merely be notified of it's
     * availability but not added as a subscriber.
     */
    public void createObject (Class dclass, Subscriber target,
                              boolean subscribe);

    /**
     * Requests that the specified subscriber be subscribed to the object
     * identified by the supplied object id. That subscriber will be
     * notified when the object is available or if the subscription
     * request failed.
     *
     * @param oid The object id of the distributed object to which
     * subscription is desired.
     * @param target The subscriber to be subscribed.
     *
     * @see Subscriber.objectAvailable
     * @see Subscriber.requestFailed
     */
    public void subscribeToObject (int oid, Subscriber target);

    /**
     * Fetches an up-to-date copy of the specified distributed object and
     * makes it available to the subscriber for a one-time access. The
     * subscriber will not be added to the object's subscriber list and
     * will not be notified of updates to the object and the object
     * represents a snapshot in time which, it should be acknowledged,
     * could be out of date by the time it reaches the subscriber. If the
     * object cannot be fetched for some reason, the subscriber will be
     * notified via <code>requestFailed</code>.
     *
     * @param oid The object id of the distributed object of which a
     * snapshot is desired.
     * @param target The subscriber that will receive the snapshot.
     *
     * @see Subscriber.objectAvailable
     * @see Subscriber.requestFailed
     */
    public void fetchObject (int oid, Subscriber target);

    /**
     * Posts a distributed object event into the system. Instead of
     * requesting the modification of a distributed object attribute by
     * calling the setter for that attribute on the object itself, an
     * <code>AttributeChangedEvent</code> can be constructed and posted
     * directly. This is true for all event types and is useful for
     * situations where one doesn't have access to the object in question,
     * but needs to affect some event.
     *
     * <p> This event will be forwarded to the ultimate manager of the
     * object (on the client, this means it will be forwarded to the
     * server) where it will be checked for validity and then applied to
     * the object and dispatched to all its subscribers.
     *
     * @param event The event to be dispatched.
     */
    public void postEvent (DEvent event);
}
