//
// $Id: Simulant.java,v 1.1 2001/12/19 09:32:02 shaper Exp $

package com.threerings.micasa.simulator.client;

import com.threerings.crowd.data.BodyObject;
import com.threerings.crowd.data.PlaceObject;

public abstract class Simulant
{
    /**
     * Sets the body object associated with this simulant.
     */
    public void setBodyObject (BodyObject self)
    {
        _self = self;
    }

    /**
     * Called when the simulant is about to enter the room in which it
     * will be doing all of its business.
     */
    public abstract void willEnterPlace (PlaceObject plobj);

    /** Our body object. */
    protected BodyObject _self;
}
