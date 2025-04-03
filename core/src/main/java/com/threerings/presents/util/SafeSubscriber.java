//
// Narya library - tools for developing networked games
// Copyright (C) 2002-2025 Three Rings Design, Inc., All Rights Reserved
// https://github.com/threerings/narya/blob/master/LICENSE

package com.threerings.presents.util;

import com.samskivert.util.StringUtil;

import com.threerings.presents.dobj.ChangeListener;
import com.threerings.presents.dobj.DObject;
import com.threerings.presents.dobj.DObjectManager;
import com.threerings.presents.dobj.ObjectAccessException;
import com.threerings.presents.dobj.Subscriber;

import static com.threerings.presents.Log.log;

/**
 * A class that safely handles the asynchronous subscription to a distributed object when it is
 * not know if the subscription will complete before the subscriber decides they no longer wish to
 * be subscribed.
 *
 * @param <T> the type of object to which we are subscribing.
 */
public class SafeSubscriber<T extends DObject>
    implements Subscriber<T>
{
    /**
     * Creates a safe subscriber for the specified distributed object which will interact with the
     * specified subscriber. If any listeners are given, they'll be added as listeners to the
     * distributed object after the subscriber is told it's available, and will be removed when
     * unsubscribing from the object.
     */
    public SafeSubscriber (int oid, Subscriber<T> subscriber, ChangeListener...listeners)
    {
        // make sure they're not fucking us around
        if (oid <= 0) {
            throw new IllegalArgumentException(
                "Invalid oid provided to safesub [oid=" + oid + "]");
        }
        if (subscriber == null) {
            throw new IllegalArgumentException(
                "Null subscriber provided to safesub [oid=" + oid + "]");
        }

        _oid = oid;
        _subscriber = subscriber;
        _listeners = listeners;
    }

    /**
     * Returns true if we are currently subscribed to our object (or in
     * the process of obtaining a subscription).
     */
    public boolean isActive ()
    {
        return _active;
    }

    /**
     * Initiates the subscription process.
     */
    public void subscribe (DObjectManager omgr)
    {
        if (_active) {
            log.warning("Active safesub asked to resubscribe " + this + ".", new Exception());
            return;
        }

        // note that we are now again in the "wishing to be subscribed" state
        _active = true;

        // make sure we dont have an object reference (which should be
        // logically impossible)
        if (_object != null) {
            log.warning("Incroyable! A safesub has an object and was " +
                        "non-active!? " + this + ".", new Exception());
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
    public void unsubscribe (DObjectManager omgr)
    {
        if (!_active) {
            // we may be non-active and have no object which could mean
            // that subscription failed; in which case we don't want to
            // complain about anything, just quietly ignore the
            // unsubscribe request
            if (_object == null && !_pending) {
                return;
            }
            log.warning("Inactive safesub asked to unsubscribe " + this + ".", new Exception());
        }

        // note that we no longer desire to be subscribed
        _active = false;

        if (_pending) {
            // make sure we don't have an object reference
            if (_object != null) {
                log.warning("Incroyable! A safesub has an object and is " +
                            "pending!? " + this + ".", new Exception());
            } else {
                // nothing to do but wait for the subscription to complete
                // at which point we'll pitch the object post-haste
                return;
            }
        }

        // make sure we have our object
        if (_object == null) {
            log.warning("Zut alors! A safesub _was_ active and not " +
                        "pending yet has no object!? " + this + ".", new Exception());
            // nothing to do since we're apparently already unsubscribed
            return;
        }

        // clear out any listeners we added
        for (ChangeListener listener : _listeners) {
            _object.removeListener(listener);
        }

        // finally effect our unsubscription
        _object = null;
        omgr.unsubscribeFromObject(_oid, this);
    }

    // documentation inherited from interface
    public void objectAvailable (T object)
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
            DObjectManager omgr = object.getManager();
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
        for (ChangeListener listener : _listeners) {
            _object.addListener(listener);
        }
    }

    // documentation inherited from interface
    public void requestFailed (int oid, ObjectAccessException cause)
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

    @Override
    public String toString ()
    {
        return "[oid=" + _oid +
            ", sub=" + StringUtil.shortClassName(_subscriber) +
            ", active=" + _active + ", pending=" + _pending +
            ", dobj=" + StringUtil.shortClassName(_object) + "]";
    }

    protected ChangeListener[] _listeners;
    protected int _oid;
    protected Subscriber<T> _subscriber;
    protected T _object;
    protected boolean _active;
    protected boolean _pending;
}
