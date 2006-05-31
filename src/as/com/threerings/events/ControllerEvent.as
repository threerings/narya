package com.threerings.events {

import flash.events.Event;

public class ControllerEvent extends Event
{
    /** The event type for all controller events. */
    public static const TYPE :String = "controller";

    public var command :String;
    public var arg :Object;

    public function ControllerEvent (command :String, arg :Object = null)
    {
        super(TYPE, true);
        this.command = command;
        this.arg = arg;
    }

    public override function clone () :Event
    {
        return new ControllerEvent(command, arg);
    }

    public override function toString () :String
    {
        return "ControllerEvent[" + command + " (" + arg + ")]";
    }
}
}
