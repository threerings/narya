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
    }

    /**
     * Returns the time to live value for this ExpiringSet.  This value cannot be changed after
     * set creation.
     */
    public function get ttl () :Number
    {
        return _ttl / 1000;  
    }

    // from Set
    public function isEmpty () :Boolean
    {
        return size() == 0;
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
    public function size () :int
    {
        return _data.length;
    }

    // from Set
    public function contains (o :Object) :Boolean
    {
        for each (var e :ExpiringElement in _data) {
            if (e.objectEquals(o)) {
                return true;
            }
        }
        return false;
    }
    
    /**
     * Note that if you add an object that the list already contains, this method will return 
     * false, but it will also update the expire time on that object to be this sets ttl from now, 
     * as if the item really were being added to the list now.
     */
    public function add (o :Object) :Boolean
    {
        var added :Boolean = true;
        var element :ExpiringElement;
        var expire :int = getTimer() + _ttl;
        for (var ii :int = 0; ii < _data.length; ii++) {
            if ((_data[ii] as ExpiringElement).objectEquals(o)) {
                // already contained - update expire time and remove from current position.
                element = _data[ii] as ExpiringElement;
                element.expirationTime = expire;
                _data.splice(ii, 1);
                added = false;
                break;
            }
        }

        // push the item onto the queue. since each element has the same TTL, elements end up 
        // being ordered by their expiration time.
        _data.push(element || new ExpiringElement(o, expire));
        checkTimer();
        return added;
    }

    // from Set
    public function remove (o :Object) :Boolean
    {
        // pull the item from anywhere in the queue.  If we remove the first element, the timer
        // will harmlessly NOOP when it wakes up
        for (var ii :int = 0; ii < _data.length; ii++) {
            var e :ExpiringElement = _data[ii] as ExpiringElement;
            if (e.objectEquals(o)) {
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
        var elements :Array = [];
        for each (var element :ExpiringElement in _data) {
            elements.push(element.element);
        }
        return elements;
    }

    protected function checkTimer (...ignored) :void
    {
        // expiration check
        var now :int = getTimer();
        while (headIsExpired(now)) {
            // pop the head off the queue and dispatch an event
            var head :ExpiringElement = _data.shift() as ExpiringElement;
            dispatchEvent(new ValueEvent(ELEMENT_EXPIRED, head.element));
        }

        // empty check
        if (isEmpty()) {
            return;
        }

        // if the timer is already running, and we're not empty, we want to just let it finish
        // its current delay, and set up a new one then
        if (_timer != null && _timer.running) {
            return;
        }

        // sanity check
        var delay :int = (_data[0] as ExpiringElement).expirationTime - now;
        if (delay <= 0) {
            // blow up
            log.warning("calculated delay after expiring elements is invalid! [" + delay + "]");
            return;
        }

        // set up next delay
        if (_timer == null) {
            _timer = new Timer(delay, 1);
            _timer.addEventListener(TimerEvent.TIMER, checkTimer);
        } else {
            _timer.delay = delay;
            _timer.reset();
        }
        _timer.start();
    }

    protected function headIsExpired (now :int) :Boolean
    {
        return isEmpty() ? false : (_data[0] as ExpiringElement).isExpired(now);
    }

    protected static const log :Log = Log.getLog(ExpiringSet);

    /** The time to live for this set, not to be changed after construction. */
    protected /* final */ var _ttl :int;

    /** Array of ExpiringElement instances, sorted by expiration time. */
    protected var _data :Array = new Array();

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

    public function isExpired (now :int) :Boolean
    {
        return expirationTime <= now;
    }

    public function objectEquals (element :Object) :Boolean
    {
        return Util.equals(element, this.element);
    }
}

