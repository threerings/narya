//
// $Id$

package com.threerings.util {

import flash.events.TimerEvent;    
import flash.utils.Timer;

/**
 * Data structure that keeps its elements for a short time, and then removes them automatically.
 */
public class ExpiringSet
{
    /**
     * Initializes the expiring set.
     *
     * @param ttl Time to live value for set elements, in seconds.
     * @param expirationHandler Optional function to be called when elements are forcibly
     *            expired, due to exceeding their TTL. The function should be of the form:
     *              <pre>  function (element :*) :void { }  </pre>
     *            and it will be called with the element expired from the set.
     */
    public function ExpiringSet (ttl :Number, expirationHandler :Function)
    {
        _callback = expirationHandler;
        _ttl = int(ttl * 1000);

        _timer.addEventListener(TimerEvent.TIMER, expireElements);
    }

    public function empty () :Boolean
    {
        return _q.length == 0;
    }

    /** Returns true if the set already contains this element. */
    public function contains (element :*) :Boolean
    {
        for each (var e :ExpiringElement in _q) {
            if (e.equals(element)) {
                return true;
            }
        }
        return false;
    }
    
    /** If element is not present in the set, adds it and returns true; otherwise returns false. */
    public function add (element :*) :Boolean
    {
        if (contains(element)) {
            return false;
        }
        
        // push the item on the end of the queue. since each element has the same TTL,
        // elements end up being ordered by their expiration time.
        _q.push(new ExpiringElement(element, _ttl));
        _timer.start();
        return true;
    }

    /** If element is present in the set, removes it and returns true; otherwise returns false. */
    public function remove (element :*) :Boolean
    {
        var result :Boolean = false;
        
        // pull the item from anywhere in the queue
        for (var ii :int = 0; ii < _q.length; ii++) {
            var e :ExpiringElement = _q[ii] as ExpiringElement;
            if (e.equals(element)) {
                _q.splice(ii, 1);
                result = true;
                break;
            }
        }

        if (empty()) {
            _timer.stop(); // we ran out of elements. nothing left to expire.
        }

        return result;
    }

    /** Is the oldest element expired already? */
    protected function headExpired () :Boolean
    {
        return empty() ? false : (_q[0] as ExpiringElement).expired();
    }
    
    /** Called on a timer, expires any elements that exceeded their time to live. */ 
    protected function expireElements (event :TimerEvent) :void
    {
        while (headExpired()) {
            // pop the head of the queue and send it to the custom callback
            var head :ExpiringElement = _q.shift() as ExpiringElement;
            if (_callback != null) {
                _callback(head.element);
            }
        }

        if (empty()) {
            _timer.stop(); // nothing left to expire. let's wait until something is added again.
        }
    }

    /** Array of ExpiringElement instances, sorted by expiration time. */
    protected var _q :Array = new Array();

    protected var _timer :Timer = new Timer(1000); // check every second
    protected var _callback :Function;
    protected var _ttl :int;
}
}


import flash.utils.getTimer; // function import

import com.threerings.util.Util;

internal class ExpiringElement
{
    public var expirationTime :int;
    public var element :*;
    
    public function ExpiringElement (element :*, ttl :int)
    {
        this.element = element;
        this.expirationTime = getTimer() + ttl;
    }

    public function expired () :Boolean
    {
        return expirationTime < getTimer();
    }

    public function equals (element :*) :Boolean
    {
        return Util.equals(element, this.element);
    }
}

