//
// $Id: SimulatorContext.java,v 1.1 2001/12/19 09:32:02 shaper Exp $

package com.threerings.micasa.simulator.util;

import com.threerings.parlor.util.ParlorContext;

import com.threerings.micasa.simulator.client.SimulatorFrame;
import com.threerings.micasa.simulator.data.SimulatorInfo;

/**
 * The simulator context encapsulates the contexts of all of the services
 * that are used by the simulator client so that we can pass around one
 * single context implementation that provides all of the necessary
 * components to all of the services in use.
 */
public interface SimulatorContext
    extends ParlorContext
{
    /** Returns a reference to the primary user interface frame. */
    public SimulatorFrame getFrame ();

    /** Returns a reference to the simulator info describing the game and
     * other details of the simulation. */
    public SimulatorInfo getSimulatorInfo ();
}
