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

import flash.errors.IllegalOperationError;

import flash.events.Event;
import flash.events.IEventDispatcher;

public class CommandEvent extends Event
{
    /** The event type for all controller events. */
    public static const TYPE :String = "commandEvt";

    /**
     * Use this method to dispatch CommandEvents.
     */
    public static function dispatch (
            disp :IEventDispatcher, cmdOrFn :Object, arg :Object = null) :void
    {
        if (cmdOrFn is Function) {
            var fn :Function = (cmdOrFn as Function);
            // build our args array
            var args :Array;
            if (arg is Array) {
                // if we were passed an array, treat it as the arg array.
                // Note: if you want to pass a single array param, you've
                // got to wrap it in another array, so sorry.
                args = arg as Array;

            } else {
                args = [ arg ];
            }
            try {
                fn.apply(null, args);
            } catch (err :Error) {
                if (arg == null) {
                    try {
                        // try with no args
                        fn();
                        err = null; // on success, clear the error
                    } catch (err2 :Error) {
                        err = err2;
                    }
                }
                if (err != null) {
                    var log :Log = Log.getLog(CommandEvent);
                    log.warning("Unable to call command callback, stack trace follows.");
                    log.logStackTrace(err);
                }
            }

        } else if (cmdOrFn is String) {
            var cmd :String = String(cmdOrFn);
            // Create the event to dispatch
            var event :CommandEvent = create(cmd, arg);

            // Dispatch it. A return value of true means that the event was
            // never cancelled, so we complain.
            if (disp == null || disp.dispatchEvent(event)) {
                Log.getLog(CommandEvent).warning("Unhandled controller command " +
                    "[cmd=" + cmd + ", arg=" + arg + "].");
            }

        } else {
            throw new ArgumentError("Argument 'cmdOrFn' must be a command (String) or a Function");
        }
    }

    /** The command. */
    public var command :String;

    /** An optional argument. */
    public var arg :Object;

    /**
     * Command events may not be directly constructed, use the dispatch
     * method to do your work.
     */
    public function CommandEvent (command :String, arg :Object)
    {
        super(TYPE, true, true);
        if (_blockConstructor) {
            throw new IllegalOperationError();
        }
        this.command = command;
        this.arg = arg;
    }

    /**
     * Mark this command as handled, stopping its propagation up the
     * hierarchy.
     */
    public function markAsHandled () :void
    {
        preventDefault();
        stopImmediatePropagation();
    }

    override public function clone () :Event
    {
        return create(command, arg);
    }

    override public function toString () :String
    {
        return "CommandEvent[" + command + " (" + arg + ")]";
    }

    /**
     * A factory method for privately creating command events.
     */
    protected static function create (cmd :String, arg :Object) :CommandEvent
    {
        var event :CommandEvent;
        _blockConstructor = false;
        try {
            event = new CommandEvent(cmd, arg);
        } finally {
            _blockConstructor = true;
        }
        return event;
    }

    /** Used to prevent unauthorized construction. */
    protected static var _blockConstructor :Boolean = true;
}
}
