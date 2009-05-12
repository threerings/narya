//
// $Id$

package com.threerings.util {

import flash.events.EventDispatcher;
import flash.events.TimerEvent;

import flash.utils.getTimer; // function import
import flash.utils.Timer;

/**
 * Dispatched when a set element expires.
 *
 * @eventType com.threerings.util.ExpiringSet.ELEMENT_EXPIRED
 */
[Event(name="ElementExpired", type="com.threerings.util.ValueEvent")]

/**
 * Data structure that keeps its elements for a short time, and then removes them automatically.
 *
 * All operations are O(n), including add().
 */
public class ExpiringSet extends EventDispatcher
    implements Set
{
    /** The even that is dispatched when a member of this set expires. */
    public static const ELEMENT_EXPIRED :String = "ElementExpired";

    /**
     * Initializes the expiring set.
     *
     * @param ttl Time to live value for set elements, in seconds.
     */
    public function ExpiringSet (ttl :Number)
    {
        _ttl = Math.round(ttl * 1000);
        _timer = new Timer(_ttl, 1);
        _timer.addEventListener(TimerEvent.TIMER, checkTimer);
    }

    /**
     * Returns the time to live value for this ExpiringSet.  This value cannot be changed after
     * set creation.
     */
    public function get ttl () :Number
    {
        return _ttl / 1000;  
    }

    /**
     * Calling this function will not expire the elements, it simply removes them. No 
     * ValueEvent will be dispatched.
     */
    public function clear () :void
    {
        // simply trunate the data array
        _data.length = 0;
    }

    // from Set
    public function forEach (fn :Function) :void
    {
        for each (var e :ExpiringElement in _data) {
            fn(e.element);
        }
    }

    // from Set
    public function size () :int
    {
        return _data.length;
    }

    // from Set
    public function isEmpty () :Boolean
    {
        return size() == 0;
    }

    // from Set
    public function contains (o :Object) :Boolean
    {
        return _data.some(function (e :ExpiringElement, ... ignored) :Boolean {
            return e.objectEquals(o);
        });
    }
    
    /**
     * Note that if you add an object that the list already contains, this method will return 
     * false, but it will also update the expire time on that object to be this sets ttl from now, 
     * as if the item really were being added to the list now.
     */
    public function add (o :Object) :Boolean
    {
        var added :Boolean = true;
        for (var ii :int = 0; ii < _data.length; ii++) {
            if (ExpiringElement(_data[ii]).objectEquals(o)) {
                // already contained, remove this one and re-add at end
                _data.splice(ii, 1);
                added = false;
                break;
            }
        }

        // push the item onto the queue. since each element has the same TTL, elements end up 
        // being ordered by their expiration time.
        _data.push(new ExpiringElement(o, getTimer() + _ttl));
        if (_data.length == 1) {
            // set up the timer to remove this one element, otherwise it was already running
            _timer.reset();
            _timer.delay = _ttl;
            _timer.start();
        }
        return added;
    }

    // from Set
    public function remove (o :Object) :Boolean
    {
        // pull the item from anywhere in the queue.  If we remove the first element, the timer
        // will harmlessly NOOP when it wakes up
        for (var ii :int = 0; ii < _data.length; ii++) {
            if (ExpiringElement(_data[ii]).objectEquals(o)) {
                _data.splice(ii, 1);
                return true;
            }
        }
        return false;
    }

    /**
     * This implementation of Set returns a fresh array that it will never reference again.  
     * Modification of this array will not change the ExpiringSet's structure.
     */
    public function toArray () :Array
    {
        return _data.map(function (e :ExpiringElement, ... ignored) :Object {
            return e.element;
        });
    }

    /**
     * Remove probably just one element from the front of the expiration queue, and
     * schedule a wakeup for the time to the new head's expiration time.
     */
    protected function checkTimer (... ignored) :void
    {
        var now :int = getTimer();
        while (_data.length > 0) {
            var e :ExpiringElement = ExpiringElement(_data[0]);
            var timeToExpire :int = e.expirationTime - now;
            if (timeToExpire <= 0) {
                _data.shift(); // remove it
                dispatchEvent(new ValueEvent(ELEMENT_EXPIRED, e.element)); // notify

            } else {
                // the head element is not yet expired
                _timer.reset();
                _timer.delay = timeToExpire;
                _timer.start();
                break;
            }
        }
    }

    protected static const log :Log = Log.getLog(ExpiringSet);

    /** The time to live for this set, not to be changed after construction. */
    protected /* final */ var _ttl :int;

    /** Array of ExpiringElement instances, sorted by expiration time. */
    protected var _data :Array = [];

    protected var _timer :Timer;
}
}

import com.threerings.util.Util;

class ExpiringElement
{
    public var expirationTime :int;
    public var element :Object;
    
    public function ExpiringElement (element :Object, expiration :int)
    {
        this.element = element;
        this.expirationTime = expiration;
    }

    public function objectEquals (element :Object) :Boolean
    {
        return Util.equals(element, this.element);
    }
}

