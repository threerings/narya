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

import flash.display.Sprite;

import flash.events.Event;

/**
 * A simple mechanism for queueing functions to be called on the next frame.
 * Similar to UIComponent's callLater, only flex-free.
 */
public class MethodQueue
{
    /**
     * Call the specified method at the entry to the next frame.
     */
    public static function callLater (fn :Function, args :Array = null) :void
    {
        _methodQueue.push([fn, args]);
        if (!_d.hasEventListener(Event.ENTER_FRAME)) {
            _d.addEventListener(Event.ENTER_FRAME, handleEnterFrame);
        }
    }

    /**
     * Handle a frame event: call any queued functions.
     */
    protected static function handleEnterFrame (event :Event) :void
    {
        // swap out the working set
        var methods :Array = _methodQueue;
        _methodQueue = [];

        // safely call each function
        for each (var arr :Array in methods) {
            var fn :Function = (arr[0] as Function);
            var args :Array = (arr[1] as Array);
            try {
                fn.apply(null, args);

            } catch (e :Error) {
                Log.getLog(MethodQueue).warning("Error calling deferred method " +
                    "[e=" + e + ", fn=" + fn + ", args=" + args + "].");
            }
        }

        // If no new functions were added while we were calling the current set,
        // then remove the listener.
        if (_methodQueue.length == 0) {
            _d.removeEventListener(Event.ENTER_FRAME, handleEnterFrame);
        }
    }

    /** A display object on which we listen for ENTER_FRAME. */
    protected static var _d :Sprite = new Sprite();

    /** The currently queued functions. */
    protected static var _methodQueue :Array = [];
}
}
