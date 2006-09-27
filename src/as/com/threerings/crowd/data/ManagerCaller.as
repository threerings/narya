//
// $Id$

package com.threerings.crowd.data {

public class ManagerCaller
{
    public function ManagerCaller (plobj :PlaceObject)
    {
        _plobj = plobj;
    }

    /**
     * Called to call a method on the manager.
     */
    public function invoke (method :String, ... args) :void
    {
        _plobj.postMessage(method, args);
    }

    /** The place object we're thingy-ing for. */
    protected var _plobj :PlaceObject;
}
}
