//
// $Id: ProxySubscriber.java,v 1.1 2003/04/10 17:48:42 mdb Exp $

package com.threerings.presents.dobj;

/**
 * Defines a special kind of subscriber that proxies events for a
 * subordinate distributed object manager. All events dispatched on
 * objects with which this subscriber is registered are passed along to
 * the subscriber for delivery to its subordinate manager.
 *
 * @see DObject#addListener
 */
public interface ProxySubscriber extends Subscriber
{
    /**
     * Called when any event has been dispatched on an object.
     *
     * @param event The event that was dispatched on the object.
     */
    public void eventReceived (DEvent event);
}
