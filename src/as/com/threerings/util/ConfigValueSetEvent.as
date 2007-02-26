package com.threerings.util {

import flash.events.Event;

/**
 * Dispatched whenever a config value is changed.
 */
public class ConfigValueSetEvent extends Event
{
    /** The type of a ConfigValueSetEvent. */
    public static const TYPE :String = "ConfigValSet";

    /** The name of the config value set. */
    public var name :String;

    /** The new value. */
    public var value :Object;

    /**
     */
    public function ConfigValueSetEvent (name :String, value :Object)
    {
        super(TYPE);

        this.name = name;
        this.value = value;
    }

    override public function clone () :Event
    {
        return new ConfigValueSetEvent(name, value);
    }
}
}
