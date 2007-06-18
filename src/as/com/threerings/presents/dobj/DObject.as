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

import flash.errors.IllegalOperationError;

import flash.events.EventDispatcher;

import com.threerings.util.ClassUtil;
import com.threerings.util.StringBuilder;

import com.threerings.io.ObjectInputStream;
import com.threerings.io.ObjectOutputStream;
import com.threerings.io.Streamable;

public class DObject // extends EventDispatcher
    implements Streamable
{
    private static const log :Log = Log.getLog(DObject);

    public function getOid ():int
    {
        return _oid;
    }

    public function getManager () :DObjectManager
    {
        return _omgr;
    }

    public function addSubscriber (sub :Subscriber) :void
    {
        if (_subscribers == null) {
            _subscribers = [ ];
        }
        if (_subscribers.indexOf(sub) == -1) {
            _subscribers.push(sub);
        }
    }

    public function removeSubscriber (sub :Subscriber) :void
    {
        if (_subscribers == null) {
            return;
        }
        var dex :int = _subscribers.indexOf(sub);
        if (dex != -1) {
            _subscribers.splice(dex, 1);
            if (_subscribers.length == 0) {
                _omgr.removedLastSubscriber(this, _deathWish);
            }
        }
    }

    public function setDestroyOnLastSubscriberRemoved (deathWish :Boolean) :void
    {
        _deathWish = deathWish;
    }

    public function addListener (listener :ChangeListener) :void
    {
        if (_listeners == null) {
            _listeners = [ ];

        } else if (_listeners.indexOf(listener) != -1) {
            log.warning("Refusing repeat listener registration " +
                "[dobj=" + which() + ", list=" + listener + "].");
            log.logStackTrace(new Error());
            return;
        }
        _listeners.push(listener);
    }

    public function removeListener (listener :ChangeListener) :void
    {
        if (_listeners != null) {
            var dex :int = _listeners.indexOf(listener);
            if (dex != -1) {
                _listeners.splice(dex, 1);
            }
        }
    }

    public function notifyListeners (event :DEvent) :void
    {
        if (_listeners == null) {
            return;
        }

        var listenersCopy :Array = _listeners.concat();
        for each (var listener :Object in listenersCopy) {
            // make sure the listener is still a listener
            // and hasn't been removed while dispatching to another listener..
            if (_listeners.indexOf(listener) == -1) {
                continue;
            }
            try {
                event.friendNotifyListener(listener);

                if (listener is EventListener) {
                    (listener as EventListener).eventReceived(event);
                }
            } catch (e :Error) {
                log.warning("Listener choked during notification " +
                    "[list=" + listener + ", event=" + event + "].");
                log.logStackTrace(e);
            }
        }
    }

    /**
     * Posts a message event on this distrubuted object.
     */
    public function postMessage (name :String, args :Array) :void
    {
        postEvent(new MessageEvent(_oid, name, args));
    }

    /**
     * Posts the specified event either to our dobject manager or to the compound event for which
     * we are currently transacting.
     */
    public function postEvent (event :DEvent) :void
    {
        if (_tevent != null) {
            _tevent.postEvent(event);

        } else if (_omgr != null) {
            _omgr.postEvent(event);

        } else {
            log.warning("Unable to post event, object has no omgr " +
                "[oid=" + getOid() + ", class=" + ClassUtil.getClassName(this) +
                ", event=" + event + "].");
        }
    }

    /**
     * Generates a concise string representation of this object.
     */
    public function which () :String
    {
        var buf :StringBuilder = new StringBuilder();
        whichBuf(buf);
        return buf.toString();
    }

    /**
     * Used to briefly describe this distributed object.
     */
    protected function whichBuf (buf :StringBuilder) :void
    {
        buf.append(ClassUtil.shortClassName(this), ":", _oid);
    }

    // documentation inherited
    public function toString () :String
    {
        var buf :StringBuilder = new StringBuilder("[");
        toStringBuf(buf);
        buf.append("]");
        return buf.toString();
    }

    /**
     * Generates a string representation of this object.
     */
    public function toStringBuf (buf :StringBuilder) :void
    {
        buf.append("oid=", _oid);
    }

    /**
     * Begins a transaction on this distributed object. In some situations, it is desirable to
     * cause multiple changes to distributed object fields in one unified operation. Starting a
     * transaction causes all subsequent field modifications to be stored in a single compound
     * event which can then be committed, dispatching and applying all included events in a single
     * group. Additionally, the events are dispatched over the network in a single unit which can
     * significantly enhance network efficiency.
     *
     * <p> When the transaction is complete, the caller must call {@link #commitTransaction} or
     * {@link CompoundEvent#commit} to commit the transaction and release the object back to its
     * normal non-transacting state. If the caller decides not to commit their transaction, they
     * must call {@link #cancelTransaction} or {@link CompoundEvent#cancel} to cancel the
     * transaction. Failure to do so will cause the pooch to be totally screwed.
     *
     * <p> Note: like all other distributed object operations, transactions are not thread safe. It
     * is expected that a single thread will handle all distributed object operations and that
     * thread will begin and complete a transaction before giving up control to unknown code which
     * might try to operate on the transacting distributed object.
     *
     * <p> Note also: if the object is already engaged in a transaction, a transaction participant
     * count will be incremented to note that an additional call to {@link #commitTransaction} is
     * required before the transaction should actually be committed. Thus <em>every</em> call to
     * {@link #startTransaction} must be accompanied by a call to either {@link #commitTransaction}
     * or {@link #cancelTransaction}. Additionally, if any transaction participant cancels the
     * transaction, the entire transaction is cancelled for all participants, regardless of whether
     * the other participants attempted to commit the transaction.
     */
    public function startTransaction () :void
    {
        if (_tevent != null) {
            _tcount++;
        } else {
            _tevent = new CompoundEvent(this, _omgr);
        }
    }

    /** 
     * Commits the transaction in which this distributed object is involved.
     *      
     * @see CompoundEvent#commit
     */ 
    public function commitTransaction () :void
    {
        if (_tevent == null) {
            throw new IllegalOperationError("Cannot commit: not involved in a transaction " +
                "[dobj=" + this + "]");
        }

        // if we are nested, we decrement our nesting count rather than committing the transaction
        if (_tcount > 0) {
            _tcount--;

        } else {
            // we may actually be doing our final commit after someone already cancelled this
            // transaction, so we need to perform the appropriate action at this point
            if (_tcancelled) {
                _tevent.cancel();
            } else {
                _tevent.commit();
            }
        }
    }

    /**
     * Returns true if this object is in the middle of a transaction or false if it is not.
     */
    public function inTransaction () :Boolean
    {
        return (_tevent != null);
    }

    /**
     * Cancels the transaction in which this distributed object is involved.
     *
     * @see CompoundEvent#cancel
     */
    public function cancelTransaction () :void
    {
        if (_tevent == null) {
            throw new IllegalOperationError("Cannot cancel: not involved in a transaction " +
                "[dobj=" + this + "]");
        }

        // if we're in a nested transaction, make a note that it is to be cancelled when all
        // parties commit and decrement the nest count
        if (_tcount > 0) {
            _tcancelled = true;
            _tcount--;

        } else {
            _tevent.cancel();
        }
    }

    /**
     * Removes this object from participation in any transaction in which it might be taking part.
     */
    internal function clearTransaction () :void
    {
        // sanity check
        if (_tcount != 0) {
            log.warning("Transaction cleared with non-zero nesting count [dobj=" + this + "].");
            _tcount = 0;
        }

        // clear our transaction state
        _tevent = null;
        _tcancelled = false;
    }

    /**
     * Returns true if this object is active and registered with the distributed object system. If
     * an object is created via <code>DObjectManager.createObject</code> it will be active until
     * such time as it is destroyed.
     */
    public final function isActive () :Boolean
    {
        return (_omgr != null);
    }

    /**
     * Don't call this function! It initializes this distributed object
     * with the supplied distributed object manager. This is called by the
     * distributed object manager when an object is created and registered
     * with the system.
     *
     * @see DObjectManager#createObject
     */
    public function setManager (omgr :DObjectManager) :void
    {
        _omgr = omgr;
    }

    /**
     * Called by derived instances when an attribute setter method was
     * called.
     */
    protected function requestAttributeChange (
            name :String, value :Object, oldValue :Object) :void
    {
        postEvent(new AttributeChangedEvent(_oid, name, value, oldValue));
    }

    /**
     * Called by derived instances when an element updater method was
     * called.
     */
    protected function requestElementUpdate (
            name :String, index :int, value :Object, oldValue :Object) :void
    {
        // dispatch an attribute changed event
        postEvent(new ElementUpdatedEvent(_oid, name, value, oldValue, index));
    }
    
    /**
     * Calls by derived instances when an oid adder method was called.
     */
    protected function requestOidAdd (name :String, oid :int) :void
    {
        // dispatch an object added event
        postEvent(new ObjectAddedEvent(_oid, name, oid));
    }
    
    /**
     * Calls by derived instances when an oid remover method was called.
     */
    protected function requestOidRemove (name :String, oid :int) :void
    {
        // dispatch an object removed event
        postEvent(new ObjectRemovedEvent(_oid, name, oid));
    }

    /**
     * Calls by derived instances when a set adder method was called.
     */
    protected function requestEntryAdd (name :String, entry :DSet_Entry) :void
    {
        // dispatch an entry added event
        postEvent(new EntryAddedEvent(_oid, name, entry));
    }

    /**
     * Calls by derived instances when a set remover method was called.
     */
    protected function requestEntryRemove (name :String, key :Object) :void
    {
        // dispatch an entry removed event
        postEvent(new EntryRemovedEvent(_oid, name, key, null));
    }

    /**
     * Calls by derived instances when a set updater method was called.
     */
    protected function requestEntryUpdate (name :String, entry :DSet_Entry) :void
    {
        // dispatch an entry updated event
        postEvent(new EntryUpdatedEvent(_oid, name, entry, null));
    }

    // documentation inherited from interface Streamable
    public final function writeObject (out :ObjectOutputStream) :void
    {
        throw new Error();
//        out.writeInt(_oid);
    }

    // documentation inherited from interface Streamable
    public function readObject (ins :ObjectInputStream) :void
    {
        _oid = ins.readInt();
    }

    protected var _oid :int;

    /** A reference to our object manager. */
    protected var _omgr :DObjectManager;

    /** Our event listeners. */
    protected var _listeners :Array;

    protected var _subscribers :Array;

    /** The compound event associated with our transaction, if we're currently in a transaction. */
    protected var _tevent :CompoundEvent;

    /** The nesting depth of our current transaction. */
    protected var _tcount :int;

    /** Whether or not our nested transaction has been cancelled. */
    protected var _tcancelled :Boolean;

    /** Indicates whether we want to be destroyed when our last subscriber is removed. */
    protected var _deathWish :Boolean = false;
}
}
