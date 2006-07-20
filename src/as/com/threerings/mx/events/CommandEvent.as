package com.threerings.mx.events {

import flash.events.Event;

public class CommandEvent extends Event
{
    /** The event type for all controller events. */
    public static const TYPE :String = "commandEvt";

    public var command :String;
    public var arg :Object;

    public function CommandEvent (command :String, arg :Object = null)
    {
        super(TYPE, true);
        this.command = command;
        this.arg = arg;
    }

    override public function clone () :Event
    {
        return new CommandEvent(command, arg);
    }

    override public function toString () :String
    {
        return "CommandEvent[" + command + " (" + arg + ")]";
    }
}
}
