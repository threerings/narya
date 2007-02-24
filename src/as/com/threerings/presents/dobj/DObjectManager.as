//
// $Id$
//
// Narya library - tools for developing networked games
// Copyright (C) 2002-2007 Three Rings Design, Inc., All Rights Reserved
// http://www.threerings.net/code/narya/
//
// This library is free software; you can redistribute it and/or modify it
// under the terms of the GNU Lesser General Public License as published
// by the Free Software Foundation; either version 2.1 of the License, or
// (at your option) any later version.
//
// This library is distributed in the hope that it will be useful,
// but WITHOUT ANY WARRANTY; without even the implied warranty of
// MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
// Lesser General Public License for more details.
//
// You should have received a copy of the GNU Lesser General Public
// License along with this library; if not, write to the Free Software
// Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA 02111-1307 USA

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
