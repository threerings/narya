//
// $Id$

package com.threerings.util {

import flash.events.Event;

public class ElementExpiredEvent extends Event
{
    public static const ELEMENT_EXPIRED :String = "ElementExpired";

    public function ElementExpiredEvent (element :Object)
    {
        super(ELEMENT_EXPIRED);
        _element = element;
    }

    public function get element () :Object
    {
        return _element;
    }

    override public function clone () :Event
    {
        return new ElementExpiredEvent(_element);
    }

    protected var _element :Object;
}
}
