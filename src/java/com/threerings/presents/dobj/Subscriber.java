//
// $Id$
//
// Narya library - tools for developing networked games
// Copyright (C) 2002-2004 Three Rings Design, Inc., All Rights Reserved
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
    public void objectAvailable (T object);

    /**
     * Called when a subscription request has failed. The nature of the
     * failure will be communicated via the supplied
     * <code>ObjectAccessException</code>.
     *
     * @see DObjectManager#subscribeToObject
     */
    public void requestFailed (int oid, ObjectAccessException cause);
}
