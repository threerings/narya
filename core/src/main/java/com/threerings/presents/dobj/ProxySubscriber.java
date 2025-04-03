//
// Narya library - tools for developing networked games
// Copyright (C) 2002-2025 Three Rings Design, Inc., All Rights Reserved
// https://github.com/threerings/narya/blob/master/LICENSE

package com.threerings.presents.dobj;

import com.threerings.presents.data.ClientObject;

/**
 * Defines a special kind of subscriber that proxies events for a subordinate distributed object
 * manager. All events dispatched on objects with which this subscriber is registered are passed
 * along to the subscriber for delivery to its subordinate manager.
 *
 * @see DObject#addListener
 */
public interface ProxySubscriber extends Subscriber<DObject>
{
    /**
     * Called when any event has been dispatched on an object.
     *
     * @param event The event that was dispatched on the object.
     */
    void eventReceived (DEvent event);

    /**
     * Returns the client object that represents the subscriber for whom we are proxying.
     */
    ClientObject getClientObject ();
}
