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

import com.threerings.presents.dobj.DObject;
import com.threerings.presents.dobj.DObjectManager;
import com.threerings.presents.dobj.ObjectAccessError;
import com.threerings.presents.dobj.Subscriber;

/**
 * A class that safely handles the asynchronous subscription to a
 * distributed object when it is not know if the subscription will
 * complete before the subscriber decides they no longer wish to be
 * subscribed.
 */
public class SafeSubscriber implements Subscriber
{
    private static const log :Log = Log.getLog(SafeSubscriber);
    /**
     * Creates a safe subscriber for the specified distributed object
     * which will interact with the specified subscriber.
     */
    public function SafeSubscriber (oid :int, subscriber :Subscriber)
    {
        // make sure they're not fucking us around
        if (oid <= 0) {
            throw new ArgumentError(
                "Invalid oid provided to safesub [oid=" + oid + "]");
        }
        if (subscriber == null) {
            throw new ArgumentError(
                "Null subscriber provided to safesub [oid=" + oid + "]");
        }

        _oid = oid;
        _subscriber = subscriber;
    }

    /**
     * Returns true if we are currently subscribed to our object (or in
     * the process of obtaining a subscription).
     */
    public function isActive () :Boolean
    {
        return _active;
    }

    /**
     * Initiates the subscription process.
     */
    public function subscribe (omgr :DObjectManager) :void
    {
        if (_active) {
            log.warning("Active safesub asked to resubscribe " + this + ".");
            return;
        }

        // note that we are now again in the "wishing to be subscribed" state
        _active = true;

        // make sure we dont have an object reference (which should be
        // logically impossible)
        if (_object != null) {
            log.warning("Incroyable! A safesub has an object and was " +
                        "non-active!? " + this + ".");
            // make do in the face of insanity
            _subscriber.objectAvailable(_object);
            return;
        }

        if (_pending) {
            // we were previously asked to subscribe, then they asked to
            // unsubscribe and now they've asked to subscribe again, all
            // before the original subscription even completed; we need do
            // nothing here except as the original subscription request
            // will eventually come through and all will be well
            return;
        }

        // we're not pending and we just became active, that means we need
        // to request to subscribe to our object
        _pending = true;
        omgr.subscribeToObject(_oid, this);
    }

    /**
     * Terminates the object subscription. If the initial subscription has
     * not yet completed, the desire to terminate will be noted and the
     * subscription will be terminated as soon as it completes.
     */
    public function unsubscribe (omgr :DObjectManager) :void
    {
        if (!_active) {
            // we may be non-active and have no object which could mean
            // that subscription failed; in which case we don't want to
            // complain about anything, just quietly ignore the
            // unsubscribe request
            if (_object == null && !_pending) {
                return;
            }
            log.warning("Inactive safesub asked to unsubscribe " + this + ".");
            Log.dumpStack();
        }

        // note that we no longer desire to be subscribed
        _active = false;

        if (_pending) {
            // make sure we don't have an object reference
            if (_object != null) {
                log.warning("Incroyable! A safesub has an object and is " +
                            "pending!? " + this + ".");
                Log.dumpStack();
            } else {
                // nothing to do but wait for the subscription to complete
                // at which point we'll pitch the object post-haste
                return;
            }
        }

        // make sure we have our object
        if (_object == null) {
            log.warning("Zut alors! A safesub _was_ active and not " +
                        "pending yet has no object!? " + this + ".");
            Log.dumpStack();
            // nothing to do since we're apparently already unsubscribed
            return;
        }

        // finally effect our unsubscription
        _object = null;
        omgr.unsubscribeFromObject(_oid, this);
    }

    // documentation inherited from interface
    public function objectAvailable (object :DObject) :void
    {
        // make sure life is not too cruel
        if (_object != null) {
            log.warning("Madre de dios! Our object came available but " +
                        "we've already got one!? " + this);
            // go ahead and pitch the old one, God knows what's going on
            _object = null;
        }
        if (!_pending) {
            log.warning("J.C. on a pogo stick! Our object came available " +
                        "but we're not pending!? " + this);
            // go with our badselves, it's the only way
        }

        // we're no longer pending
        _pending = false;

        // if we are no longer active, we don't want this damned thing
        if (!_active) {
            var omgr :DObjectManager = object.getManager();
            // if the object's manager is null, that means the object is
            // already destroyed and we need not trouble ourselves with
            // unsubscription as it has already been pitched to the dogs
            if (omgr != null) {
                omgr.unsubscribeFromObject(_oid, this);
            }
            return;
        }

        // otherwise the air is fresh and clean and we can do our job
        _object = object;
        _subscriber.objectAvailable(object);
    }

    // documentation inherited from interface
    public function requestFailed (oid :int, cause :ObjectAccessError) :void
    {
        // do the right thing with our pending state
        if (!_pending) {
            log.warning("Criminy creole! Our subscribe failed but we're " +
                        "not pending!? " + this);
            // go with our badselves, it's the only way
        }
        _pending = false;

        // if we're active, let our subscriber know that the shit hit the fan
        if (_active) {
            // deactivate ourselves as we never got our object (and thus
            // the real subscriber need not call unsubscribe())
            _active = false;
            _subscriber.requestFailed(oid, cause);
        }
    }

    /**
     * Returns a string representation of this instance.
     */
    public function toString () :String
    {
        return "[oid=" + _oid + ", active=" + _active + ", pending=" + _pending + ", ]";
    }

    protected var _oid :int
    protected var _subscriber :Subscriber;
    protected var _object :DObject;;
    protected var _active :Boolean;
    protected var _pending :Boolean;
}
}
