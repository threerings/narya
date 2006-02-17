package com.threerings.presents.dobj {

import flash.events.EventDispatcher;

import mx.collections.ArrayCollection;

import com.threerings.io.ObjectInputStream;
import com.threerings.io.ObjectOutputStream;
import com.threerings.io.Streamable;

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

    public function addListener (listener :ChangeListener) :void
    {
        if (_listeners == null) {
            _listeners = new ArrayCollection();

        } else if (_listeners.contains(listener)) {
            trace("Refusing repeat listener registration");
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
            var listener :* = _listeners.getItemAt(ii);
            try {
                event.notifyListener(listener);

                if (listener is EventListener) {
                    listener.eventReceived(event);
                }
            } catch (e :Error) {
                trace("Listener choked during notification");
                trace(e.getStackTrace());
            }
        }
    }

    public function postMessage (name :String, args :Array) :void
    {
        postEvent(new MessageEvent(_oid, name, args));
    }

    public function postEvent (DEvent event) :void
    {
        // TODO: transactons?
        if (_omgr != null) {
            _omgr.postEvent(event);

        } else {
            trace("Unable to post event, object has no omgr");
        }
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
            name :String, value :*, oldValue :*) :void
    {
        postEvent(new AttributeChangedEvent(_oid, name, value, oldValue));
    }

    /**
     * Called by derived instances when an element updater method was
     * called.
     */
    protected function requestElementUpdate (
            name :String, index :int, value :*, oldValue :*) :void
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
    protected function requestEntryAdd (name :String, entry :DSet.Entry) :void
    {
        // dispatch an entry added event
        postEvent(new EntryAddedEvent(_oid, name, entry, false));
    }

    /**
     * Calls by derived instances when a set remover method was called.
     */
    protected function requestEntryRemove (name :String, key :Comparable) :void
    {
        // dispatch an entry removed event
        postEvent(new EntryRemovedEvent(_oid, name, key, null));
    }

    /**
     * Calls by derived instances when a set updater method was called.
     */
    protected function requestEntryUpdate (name :String, entry :DSet.Entry)
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
}
}
