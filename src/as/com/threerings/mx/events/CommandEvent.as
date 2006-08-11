package com.threerings.mx.events {

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
            disp :IEventDispatcher, cmd :String, arg :Object = null) :void
    {
        // Create the event
        var event :CommandEvent = create(cmd, arg);

        // Dispatch it. A return value of true means that the event was
        // never cancelled, so we complain.
        if (disp == null || disp.dispatchEvent(event)) {
            Log.getLog(CommandEvent).warning("Unhandled controller command " +
                "[cmd=" + cmd + ", arg=" + arg + "].");
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
