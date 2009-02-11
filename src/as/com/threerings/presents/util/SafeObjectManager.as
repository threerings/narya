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

package com.threerings.presents.util {

import flash.utils.Dictionary;
import com.threerings.presents.dobj.DObject;
import com.threerings.presents.dobj.DObjectManager;
import com.threerings.presents.dobj.ObjectAccessError;
import com.threerings.presents.dobj.ObjectDeathListener;
import com.threerings.presents.dobj.ObjectDestroyedEvent;
import com.threerings.presents.dobj.SubscriberAdapter;
import com.threerings.util.Log;

/**
 * Safely manages subscriptions and provides access to objects.
 */
public class SafeObjectManager
{
    /**
     * Creates a new manager. The optional available and failed parameters are callbacks
     * that must match the <code>Subscriber</code> interface methods.
     *
     * @param omgr the underlying object manager to use for requesting objects
     * @param log sink for warnings and info messages
     * @param available optional callback for when an object becomes available
     * @param failed optional callback for when a subscription request fails
     *
     * @see Subscriber
     */
    public function SafeObjectManager (
        omgr :DObjectManager, log :Log, available :Function = null, failed :Function = null)
    {
        _omgr = omgr;
        _log = log;
        _available = available;
        _failed = failed;
    }


    /**
     * Safely requests the given object from the manager. If an <code>available</code> callback was 
     * provided to the constructor, it will be invoked when the request is fulfilled. If a 
     * <code>failed</code> callback was provided, it will be invoked if the request fails. After 
     * and only after the request succeeds, <code>getObj</code> will return the object when called
     * with this id. The optional callbacks must match the signatures of the ones from 
     * <code>Subscriber</code>. If provided, they will be called in addition to the ones provided to
     * <code>SafeObjectManager</code>.
     * @param oid the id of the object to request.
     * @param available an optional callback to be invoked when the object is ready
     * @param failed an optional callback to be invoked if the request fails
     */
    public function subscribe (
        oid :int, available :Function=null, failed :Function=null) :void
    {
        if (_entries[oid] != null ) {
            _log.warning("Object already subscribed", "oid", oid);
            return;
        }

        var sub :SafeSubscriber = new SafeSubscriber(oid, _adapter);
        var entry :Entry = new Entry(sub);
        entry.availableFn = available;
        entry.failedFn = failed;
        sub.subscribe(_omgr);
        _log.debug("Subscribing", "sub", sub);
        _entries[oid] = entry;
    }

    /**
     * Safely notifies the manager that an object is no longer needed. The <code>getObj</code> 
     * call will subsequently return null for this id.
     * @param oid the id of the object that is no longer needed
     */
    public function unsubscribe (oid :int) :void
    {
        var entry :Entry = _entries[oid] as Entry;
        if (entry == null) {
            _log.warning("Object not subscribed", "oid", oid);
            return;
        }

        _log.debug("Unsubscribing", "sub", entry.sub);
        if (entry.obj != null) {
            entry.obj.removeListener(_objectDeathListenerAdapter);
        }
        if (!entry.destroyed && !entry.failed) {
            entry.sub.unsubscribe(_omgr);
        }
        entry.obj = null;

        delete _entries[oid];
    }

    /**
     * Safely terminates the subscriptions for all objects.
     */
    public function unsubscribeAll () :void
    {
        _log.debug("Unsubscribing all objects");

        // get the keys (integer ids of subscribed objects)
        var ids :Array = new Array();
        for (var id :* in _entries) {
            ids.push(id);
        }

        // now unsubscribe them all
        for each (var ii :* in ids) {
            unsubscribe(ii as int);
        }
    }

    /**
     * Accesses an object. This returns non-null if the object corresponding to the id has been 
     * subscribed using <code>subscribe</code> and the subscription was completed successfully. It
     * will return null if the subscription has not yet succeeded or the object has been 
     * unsubscribed using <code>unsubscribe</code> or <code>unsubscribeAll</code>.
     */
    public function getObj (oid :int) :DObject
    {
        var entry :Entry = _entries[oid];
        if (entry != null) {
            return entry.obj;
        }

        return null;
    }

    /** Notifies the manager that an object subscription request has succeeded */
    protected function available (obj :DObject) :void
    {
        var entry :Entry = _entries[obj.getOid()];
        if (entry == null) {
            _log.warning("Object available without request", "obj", obj.which());

        } else {
            _log.debug("Object now available", "obj", obj.which());
            entry.obj = obj;
        }

        obj.addListener(_objectDeathListenerAdapter);

        if (_available != null) {
            _available(obj);
        }

        if (entry.availableFn != null) {
            entry.availableFn(obj);
        }
    }

    /** Notifies the manager that an object subscription request has failed */
    protected function failed (oid :int, cause :ObjectAccessError) :void
    {
        var entry :Entry = _entries[oid];
        if (entry == null) {
            _log.warning("Object failed without request", "oid", oid);

        } else {
            entry.failed = true;
        }

        _log.warning("Failed to subscribe", "oid", oid, cause);

        if (_failed != null) {
            _failed(oid, cause);
        }

        if (entry.failedFn != null) {
            entry.failedFn(oid, cause);
        }
    }

    /** Notifies the manager that the server has destroyed an object */
    protected function destroyed (event :ObjectDestroyedEvent) :void
    {
        var oid :int = event.getTargetOid();
        
        var entry :Entry = _entries[oid] as Entry;
        if (entry == null) {
            _log.warning("Unsubscribed object notified of destroy", "oid", oid);

        } else {
            entry.destroyed = true;
            entry.obj = null;
        }

        _log.debug("Object destroyed", "oid", destroyed);
    }

    protected var _omgr :DObjectManager;
    protected var _log :Log;
    protected var _available :Function;
    protected var _failed :Function;
    protected var _adapter :SubscriberAdapter = new SubscriberAdapter(available, failed);
    protected var _objectDeathListenerAdapter :ObjectDeathListenerAdapter = 
        new ObjectDeathListenerAdapter(destroyed);
    protected var _entries :Dictionary = new Dictionary();
}
}

import com.threerings.presents.dobj.DObject;
import com.threerings.presents.dobj.ObjectDeathListener;
import com.threerings.presents.dobj.ObjectDestroyedEvent;
import com.threerings.presents.util.SafeSubscriber;

class Entry
{
    public var sub :SafeSubscriber;
    public var obj :DObject;
    public var destroyed :Boolean;
    public var failed :Boolean;
    public var availableFn :Function;
    public var failedFn :Function;

    public function Entry (subscriber :SafeSubscriber)
    {
        sub = subscriber;
    }
}

class ObjectDeathListenerAdapter
    implements ObjectDeathListener
{
    public var callback :Function;

    public function ObjectDeathListenerAdapter (callback :Function)
    {
        this.callback = callback;
    }

    public function objectDestroyed (event :ObjectDestroyedEvent) :void
    {
        callback(event);
    }
}
