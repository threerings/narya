package com.threerings.util {

import flash.events.Event;

/**
 * A handy event for simply dispatching a value associated with the event type.
 */
public class ValueEvent extends Event
{
    /**
     * Accessor: get the value.
     */
    public function get value () :Object
    {
        return _value;
    }

    /**
     * Construct the value event.
     */
    public function ValueEvent (type :String, value :Object)
    {
        super(type);
        _value = value;
    }

    override public function clone () :Event
    {
        return new ValueEvent(type, _value);
    }

    /** The value. */
    protected var _value :Object;
}
}
