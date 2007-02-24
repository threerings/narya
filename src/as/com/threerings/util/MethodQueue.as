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

import flash.display.Stage;

import flash.events.Event;

/**
 * A simple mechanism for queueing functions to be called on the next frame.
 * Similar to UIComponent's callLater, only flex-free.
 */
public class MethodQueue
{
    /**
     * Set the stage which will be used for coordinating the use of the
     * method queue. If no stage is set, functions will continue to pile up
     * without being called.
     */
    public static function setStage (stage :Stage) :void
    {
        if (_listening) {
            removeListener();
        }
        _stage = stage;
        checkListen();
    }

    /**
     * Call the specified method at the entry to the next frame.
     */
    public static function callLater (fn :Function, args :Array = null) :void
    {
        _methodQueue.push([fn, args]);
        if (!_listening) {
            checkListen();
        }
    }

    /**
     * Stop listening for the frame event.
     */
    protected static function removeListener () :void
    {
        _stage.removeEventListener(Event.ENTER_FRAME, handleEnterFrame);
        _listening = false;
    }

    /**
     * Check to see if we should be listening for the next frame event.
     */
    protected static function checkListen () :void
    {
        if (_stage != null && _methodQueue.length > 0) {
            _stage.addEventListener(Event.ENTER_FRAME, handleEnterFrame);
            _listening = true;
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
            removeListener();
        }
    }

    /** The stage we're working with. */
    protected static var _stage :Stage;

    /** The currently queued functions. */
    protected static var _methodQueue :Array = [];

    /** Are we presently listening for the frame event? */
    protected static var _listening :Boolean = false;
}
}
