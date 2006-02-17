package com.threerings.presents.dobj {

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
     * Returns true if this distributed object manager is the
     * authoritative manager for the specified distributed object, or fals
     * if we are only providing a proxy to the object.
     */
    function isManager (obj :DObject) :Boolean;

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
     */
    function createObject (dclass :Class, target :Subscriber) :void;

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
     * @see Subscriber#objectAvailable
     * @see Subscriber#requestFailed
     */
    function subscribeToObject (oid :int, target :Subscriber) :void;

    /**
     * Requests that the specified subscriber be unsubscribed from the
     * object identified by the supplied object id.
     *
     * @param oid The object id of the distributed object from which
     * unsubscription is desired.
     * @param target The subscriber to be unsubscribed.
     */
    function unsubscribeFromObject (oid :int, target :Subscriber) :void;

    /**
     * Requests that the specified object be destroyed. Once destroyed an
     * object is removed from the runtime system and may no longer have
     * events dispatched on it.
     *
     * @param oid The object id of the distributed object to be destroyed.
     */
    function destroyObject (oid :int) :void;

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
    function postEvent (event :DEvent) :void;

    /**
     * When a distributed object removes its last subscriber, it will call
     * this function to let the object manager know. The manager might
     * then choose to flush this object from the system or unregister from
     * some upstream manager whose object it was proxying, for example.
     */
    function removedLastSubscriber (obj :DObject, deathWish :Boolean) :void;
}
}
