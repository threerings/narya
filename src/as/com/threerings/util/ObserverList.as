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

package com.threerings.util {

public class ObserverList
{
    /** A notification ordering policy indicating that the observers
     * should be notified in the order they were added and that the
     * notification should be done on a snapshot of the array. */
    public static const SAFE_IN_ORDER_NOTIFY :int = 1;

    /** A notification ordering policy wherein the observers are notified
     * last to first so that they can be removed during the notification
     * process and new observers added will not inadvertently be notified
     * as well, but no copy of the observer list need be made. This will
     * not work if observers are added or removed from arbitrary positions
     * in the list during a notification call. */
    public static const FAST_UNSAFE_NOTIFY :int = 2;

    /**
     * Constructor
     */
    public function ObserverList (notifyPolicy :int = 2)
    {
        _notifyPolicy = notifyPolicy;
    }

    /**
     * Add an observer to this list.
     *
     * @param index the index at which to add the observer, or -1 for the end.
     */
    public function add (observer :Object, index :int = -1) :void
    {
        if (!ArrayUtil.contains(_list, observer)) {
            _list.splice(index, 0, observer); // -1 to splice means "end"
        }
    }

    /**
     * Return the size of the list.
     */
    public function size () :int
    {
        return _list.length;
    }

    /**
     * Remove an observer from this list.
     */
    public function remove (observer :Object) :void
    {
        ArrayUtil.removeFirst(_list, observer);
    }

    /**
     * Apply some operation to all observers.
     * The function to be passed in should expect one argument and either
     * return void or a Boolean. If returning a Boolean, returning false
     * indicates that the observer should be removed from the list.
     */
    public function apply (func :Function) :void
    {
        var list :Array = _list;
        if (_notifyPolicy == SAFE_IN_ORDER_NOTIFY) {
            list = list.concat(); // make a duplicate
            list.reverse(); // reverse it so that we start with earlier obs
        }
        for (var ii :int = list.length-1; ii >= 0; ii--) {
            try {
                var result :* = func(list[ii]);
                if (result !== undefined && !Boolean(result)) {
                    // remove it if directed to do so
                    remove(list[ii]);
                }
            } catch (err :Error) {
                var log :Log = Log.getLog(this);
                log.warning("ObserverOp choked during notification.");
                log.logStackTrace(err);
            }
        }
    }

    /** Our notification policy. */
    protected var _notifyPolicy :int;

    /** The actual list of observers. */
    protected var _list :Array = new Array();
}
}
