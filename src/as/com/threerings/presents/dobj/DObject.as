package com.threerings.presents.dobj {

import flash.events.EventDispatcher;

import flash.util.StringBuilder;

import mx.collections.ArrayCollection;

import com.threerings.util.ClassUtil;

import com.threerings.io.ObjectInputStream;
import com.threerings.io.ObjectOutputStream;
import com.threerings.io.Streamable;

import com.threerings.presents.Log;

public class DObject // extends EventDispatcher
    implements Streamable
{
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
            _subscribers = new ArrayCollection();
        }
        if (!_subscribers.contains(sub)) {
            _subscribers.addItem(sub);
        }
    }

    public function removeSubscriber (sub :Subscriber) :void
    {
        if (_subscribers == null) {
            return;
        }
        var dex :int = _subscribers.getItemIndex(sub);
        if (dex != -1) {
            _subscribers.removeItemAt(dex);
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
            _listeners = new ArrayCollection();

        } else if (_listeners.contains(listener)) {
            com.threerings.presents.Log.warning("Refusing repeat listener registration " +
                "[dobj=" + which() + ", list=" + listener + "].");
            com.threerings.presents.Log.logStackTrace(new Error());
            return;
        }
        _listeners.addItem(listener);
    }

    public function removeListener (listener :ChangeListener) :void
    {
        if (_listeners != null) {
            var dex :int = _listeners.getItemIndex(listener);
            if (dex != -1) {
                _listeners.removeItemAt(dex);
            }
        }
    }

    public function notifyListeners (event :DEvent) :void
    {
        if (_listeners == null) {
            return;
        }

        for (var ii :int = 0; ii < _listeners.length; ii++) {
            var listener :Object = _listeners.getItemAt(ii);
            try {
                event.friendNotifyListener(listener);

                if (listener is EventListener) {
                    (listener as EventListener).eventReceived(event);
                }
            } catch (e :Error) {
                com.threerings.presents.Log.warning("Listener choked during notification " +
                    "[list=" + listener + ", event=" + event + "].");
                com.threerings.presents.Log.logStackTrace(e);
            }
        }
    }

    public function postMessage (name :String, args :Array) :void
    {
        postEvent(new MessageEvent(_oid, name, args));
    }

    public function postEvent (event :DEvent) :void
    {
        // TODO: transactons?
        if (_omgr != null) {
            _omgr.postEvent(event);

        } else {
            com.threerings.presents.Log.warning("Unable to post event, object has no omgr " +
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
    public function whichBuf (buf :StringBuilder) :void
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

    public function startTransaction () :void
    {
        // TODO
    }

    public function commitTransaction () :void
    {
        // TODO
    }

    public function inTransaction () :Boolean
    {
        return false; // TODO
    }

    public function cancelTransaction () :void
    {
        // TODO
    }

    internal function clearTransaction () :void
    {
        // TODO
    }

    public function isActive () :Boolean
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
    protected function requestEntryAdd (name :String, entry :DSetEntry) :void
    {
        // dispatch an entry added event
        postEvent(new EntryAddedEvent(_oid, name, entry, false));
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
    protected function requestEntryUpdate (name :String, entry :DSetEntry)
    {
        // dispatch an entry updated event
        postEvent(new EntryUpdatedEvent(_oid, name, entry, null));
    }

    // documentation inherited from interface Streamable
    public function writeObject (out :ObjectOutputStream) :void
    {
        out.writeInt(_oid);
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
    protected var _listeners :ArrayCollection;

    protected var _subscribers :ArrayCollection;

    protected var _deathWish :Boolean = false;
}
}
