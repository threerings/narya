//
// $Id$
//
// Narya library - tools for developing networked games
// Copyright (C) 2002-2009 Three Rings Design, Inc., All Rights Reserved
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

/**
 * A HashMap that stores values weakly: if not referenced anywhere else, they may
 * be garbage collected. Note that the size might change unexpectedly as things are removed.
 */
public class WeakValueHashMap extends HashMap
{
    /**
     * @inheritDoc
     */
    public function WeakValueHashMap (
        loadFactor :Number = 1.75,
        equalsFn :Function = null,
        hashFn :Function = null)
    {
        super(loadFactor, equalsFn, hashFn);
    }

    /** @inheritDoc */
    override public function get (key :Object) :*
    {
        var result :* = super.get(key);
        if (result is WeakReference) { // could also just be undefined or null
            result = WeakReference(result).get();
            if (result === undefined) {
                super.remove(key);
            }
        }
        return result;
    }

    /** @inheritDoc */
    override public function put (key :Object, value :Object) :*
    {
        // store nulls directly
        return unwrap(super.put(key, (value == null) ? null : new WeakReference(value)));
    }

    /** @inheritDoc */
    override public function remove (key :Object) :*
    {
        return unwrap(super.remove(key));
    }

    /** @inheritDoc */
    override public function size () :int
    {
        forEach0(function (k :*, v: *) :void {});
        return super.size(); // alternately, we could prune and increment a count for all present..
    }

    /** @private */
    override protected function forEach0 (fn :Function) :void
    {
        var removeKeys :Array = [];
        super.forEach0(function (key :*, value :WeakReference) :void {
            var rawVal :* = (value == null) ? null : value.get();
            if (rawVal === undefined) {
                removeKeys.push(key);
            } else {
                fn(key, rawVal);
            }
        });
        for each (var key :Object in removeKeys) {
            super.remove(key); // slightly more efficient, since we don't need to unwrap
        }
    }

    /**
     * Unwrap a possible WeakReference for returning to the user.
     * @private
     */
    protected function unwrap (val :*) :*
    {
        return (val is WeakReference) ? WeakReference(val).get() : val;
    }
}
}
